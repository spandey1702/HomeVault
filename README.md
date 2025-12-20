# HomeVault - Smart Home Inventory Management System

A production-ready, cloud-native inventory management system with intelligent expiration tracking and automated AWS deployment

---

## Overview

HomeVault is a full-stack cloud-native application designed to help households manage their inventory efficiently. Built with enterprise-grade architecture and deployed on AWS using infrastructure-as-code principles.

### The Problem
- Tracking expiration dates of food and medications
- Managing multi-location inventory (pantry, freezer, garage)
- Sharing household items among family members
- Preventing waste from expired products

### The Solution
- **Centralized inventory management** with category and location organization
- **Smart expiration tracking** with automated 7-day alerts
- **Multi-user households** with shared access and permissions
- **RESTful API** for potential mobile app integration
- **Cloud-native deployment** with auto-scaling and high availability

---

## Features

### Core Functionality
- **User Authentication & Authorization** - Secure JWT-based auth with BCrypt password encryption
- **Item Management** - Complete CRUD operations for inventory items
- **Advanced Categorization** - Organize by category, location, brand, and custom tags
- **Expiration Tracking** - Smart alerts for items expiring within 7 days
- **Household Sharing** - Multi-user households with member management
- **Price Tracking** - Monitor inventory value and spending patterns
- **Search & Filter** - Fast queries by name, category, location, or expiration

### Technical Features
- **Enterprise Security** - BCrypt password hashing, JWT tokens, AWS Secrets Manager
- **Real-time Monitoring** - CloudWatch dashboards, custom metrics, automated alerting
- **Auto-scaling** - ECS Fargate with automatic scaling based on load
- **CI/CD Ready** - Containerized with Docker, automated deployments
- **Comprehensive Logging** - Structured logging with CloudWatch integration
- **Infrastructure as Code** - Complete Terraform and Packer configurations
- **Automated Backups** - RDS automated backups with 7-day retention and point-in-time recovery

---

## Technology Stack

### Backend
- **Java 17** - Modern LTS version with performance improvements
- **Spring Boot 3.2.0** - Latest framework with native compilation support
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - ORM with Hibernate
- **PostgreSQL 15** - Relational database with advanced features
- **Maven** - Dependency management and build automation

### Frontend
- **React 18** - Modern UI with hooks and concurrent features
- **React Router** - Client-side routing
- **Axios** - HTTP client for API calls

### DevOps & Infrastructure
- **Docker** - Containerization
- **AWS ECS Fargate** - Serverless container orchestration
- **AWS RDS** - Managed PostgreSQL database
- **AWS ECR** - Container registry
- **AWS ALB** - Application load balancing
- **AWS VPC** - Network isolation with public/private subnets
- **Terraform** - Infrastructure provisioning
- **Packer** - AMI building and configuration
- **CloudWatch** - Monitoring and logging
- **Secrets Manager** - Secure credential storage

---

### Architecture Overview

**AWS Cloud Infrastructure:**
- **VPC (10.0.0.0/16)** - Complete network isolation across 2 availability zones
- **Public Subnets (2 AZs)** - Application Load Balancer and NAT Gateways
- **Private Subnets (2 AZs)** - ECS Fargate tasks running Spring Boot containers
- **Database Subnets (2 AZs)** - RDS PostgreSQL with Multi-AZ failover
- **Multi-AZ Deployment** - High availability across us-east-1a and us-east-1b

**Data Flow:**
1. Users connect via HTTPS/HTTP to Application Load Balancer
2. ALB distributes traffic to healthy ECS Fargate tasks
3. Spring Boot application processes requests
4. JPA/Hibernate queries PostgreSQL database
5. Responses return through ALB to users

**Security Layers:**
- Network isolation with security groups (ALB to ECS to RDS)
- Secrets Manager for encrypted credential storage
- KMS encryption for data at rest
- SSL/TLS for data in transit
- IAM roles with least-privilege access

**Supporting Services:**
- **ECR** - Docker container registry with automated scanning
- **CloudWatch** - Comprehensive logging, metrics, and alarms
- **Secrets Manager** - Secure credential management with KMS encryption
- **KMS** - Encryption key management with auto-rotation

---

## Infrastructure Highlights

### AWS Services Architecture

| Service | Purpose | Configuration |
|---------|---------|---------------|
| **VPC** | Network isolation | CIDR: 10.0.0.0/16, Multi-AZ |
| **Subnets** | Network segmentation | 2 public + 2 private across AZs |
| **Internet Gateway** | Public internet access | Single IGW for VPC |
| **NAT Gateway** | Outbound internet | 2x NAT (Multi-AZ) |
| **ECS Fargate** | Serverless containers | 0.5 vCPU, 1GB RAM |
| **RDS PostgreSQL** | Managed database | db.t3.micro, Multi-AZ, 20GB |
| **Application Load Balancer** | Traffic distribution | HTTP/HTTPS, health checks |
| **ECR** | Container registry | Private, scan on push |
| **Secrets Manager** | Credential storage | KMS encrypted |
| **CloudWatch** | Monitoring & logging | Logs, metrics, dashboards, alarms |
| **KMS** | Encryption keys | Auto-rotation enabled |
| **IAM** | Access control | Task execution roles |

