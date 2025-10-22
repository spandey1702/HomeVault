packer {
  required_plugins {
    amazon = {
      version = ">= 1.2.0"
      source  = "github.com/hashicorp/amazon"
    }
  }
}

# Variables
variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "project_name" {
  type    = string
  default = "homevault"
}

variable "environment" {
  type    = string
  default = "dev"
}

variable "instance_type" {
  type    = string
  default = "t3.micro"
}

# Data source for latest Ubuntu AMI
data "amazon-ami" "ubuntu" {
  filters = {
    name                = "ubuntu/images/hvm-ssd/ubuntu-22.04-amd64-server-*"
    root-device-type    = "ebs"
    virtualization-type = "hvm"
  }
  most_recent = true
  owners      = ["099720109477"] # Canonical
  region      = var.aws_region
}

# Database AMI Builder (for standalone PostgreSQL if needed)
source "amazon-ebs" "database" {
  ami_name      = "${var.project_name}-database-${var.environment}-{{timestamp}}"
  instance_type = var.instance_type
  region        = var.aws_region
  source_ami    = data.amazon-ami.ubuntu.id
  ssh_username  = "ubuntu"

  tags = {
    Name        = "${var.project_name}-database-${var.environment}"
    Environment = var.environment
    Project     = var.project_name
    OS          = "Ubuntu 22.04"
    Created     = "{{timestamp}}"
    BuildBy     = "Packer"
  }

  run_tags = {
    Name = "${var.project_name}-packer-db-builder"
  }
}

# Build Configuration
build {
  name = "homevault-database"
  sources = ["source.amazon-ebs.database"]

  # Wait for cloud-init
  provisioner "shell" {
    inline = [
      "echo 'Waiting for cloud-init...'",
      "cloud-init status --wait"
    ]
  }

  # Update system
  provisioner "shell" {
    inline = [
      "sudo apt-get update",
      "sudo apt-get upgrade -y"
    ]
  }

  # Install PostgreSQL 15
  provisioner "shell" {
    inline = [
      "echo 'Installing PostgreSQL 15...'",
      "sudo sh -c 'echo \"deb http://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main\" > /etc/apt/sources.list.d/pgdg.list'",
      "wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | sudo apt-key add -",
      "sudo apt-get update",
      "sudo apt-get install -y postgresql-15 postgresql-contrib-15"
    ]
  }

  # Configure PostgreSQL
  provisioner "file" {
    source      = "scripts/postgresql.conf"
    destination = "/tmp/postgresql.conf"
  }

  provisioner "file" {
    source      = "scripts/pg_hba.conf"
    destination = "/tmp/pg_hba.conf"
  }

  provisioner "shell" {
    inline = [
      "sudo systemctl stop postgresql",
      "sudo cp /tmp/postgresql.conf /etc/postgresql/15/main/postgresql.conf",
      "sudo cp /tmp/pg_hba.conf /etc/postgresql/15/main/pg_hba.conf",
      "sudo chown postgres:postgres /etc/postgresql/15/main/*.conf"
    ]
  }

  # Install backup tools
  provisioner "shell" {
    inline = [
      "sudo apt-get install -y postgresql-client-15 awscli"
    ]
  }

  # Copy backup script
  provisioner "file" {
    source      = "scripts/backup-db.sh"
    destination = "/tmp/backup-db.sh"
  }

  provisioner "shell" {
    inline = [
      "sudo mv /tmp/backup-db.sh /usr/local/bin/backup-db.sh",
      "sudo chmod +x /usr/local/bin/backup-db.sh"
    ]
  }

  # Setup cron for backups
  provisioner "shell" {
    inline = [
      "echo '0 2 * * * /usr/local/bin/backup-db.sh' | sudo crontab -"
    ]
  }

  # Install CloudWatch agent
  provisioner "shell" {
    inline = [
      "wget https://s3.amazonaws.com/amazoncloudwatch-agent/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb",
      "sudo dpkg -i -E ./amazon-cloudwatch-agent.deb",
      "rm amazon-cloudwatch-agent.deb"
    ]
  }

  # Clean up
  provisioner "shell" {
    inline = [
      "sudo apt-get clean",
      "sudo rm -rf /var/lib/apt/lists/*",
      "sudo rm -rf /tmp/*",
      "history -c"
    ]
  }

  post-processor "manifest" {
    output     = "database-manifest.json"
    strip_path = true
  }
}
