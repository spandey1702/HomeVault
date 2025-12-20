HomeVault is a cloud-native application designed to help households manage their inventory efficiently. Built with modern microservices architecture and deployed on AWS using infrastructure-as-code principles.

The Problem
Households struggle with:
- Tracking expiration dates of food and medications
- Managing multi-location inventory (pantry, freezer, garage)
- Sharing household items among family members
- Preventing waste from expired products

The Solution
HomeVault provides:
- Centralized inventory management** with category and location organization
- Smart expiration tracking** with automated notifications
- Multi-user households** with shared access and permissions
- RESTful API for potential mobile app integration
- Cloud-native deployment with auto-scaling and high availability

---
Key Features
### Core Functionality
User Authentication & Authorization- Secure JWT-based auth with password encryption
Item Management- Create, read, update, delete inventory items
Advanced Categorization - Organize by category, location, brand, and custom tags
Expiration Tracking - Smart alerts for items expiring within 7 days
Household Sharing - Multi-user households with member management
Price Tracking - Monitor inventory value and spending patterns
Search & Filter - Fast queries by name, category, location, or expiration

### Technical Features
Enterprise Security - password hashing, JWT tokens, AWS Secrets Manager
Auto-scaling - ECS Fargate with automatic scaling based on load
CI/CD Ready- Containerized with Docker, automated deployments
Comprehensive Logging - Structured logging with CloudWatch integration
Infrastructure as Code - Complete Terraform and Packer configurations
Automated Backups - RDS automated backups with point-in-time recovery
CORS Support - Ready for SPA frontend integration

## Technology Stack
### Backend
Java 17- Modern LTS version with performance improvements
Spring Boot 3.2.0 - Latest framework with native compilation support
pring Security- Authentication and authorization
Spring Data JPA - ORM with Hibernate
PostgreSQL 15 - Relational database
Maven - Dependency management and build automation

### Frontend
React 18 - Modern UI with hooks and concurrent features
React Router - Client-side routing
Axios - HTTP client for API calls

### DevOps & Infrastructure
Docker- Containerization
AWS ECS Fargate - container orchestration
AWS RDS- Managed PostgreSQL database
AWS ECR - Container registry
AWS ALB - Application load balancing
AWS VPC - Network isolation with public/private subnets
Terraform- Infrastructure provisioning
Packer- AMI building and configuration
CloudWatch - Monitoring and logging
Secrets Manager - Secure credential storage

Prerequisites

Java 17+
Maven 3.8+
Docker & Docker Compose
PostgreSQL 15 (or use Docker)
Node.js 18+ (for frontend)
AWS CLI (for cloud deployment)
Terraform 1.0+ (for infrastructure)

Local Development Setup

Clone the repository

bash   git clone https://github.com/yourusername/homevault.git
   cd homevault

Start PostgreSQL (using Docker)

bash   docker run -d \
     --name homevault-db \
     -e POSTGRES_DB=homevault \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=postgres \
     -p 5432:5432 \
     postgres:15-alpine

Initialize database

bash   psql -h localhost -U postgres -d homevault -f database/init.sql

Configure backend

bash   cd backend
# Create application.properties
   cat > src/main/resources/application-local.properties << EOF
   spring.datasource.url=jdbc:postgresql://localhost:5432/homevault
   spring.datasource.username=postgres
   spring.datasource.password=postgres
   app.jwt.secret=your-secret-key-change-this-in-production
   EOF

Run backend

bash   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
Backend will start on http://localhost:8080

Run frontend (in a new terminal)

bash   cd frontend
   npm install
   npm start
Frontend will start on http://localhost:3000

Access the application

Backend API: http://localhost:8080/api
Health Check: http://localhost:8080/api/health
API Documentation: http://localhost:8080/swagger-ui.html (if Swagger is added)



Testing the API
bash# Register a user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "securePassword123"
  }'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "securePassword123"
  }'

# Create an item (use token from login response)
curl -X POST http://localhost:8080/api/items \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "Milk",
    "category": "Dairy",
    "location": "Refrigerator",
    "quantity": 2,
    "expiryDate": "2024-12-25"
  }'

🌩️ Deployment
AWS Infrastructure Deployment
This project includes complete Infrastructure as Code (IaC) using Terraform and Packer for reproducible, automated deployments.
Prerequisites

AWS Account with appropriate permissions
AWS CLI configured
Terraform 1.0+
Docker installed
Packer 1.8+ (optional, for custom AMIs)

1. Build Infrastructure with Terraform
bashcd infrastructure/terraform

# Initialize Terraform
terraform init

# Create terraform.tfvars with your configuration
cat > terraform.tfvars << EOF
aws_region        = "us-east-1"
project_name      = "homevault"
environment       = "prod"
db_username       = "admin"
db_password       = "YourSecurePassword123!"  # Change this!
session_secret    = "YourLongRandomSecret456!" # Change this!
# domain_name     = "yourdomain.com"           # Optional
EOF

# Review the execution plan
terraform plan

# Deploy infrastructure
terraform apply

# Save outputs for later use
terraform output > outputs.txt
What Gets Created:

VPC with public/private/database subnets across 2 AZs
Internet Gateway and 2 NAT Gateways
Application Load Balancer with target group
ECS Fargate cluster with task definition
RDS PostgreSQL 15 instance (Multi-AZ)
ECR repository for Docker images
Security groups with least-privilege access
Secrets Manager for credentials
CloudWatch log groups, dashboards, and alarms
IAM roles and policies

Estimated Time: 15-20 minutes
2. Build and Push Backend Docker Image
bash# Login to ECR
aws ecr get-login-password --region us-east-1 | \
  docker login --username AWS --password-stdin \
  $(cd infrastructure/terraform && terraform output -raw ecr_repository_url | cut -d'/' -f1)

# Build backend image from project root
cd ../..
docker build -t homevault-backend -f backend/Dockerfile.backend .

# Tag for ECR
docker tag homevault-backend:latest \
  $(cd infrastructure/terraform && terraform output -raw ecr_repository_url):latest

# Push to ECR
docker push \
  $(cd infrastructure/terraform && terraform output -raw ecr_repository_url):latest
3. Deploy Application to ECS
bashcd infrastructure/terraform

# Update ECS service to deploy new image
aws ecs update-service \
  --cluster $(terraform output -raw ecs_cluster_name) \
  --service $(terraform output -raw ecs_service_name) \
  --force-new-deployment \
  --region us-east-1

# Monitor deployment status
aws ecs describe-services \
  --cluster $(terraform output -raw ecs_cluster_name) \
  --services $(terraform output -raw ecs_service_name) \
  --query 'services[0].deployments' \
  --region us-east-1
4. Verify Deployment
bash# Get application URL
APPLICATION_URL=$(terraform output -raw application_url)

# Connect and run init script
psql -h $RDS_ENDPOINT -U admin -d homevault -f ../../database/init.sql

