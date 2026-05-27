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