### Network Architecture

```
VPC: 10.0.0.0/16
├── Public Subnets (Internet-facing)
│   ├── 10.0.1.0/24 (us-east-1a) → ALB, NAT Gateway
│   └── 10.0.2.0/24 (us-east-1b) → ALB, NAT Gateway
├── Private Subnets (Application tier)
│   ├── 10.0.10.0/24 (us-east-1a) → ECS Tasks
│   └── 10.0.11.0/24 (us-east-1b) → ECS Tasks
└── Database Subnets (Data tier, isolated)
    ├── 10.0.20.0/24 (us-east-1a) → RDS Primary
    └── 10.0.21.0/24 (us-east-1b) → RDS Standby
```

### Security Group Rules

**ALB Security Group:**
- Inbound: Port 80 (HTTP) from 0.0.0.0/0
- Inbound: Port 443 (HTTPS) from 0.0.0.0/0
- Outbound: All traffic

**ECS Tasks Security Group:**
- Inbound: Port 8080 from ALB Security Group only
- Outbound: All traffic (for package downloads, database access)

**RDS Security Group:**
- Inbound: Port 5432 from ECS Security Group only
- Outbound: All traffic
- Note: Database is completely isolated from internet

### Terraform Resources

The infrastructure consists of **44 Terraform resources**:

**Networking (15 resources):**
- 1 VPC, 1 Internet Gateway
- 2 NAT Gateways, 2 Elastic IPs
- 6 Subnets (2 public, 2 private, 2 database)
- 3 Route Tables, 6 Route Table Associations

**Compute (7 resources):**
- 1 ECS Cluster, 1 ECS Task Definition, 1 ECS Service
- 2 IAM Roles, 2 IAM Role Policy Attachments

**Database (2 resources):**
- 1 RDS Instance, 1 DB Subnet Group

**Load Balancing (3 resources):**
- 1 Application Load Balancer, 1 Target Group, 1 ALB Listener

**Security (4 resources):**
- 3 Security Groups, 1 KMS Key

**Container Registry (2 resources):**
- 1 ECR Repository, 1 ECR Lifecycle Policy

**Secrets & Monitoring (11 resources):**
- 1 Secrets Manager Secret, 1 Secret Version
- 1 CloudWatch Log Group, 1 CloudWatch Dashboard
- 8 CloudWatch Alarms, 1 SNS Topic

---

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL 15 (or use Docker)
- Node.js 18+ (for frontend)
- AWS CLI (for cloud deployment)
- Terraform 1.0+ (for infrastructure)

### Local Development Setup

**1. Clone the repository**
```bash
git clone https://github.com/yourusername/homevault.git
cd homevault
```

**2. Start PostgreSQL (using Docker)**
```bash
docker run -d \
  --name homevault-db \
  -e POSTGRES_DB=homevault \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15-alpine
```

**3. Initialize database**
```bash
psql -h localhost -U postgres -d homevault -f database/init.sql
```

**4. Configure backend**
```bash
cd backend

# Create application.properties
cat > src/main/resources/application-local.properties << EOF
spring.datasource.url=jdbc:postgresql://localhost:5432/homevault
spring.datasource.username=postgres
spring.datasource.password=postgres
app.jwt.secret=your-secret-key-change-this-in-production
EOF
```

**5. Run backend**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```
Backend will start on `http://localhost:8080`

**6. Run frontend (in a new terminal)**
```bash
cd frontend
npm install
npm start
```
Frontend will start on `http://localhost:3000`

**7. Access the application**
- Backend API: http://localhost:8080/api
- Health Check: http://localhost:8080/api/health

### Testing the API

```bash
# Register a user
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
```

---

## Deployment

### AWS Infrastructure Deployment

This project includes complete Infrastructure as Code (IaC) using Terraform and Packer for reproducible, automated deployments.

#### Prerequisites
- AWS Account with appropriate permissions
- AWS CLI configured
- Terraform 1.0+
- Docker installed
- Packer 1.8+ (optional, for custom AMIs)

#### 1. Build Infrastructure with Terraform

```bash
cd infrastructure/terraform

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
```

#### 2. Build and Push Backend Docker Image

```bash
# Login to ECR
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
```

#### 3. Deploy Application to ECS

```bash
cd infrastructure/terraform

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
```

#### 4. Verify Deployment

```bash
# Get application URL
APPLICATION_URL=$(terraform output -raw application_url)

# Check health endpoint
curl $APPLICATION_URL/api/health

# Expected response:
# {
#   "status": "UP",
#   "service": "HomeVault Backend",
#   "timestamp": "2024-12-20T10:30:00Z"
# }
```

#### 5. Initialize Database (One-time)

```bash
# Get RDS endpoint
RDS_ENDPOINT=$(terraform output -raw rds_endpoint)

# Connect and run init script
psql -h $RDS_ENDPOINT -U admin -d homevault -f ../../database/init.sql
```
### Monitoring Deployment

