# HomeVault Terraform Infrastructure

Simple AWS infrastructure setup for HomeVault application.

## What's Included

- **VPC** with public and private subnets
- **RDS PostgreSQL** database
- **ECS Fargate** cluster for containers
- **Application Load Balancer** for traffic distribution
- **ECR** for Docker images
- **Security Groups** for network security
- **Route 53** (optional) for custom domain

## Quick Start

### 1. Prerequisites

- AWS CLI installed and configured
- Terraform installed (v1.0+)
- Docker installed

### 2. Setup

```bash
cd terraform

# Copy and edit variables
cp terraform.tfvars.example terraform.tfvars
nano terraform.tfvars  # Update with your values

# Initialize Terraform
terraform init

# Preview changes
terraform plan

# Create infrastructure
terraform apply
```

### 3. Deploy Application

```bash
# Login to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin $(terraform output -raw ecr_repository_url | cut -d'/' -f1)

# Build and push image
cd ..
docker build -t homevault-backend -f backend/Dockerfile.backend .
docker tag homevault-backend:latest $(cd terraform && terraform output -raw ecr_repository_url):latest
docker push $(cd terraform && terraform output -raw ecr_repository_url):latest

# Update ECS service
cd terraform
aws ecs update-service --cluster $(terraform output -raw ecs_cluster_name) --service $(terraform output -raw ecs_service_name) --force-new-deployment
```

### 4. Access Application

```bash
terraform output application_url
```

Visit the URL shown to access your application.

## Configuration

### Variables

Edit `terraform.tfvars`:

- `aws_region` - AWS region (default: us-east-1)
- `project_name` - Project name (default: homevault)
- `db_password` - **Required** - Database password
- `session_secret` - **Required** - Application session secret
- `domain_name` - Optional - Your custom domain

### Resources Created

- VPC with 2 public and 2 private subnets
- Internet Gateway and NAT Gateway
- RDS PostgreSQL (db.t3.micro)
- ECS Fargate cluster (0.5 vCPU, 1GB RAM)
- Application Load Balancer
- ECR Repository
- Security Groups
- IAM Roles

## Cost Estimate

Approximate monthly costs:
- RDS: ~$15
- ECS Fargate: ~$15
- NAT Gateway: ~$32
- Load Balancer: ~$16
- **Total: ~$78/month**

## Cleanup

To destroy all resources:

```bash
terraform destroy
```

## Troubleshooting

### Check ECS Service Status
```bash
aws ecs describe-services --cluster $(terraform output -raw ecs_cluster_name) --services $(terraform output -raw ecs_service_name)
```

### View Logs
```bash
aws logs tail /ecs/homevault-backend --follow
```

### Database Connection Issues
- Check security groups
- Verify database endpoint in outputs
- Ensure ECS tasks are in private subnets

## File Structure

```
terraform/
├── main.tf           # VPC and networking
├── security.tf       # Security groups
├── rds.tf           # Database
├── alb.tf           # Load balancer
├── ecs.tf           # ECS cluster and service
├── ecr.tf           # Docker registry
├── route53.tf       # DNS (optional)
├── variables.tf     # Input variables
├── outputs.tf       # Output values
└── terraform.tfvars # Your configuration
```

## Support

For issues:
1. Check CloudWatch logs
2. Verify security group rules
3. Review ECS task status
