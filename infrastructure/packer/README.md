# HomeVault Packer Configuration

Build custom AMIs for the HomeVault application using Packer.

## What's Included

- **Backend AMI** - Ubuntu 22.04 with Java 17, Docker, AWS CLI
- **Database AMI** - Ubuntu 22.04 with PostgreSQL 15 (optional, if not using RDS)

## Prerequisites

1. **Packer** installed (v1.8+)
   ```bash
   # macOS
   brew install packer
   
   # Linux
   wget https://releases.hashicorp.com/packer/1.9.4/packer_1.9.4_linux_amd64.zip
   unzip packer_1.9.4_linux_amd64.zip
   sudo mv packer /usr/local/bin/
   ```

2. **AWS CLI** configured with appropriate permissions

3. **AWS Credentials** with permissions to:
   - Create EC2 instances
   - Create AMIs
   - Create security groups
   - Upload to S3

## Quick Start

### 1. Initialize Packer

```bash
cd packer
make init
```

### 2. Validate Templates

```bash
make validate
```

### 3. Build AMIs

```bash
# Build backend AMI
make build-backend

# Build database AMI (optional)
make build-database

# Build all AMIs
make build-all
```

## Configuration

### Variables

Edit `variables.pkrvars.hcl`:

```hcl
aws_region    = "us-east-1"
project_name  = "homevault"
environment   = "dev"
instance_type = "t3.micro"
```

### Backend AMI Includes

- Ubuntu 22.04 LTS
- OpenJDK 17
- Docker & Docker Compose
- AWS CLI v2
- CloudWatch Agent
- Application startup scripts
- Systemd service configuration
- Health check scripts

### Database AMI Includes

- Ubuntu 22.04 LTS
- PostgreSQL 15
- Optimized PostgreSQL configuration
- Automated backup scripts
- CloudWatch monitoring
- S3 backup integration

## Usage

### Using Backend AMI

After building the backend AMI, you can:

1. **Launch EC2 instances** with the AMI
2. **Use in Auto Scaling Groups**
3. **Reference in Terraform**:

```hcl
data "aws_ami" "backend" {
  most_recent = true
  owners      = ["self"]

  filter {
    name   = "name"
    values = ["homevault-backend-*"]
  }

  filter {
    name   = "tag:Project"
    values = ["homevault"]
  }
}

resource "aws_instance" "backend" {
  ami           = data.aws_ami.backend.id
  instance_type = "t3.micro"
  
  user_data = <<-EOF
    #!/bin/bash
    export ECR_REPOSITORY="${aws_ecr_repository.backend.repository_url}"
    export DB_SECRET_ARN="${aws_secretsmanager_secret.db_credentials.arn}"
    export APP_SECRET_ARN="${aws_secretsmanager_secret.app_secrets.arn}"
    
    sudo systemctl start homevault-backend
  EOF
}
```

### Using Database AMI

For standalone PostgreSQL (if not using RDS):

```hcl
data "aws_ami" "database" {
  most_recent = true
  owners      = ["self"]

  filter {
    name   = "name"
    values = ["homevault-database-*"]
  }
}

resource "aws_instance" "database" {
  ami           = data.aws_ami.database.id
  instance_type = "t3.small"
  
  user_data = <<-EOF
    #!/bin/bash
    # Initialize database
    sudo -u postgres psql -c "CREATE DATABASE homevault;"
    sudo -u postgres psql -c "CREATE USER homevault_user WITH PASSWORD 'secure_password';"
    sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE homevault TO homevault_user;"
  EOF
}
```

## Build Process

### Backend AMI Build Steps

1. Start with Ubuntu 22.04 base AMI
2. Update system packages
3. Install Java 17
4. Install Docker
5. Install AWS CLI
6. Install CloudWatch Agent
7. Create application directories
8. Copy startup scripts
9. Configure systemd service
10. Clean up and create AMI

**Build time**: ~10-15 minutes

### Database AMI Build Steps

1. Start with Ubuntu 22.04 base AMI
2. Update system packages
3. Install PostgreSQL 15
4. Configure PostgreSQL
5. Install backup tools
6. Setup automated backups
7. Install CloudWatch Agent
8. Clean up and create AMI