```bash
# View ECS service events
aws ecs describe-services \
  --cluster homevault-cluster \
  --services homevault-backend-service \
  --query 'services[0].events[0:5]'

# Check task status
aws ecs list-tasks \
  --cluster homevault-cluster \
  --service-name homevault-backend-service

# View CloudWatch logs
aws logs tail /ecs/homevault-backend --follow

# Check ALB target health
aws elbv2 describe-target-health \
  --target-group-arn $(terraform output -raw target_group_arn)
```

## API Documentation

### Authentication Endpoints

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "securePassword123"
}

Response: 200 OK
{
  "token": "temp_token_1",
  "type": "Bearer",
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "securePassword123"
}

Response: 200 OK
{
  "token": "temp_token_1",
  "type": "Bearer",
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com"
}
```

#### Get Current User
```http
GET /api/auth/me
Authorization: Bearer temp_token_1

Response: 200 OK
{
  "token": "temp_token_1",
  "type": "Bearer",
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com"
}
```

### Item Management Endpoints

#### Get All Items
```http
GET /api/items
Authorization: Bearer {token}

Response: 200 OK
[
  {
    "id": 1,
    "name": "Milk",
    "category": "Dairy",
    "location": "Refrigerator",
    "quantity": 2,
    "expiryDate": "2024-12-25",
    "isExpiring": false,
    "isExpired": false
  }
]
```

#### Create Item
```http
POST /api/items
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Bread",
  "description": "Whole wheat bread",
  "category": "Bakery",
  "location": "Pantry",
  "quantity": 1,
  "price": 3.99,
  "purchaseDate": "2024-12-15",
  "expiryDate": "2024-12-22",
  "brand": "Wonder"
}

Response: 200 OK
```

#### Update Item
```http
PUT /api/items/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "quantity": 0,
  "notes": "Consumed"
}

Response: 200 OK
```

#### Delete Item
```http
DELETE /api/items/{id}
Authorization: Bearer {token}

Response: 200 OK
{
  "message": "Item deleted successfully"
}
```

#### Get Expiring Items
```http
GET /api/items/expiring?days=7
Authorization: Bearer {token}

Response: 200 OK
[
  {
    "id": 1,
    "name": "Milk",
    "expiryDate": "2024-12-25",
    "isExpiring": true
  }
]
```

### Health Check
```http
GET /api/health

Response: 200 OK
{
  "status": "UP",
  "service": "HomeVault Backend",
  "timestamp": "2024-12-20T10:30:00Z"
}
```

---

## Monitoring & Observability

### CloudWatch Dashboards

The application includes comprehensive CloudWatch monitoring:
- **ECS Metrics**: CPU, memory utilization, task count
- **RDS Metrics**: Connections, CPU, storage, replication lag
- **ALB Metrics**: Request count, response time, 4xx/5xx errors

## Security

### Authentication & Authorization
- BCrypt password hashing with salt
- JWT tokens for stateless authentication
- CORS configuration for frontend access
- Input validation with Jakarta Bean Validation

### Infrastructure Security
- Private subnets for application and database tiers
- Security groups with least-privilege access
- AWS Secrets Manager for sensitive credentials
- KMS encryption for secrets and RDS
- VPC flow logs for network monitoring
- SSL/TLS termination at ALB (when certificate configured)
- IAM roles with minimal permissions
- ECR image scanning for vulnerabilities

### Database Security
- Isolated in private subnets
- Encrypted at rest with KMS
- Encrypted in transit with SSL
- Automated backup with point-in-time recovery
- No public accessibility

---

## Project Structure

homevault/
├── backend/
│   ├── src/main/java/com/homevault/
│   │   ├── config/           # Security, CORS configuration
│   │   ├── controller/       # REST API endpoints
│   │   ├── dto/              # Data transfer objects
│   │   ├── entity/           # JPA entities
│   │   ├── repository/       # Database repositories
│   │   ├── service/          # Business logic
│   │   └── HomeVaultApplication.java
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── Dockerfile.backend
│   └── pom.xml
│
├── frontend/                 # React application
│   ├── src/
│   ├── public/
│   ├── package.json
│   └── Dockerfile
│
├── database/
│   └── init.sql             # Database schema and seed data
│
├── infrastructure/
│   ├── terraform/           # AWS infrastructure as code
│   │   ├── main.tf          # VPC and networking
│   │   ├── security.tf      # Security groups
│   │   ├── rds.tf           # Database configuration
│   │   ├── ecs.tf           # Container orchestration
│   │   ├── alb.tf           # Load balancer
│   │   ├── ecr.tf           # Container registry
│   │   ├── monitoring.tf    # CloudWatch setup
│   │   ├── acm-secrets.tf   # Certificates and secrets
│   │   ├── variables.tf
│   │   ├── outputs.tf
│   │   ├── Makefile
│   │   └── README.md
│   │
│   └── packer/              # AMI building
│       ├── backend.pkr.hcl
│       ├── database.pkr.hcl
│       ├── scripts/
│       ├── Makefile
│       └── README.md
│
└── README.md          
