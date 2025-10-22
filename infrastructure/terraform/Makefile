.PHONY: help init plan apply destroy validate fmt clean login-ecr build-backend push-backend deploy-backend

# Variables
AWS_REGION ?= us-east-1
ENVIRONMENT ?= dev
PROJECT_NAME ?= homevault

help: ## Show this help message
	@echo 'Usage: make [target]'
	@echo ''
	@echo 'Available targets:'
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  %-20s %s\n", $$1, $$2}' $(MAKEFILE_LIST)

init: ## Initialize Terraform
	terraform init

validate: ## Validate Terraform configuration
	terraform validate

fmt: ## Format Terraform files
	terraform fmt -recursive

plan: ## Create Terraform execution plan
	terraform plan -out=tfplan

apply: ## Apply Terraform configuration
	terraform apply tfplan

apply-auto: ## Apply Terraform configuration without approval
	terraform apply -auto-approve

destroy: ## Destroy Terraform-managed infrastructure
	terraform destroy

destroy-auto: ## Destroy infrastructure without approval (dangerous!)
	terraform destroy -auto-approve

output: ## Show Terraform outputs
	terraform output

state-list: ## List resources in state
	terraform state list

clean: ## Remove temporary files
	rm -f tfplan
	rm -rf .terraform

refresh: ## Refresh Terraform state
	terraform refresh

show: ## Show current state
	terraform show

graph: ## Generate visual dependency graph
	terraform graph | dot -Tpng > graph.png

# Docker and Deployment Commands

login-ecr: ## Login to AWS ECR
	@aws ecr get-login-password --region $(AWS_REGION) | docker login --username AWS --password-stdin $$(terraform output -raw ecr_backend_repository_url | cut -d'/' -f1)

build-backend: ## Build backend Docker image
	@cd .. && docker build -t $(PROJECT_NAME)-backend:latest -f backend/Dockerfile.backend .

build-frontend: ## Build frontend Docker image
	@cd .. && docker build -t $(PROJECT_NAME)-frontend:latest -f frontend/Dockerfile .

tag-backend: ## Tag backend image for ECR
	@docker tag $(PROJECT_NAME)-backend:latest $$(terraform output -raw ecr_backend_repository_url):latest

tag-frontend: ## Tag frontend image for ECR
	@docker tag $(PROJECT_NAME)-frontend:latest $$(terraform output -raw ecr_frontend_repository_url):latest

push-backend: login-ecr tag-backend ## Push backend image to ECR
	@docker push $$(terraform output -raw ecr_backend_repository_url):latest

push-frontend: login-ecr tag-frontend ## Push frontend image to ECR
	@docker push $$(terraform output -raw ecr_frontend_repository_url):latest

deploy-backend: ## Deploy backend to ECS
	@aws ecs update-service \
		--cluster $$(terraform output -raw ecs_cluster_name) \
		--service $$(terraform output -raw backend_service_name) \
		--force-new-deployment \
		--region $(AWS_REGION)

full-deploy: build-backend build-frontend push-backend push-frontend deploy-backend ## Build, push, and deploy everything

# Monitoring Commands

logs-backend: ## Tail backend logs
	@aws logs tail $$(terraform output -raw cloudwatch_log_group_backend) --follow

logs-frontend: ## Tail frontend logs
	@aws logs tail $$(terraform output -raw cloudwatch_log_group_frontend) --follow

describe-backend-service: ## Describe backend ECS service
	@aws ecs describe-services \
		--cluster $$(terraform output -raw ecs_cluster_name) \
		--services $$(terraform output -raw backend_service_name) \
		--region $(AWS_REGION)

list-tasks: ## List running ECS tasks
	@aws ecs list-tasks \
		--cluster $$(terraform output -raw ecs_cluster_name) \
		--region $(AWS_REGION)

# Cost Estimation
cost-estimate: ## Estimate infrastructure costs (requires infracost)
	@which infracost > /dev/null || (echo "Please install infracost: https://www.infracost.io/docs/" && exit 1)
	@infracost breakdown --path .

# Security Scanning
security-scan: ## Run security scan (requires tfsec)
	@which tfsec > /dev/null || (echo "Please install tfsec: https://github.com/aquasecurity/tfsec" && exit 1)
	@tfsec .

# Workspace Management
workspace-list: ## List Terraform workspaces
	terraform workspace list

workspace-new: ## Create new workspace (usage: make workspace-new WORKSPACE=prod)
	terraform workspace new $(WORKSPACE)

workspace-select: ## Select workspace (usage: make workspace-select WORKSPACE=prod)
	terraform workspace select $(WORKSPACE)

# Backup and Recovery
backup-state: ## Backup Terraform state
	@mkdir -p backups
	@cp terraform.tfstate backups/terraform.tfstate.$$(date +%Y%m%d_%H%M%S)

# Documentation
docs: ## Generate documentation (requires terraform-docs)
	@which terraform-docs > /dev/null || (echo "Please install terraform-docs: https://github.com/terraform-docs/terraform-docs" && exit 1)
	@terraform-docs markdown table . > TERRAFORM_DOCS.md

# Complete Setup
setup: init validate fmt plan ## Complete setup (init, validate, format, plan)

# Complete Teardown
teardown: backup-state destroy clean ## Complete teardown (backup, destroy, clean)