**Build time**: ~8-10 minutes

## Scripts

### Backend Scripts

- `start-backend.sh` - Pulls Docker image and starts container
- `homevault-backend.service` - Systemd service file
- `cloudwatch-config.json` - CloudWatch agent configuration

### Database Scripts

- `backup-db.sh` - Database backup to S3
- `postgresql.conf` - PostgreSQL configuration
- `pg_hba.conf` - PostgreSQL authentication

## Management

### List AMIs

```bash
make list-amis
```

### Test AMI

```bash
make test-backend
# or
make test-database
```

### Clean Up

```bash
# Clean build artifacts
make clean

# Delete old AMIs (30+ days)
make delete-old-amis
```

## Environment Variables

The backend service expects these environment variables:

- `ECR_REPOSITORY` - ECR repository URL
- `DB_SECRET_ARN` - Secrets Manager ARN for database credentials
- `APP_SECRET_ARN` - Secrets Manager ARN for app secrets
- `AWS_REGION` - AWS region

Set these via EC2 user data or environment file at `/etc/homevault/environment`.

## Monitoring

Both AMIs include CloudWatch Agent configured to send:

- **System metrics**: CPU, memory, disk usage
- **Application logs**: /var/log/homevault/
- **System logs**: /var/log/syslog

View logs in CloudWatch:
- Log Group: `/homevault/backend` or `/homevault/database`

## Costs

Building AMIs:
- **Instance usage**: ~$0.02 per build (10-15 mins on t3.micro)
- **AMI storage**: ~$0.05/GB/month per AMI
- **Total**: ~$0.15-0.30 per month for 2-3 AMIs

## Troubleshooting

### Build Fails

```bash
# Check Packer logs
export PACKER_LOG=1
packer build -var-file=variables.pkrvars.hcl backend.pkr.hcl
```

### AMI Won't Boot

1. Check CloudWatch logs
2. SSH into test instance
3. Check systemd service status:
   ```bash
   sudo systemctl status homevault-backend
   sudo journalctl -u homevault-backend
   ```

### Docker Issues

```bash
# Check Docker service
sudo systemctl status docker

# Check Docker logs
sudo journalctl -u docker

# Test Docker
docker run hello-world
```

## Integration with Terraform

These AMIs can be used with your Terraform configuration:

```hcl
# In terraform/main.tf
data "aws_ami" "backend" {
  most_recent = true
  owners      = ["self"]
  
  filter {
    name   = "name"
    values = ["homevault-backend-dev-*"]
  }
}

# Use in Launch Template for Auto Scaling
resource "aws_launch_template" "backend" {
  name_prefix   = "homevault-backend-"
  image_id      = data.aws_ami.backend.id
  instance_type = "t3.micro"
  
  # ... rest of configuration
}
```

## Best Practices

1. **Version your AMIs** - Include timestamp in name
2. **Test before production** - Always test AMIs in dev first
3. **Automate builds** - Use CI/CD to build AMIs on changes
4. **Clean up old AMIs** - Delete AMIs older than 30 days
5. **Tag properly** - Use consistent tagging for tracking
6. **Document changes** - Keep changelog of AMI updates

## File Structure

```
packer/
├── backend.pkr.hcl              # Backend AMI template
├── database.pkr.hcl             # Database AMI template
├── variables.pkrvars.hcl        # Variable definitions
├── Makefile                     # Build commands
├── README.md                    # This file
└── scripts/
    ├── start-backend.sh         # Backend startup script
    ├── homevault-backend.service # Systemd service
    ├── cloudwatch-config.json   # CloudWatch configuration
    ├── postgresql.conf          # PostgreSQL config
    ├── pg_hba.conf             # PostgreSQL auth
    └── backup-db.sh            # Database backup script
```

## Support

For issues:
1. Check Packer logs with `PACKER_LOG=1`
2. Verify AWS credentials and permissions
3. Test AMI with make test-backend or test-database
4. Review CloudWatch logs after instance launch
