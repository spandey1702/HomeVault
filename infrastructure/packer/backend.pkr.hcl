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

# Backend AMI Builder
source "amazon-ebs" "backend" {
  ami_name      = "${var.project_name}-backend-${var.environment}-{{timestamp}}"
  instance_type = var.instance_type
  region        = var.aws_region
  source_ami    = data.amazon-ami.ubuntu.id
  ssh_username  = "ubuntu"

  tags = {
    Name        = "${var.project_name}-backend-${var.environment}"
    Environment = var.environment
    Project     = var.project_name
    OS          = "Ubuntu 22.04"
    Created     = "{{timestamp}}"
    BuildBy     = "Packer"
  }

  run_tags = {
    Name = "${var.project_name}-packer-builder"
  }
}

# Build Configuration
build {
  name = "homevault-backend"
  sources = ["source.amazon-ebs.backend"]

  # Wait for cloud-init to complete
  provisioner "shell" {
    inline = [
      "echo 'Waiting for cloud-init to complete...'",
      "cloud-init status --wait"
    ]
  }

  # Update system packages
  provisioner "shell" {
    inline = [
      "echo 'Updating system packages...'",
      "sudo apt-get update",
      "sudo apt-get upgrade -y"
    ]
  }

  # Install Java 17 for Spring Boot
  provisioner "shell" {
    inline = [
      "echo 'Installing Java 17...'",
      "sudo apt-get install -y openjdk-17-jdk",
      "java -version"
    ]
  }

  # Install Docker
  provisioner "shell" {
    inline = [
      "echo 'Installing Docker...'",
      "sudo apt-get install -y apt-transport-https ca-certificates curl software-properties-common",
      "curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg",
      "echo 'deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable' | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null",
      "sudo apt-get update",
      "sudo apt-get install -y docker-ce docker-ce-cli containerd.io",
      "sudo systemctl enable docker",
      "sudo systemctl start docker",
      "sudo usermod -aG docker ubuntu"
    ]
  }

  # Install AWS CLI
  provisioner "shell" {
    inline = [
      "echo 'Installing AWS CLI...'",
      "curl 'https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip' -o 'awscliv2.zip'",
      "sudo apt-get install -y unzip",
      "unzip awscliv2.zip",
      "sudo ./aws/install",
      "rm -rf aws awscliv2.zip",
      "aws --version"
    ]
  }

  # Install CloudWatch Agent
  provisioner "shell" {
    inline = [
      "echo 'Installing CloudWatch Agent...'",
      "wget https://s3.amazonaws.com/amazoncloudwatch-agent/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb",
      "sudo dpkg -i -E ./amazon-cloudwatch-agent.deb",
      "rm amazon-cloudwatch-agent.deb"
    ]
  }

  # Install monitoring + mail-testing tools
  provisioner "shell" {
    inline = [
      "echo 'Installing monitoring and utilities...'",
      "sudo apt-get install -y htop netcat-openbsd curl wget jq mailutils"
    ]
  }

  # Verify SES SMTP connectivity (smoke test — does not send real mail)
  provisioner "shell" {
    inline = [
      "echo 'Verifying SES SMTP port is reachable...'",
      "nc -zv -w5 email-smtp.us-east-1.amazonaws.com 587 && echo 'SMTP port reachable' || echo 'Warning: SMTP port not reachable from build instance'"
    ]
  }

  # Create application directory structure
  provisioner "shell" {
    inline = [
      "echo 'Creating application directories...'",
      "sudo mkdir -p /opt/homevault",
      "sudo mkdir -p /var/log/homevault",
      "sudo chown -R ubuntu:ubuntu /opt/homevault",
      "sudo chown -R ubuntu:ubuntu /var/log/homevault"
    ]
  }

  # Copy application startup script
  provisioner "file" {
    source      = "scripts/start-backend.sh"
    destination = "/tmp/start-backend.sh"
  }

  provisioner "shell" {
    inline = [
      "sudo mv /tmp/start-backend.sh /opt/homevault/start-backend.sh",
      "sudo chmod +x /opt/homevault/start-backend.sh",
      "sudo chown ubuntu:ubuntu /opt/homevault/start-backend.sh"
    ]
  }

  # Copy systemd service file
  provisioner "file" {
    source      = "scripts/homevault-backend.service"
    destination = "/tmp/homevault-backend.service"
  }

  provisioner "shell" {
    inline = [
      "sudo mv /tmp/homevault-backend.service /etc/systemd/system/homevault-backend.service",
      "sudo systemctl daemon-reload",
      "sudo systemctl enable homevault-backend.service"
    ]
  }

  # Copy CloudWatch configuration
  provisioner "file" {
    source      = "scripts/cloudwatch-config.json"
    destination = "/tmp/cloudwatch-config.json"
  }

  provisioner "shell" {
    inline = [
      "sudo mv /tmp/cloudwatch-config.json /opt/aws/amazon-cloudwatch-agent/etc/cloudwatch-config.json"
    ]
  }

  # Clean up
  provisioner "shell" {
    inline = [
      "echo 'Cleaning up...'",
      "sudo apt-get clean",
      "sudo rm -rf /var/lib/apt/lists/*",
      "sudo rm -rf /tmp/*",
      "history -c"
    ]
  }

  # Post-processor to create a manifest
  post-processor "manifest" {
    output     = "manifest.json"
    strip_path = true
  }
}
