HomeVault is a production-ready inventory management system designed to help households track items, manage expiration dates, and share inventory across family members. Built with modern cloud-native architecture, it demonstrates enterprise-level software engineering practices including microservices, infrastructure as code, and CI/CD automation.

Key Features:
Smart Inventory Tracking
    Add, edit, and categorize household items
    Track purchase dates, expiration dates, and quantities
    Organize by location and category
Expiration Management
    Automatic expiration date monitoring
    Visual indicators for expiring items (7-day warning)
    Expired item notifications
Multi-User Support
    Household-based sharing
    Role-based access control (planned)
    Individual and shared inventories
Cloud-Native Architecture
    Containerized microservices on AWS ECS
    Auto-scaling based on demand
    High availability across multiple AZs
Modern Tech Stack
    Reactive frontend with React 19
    RESTful API with Spring Boot 3.2
    PostgreSQL database with automated backups

Technology Stack-- 
  Framework: React 19.1 with TypeScript
  Framework: Spring Boot 3.2.0
  Language: Java 17
  Database: PostgreSQL 15
  ORM: Hibernate/JPA
  Security: Spring Security with JWT
  API: RESTful with JSON
  Build Tool: Maven

Infrastructure
  Cloud Provider: AWS
  Database: RDS PostgreSQL
  Load Balancer: Application Load Balancer
  IaC: Terraform + Packer
  Networking: VPC with public/private subnets
  Storage: S3 
DevOps & Monitoring
  CI/CD: GitHub Actions
  Logging: CloudWatch Logs
  Monitoring: CloudWatch Metrics + Dashboards
  Alerting: SNS + CloudWatch Alarms
  Image Building: Packer for custom AMIs
  Secrets: AWS Secrets Managerabase
  Git for version control
