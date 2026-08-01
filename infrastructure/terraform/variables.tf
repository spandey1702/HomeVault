variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Project name"
  type        = string
  default     = "homevault"
}

variable "db_instance_class" {
  description = "RDS instance type"
  type        = string
  default     = "db.t3.micro"
}

variable "db_name" {
  description = "Database name"
  type        = string
  default     = "homevault"
}

variable "db_username" {
  description = "Database username"
  type        = string
  default     = "postgres"
}

variable "db_password" {
  description = "Database password"
  type        = string
  sensitive   = true
}

variable "session_secret" {
  description = "Session secret for JWT"
  type        = string
  sensitive   = true
  default     = "change-this-secret-key"
}

# Optional domain configuration
variable "domain_name" {
  description = "Domain name (optional)"
  type        = string
  default     = ""
}

variable "environment" {
  description = "Deployment environment (dev / staging / prod)"
  type        = string
  default     = "prod"
}

variable "ops_email" {
  description = "Ops email to receive SNS alarm notifications (optional)"
  type        = string
  default     = ""
}

variable "notifications_enabled" {
  description = "Enable SES email notifications from the app"
  type        = bool
  default     = false
}

variable "certificate_arn" {
  description = "Existing ACM certificate ARN to use instead of provisioning one (optional)"
  type        = string
  default     = ""
}

variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "availability_zones" {
  description = "Availability zones to spread subnets across (must match aws_region)"
  type        = list(string)
  default     = ["us-east-1a", "us-east-1b"]
}

variable "public_subnet_cidrs" {
  description = "CIDR blocks for public subnets (ALB, NAT gateways)"
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "private_subnet_cidrs" {
  description = "CIDR blocks for private subnets (ECS tasks)"
  type        = list(string)
  default     = ["10.0.10.0/24", "10.0.11.0/24"]
}

variable "database_subnet_cidrs" {
  description = "CIDR blocks for database subnets (RDS)"
  type        = list(string)
  default     = ["10.0.20.0/24", "10.0.21.0/24"]
}

variable "enable_nat_gateway" {
  description = "Provision NAT gateways for private subnet internet egress. Costs ~$0.045/hr per gateway (2 by default) plus data processing — disable for a cost-free demo where ECS tasks don't need outbound internet."
  type        = bool
  default     = true
}

variable "log_retention_days" {
  description = "CloudWatch log retention for VPC flow logs"
  type        = number
  default     = 14
}
