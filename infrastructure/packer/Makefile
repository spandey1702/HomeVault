.PHONY: help validate build-backend build-database build-all clean

# Variables
PACKER_VAR_FILE ?= variables.pkrvars.hcl

help: ## Show this help message
	@echo 'Usage: make [target]'
	@echo ''
	@echo 'Available targets:'
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  %-20s %s\n", $$1, $$2}' $(MAKEFILE_LIST)

init: ## Initialize Packer
	packer init backend.pkr.hcl
	packer init database.pkr.hcl

validate: ## Validate Packer templates
	@echo "Validating backend template..."
	packer validate -var-file=$(PACKER_VAR_FILE) backend.pkr.hcl
	@echo "Validating database template..."
	packer validate -var-file=$(PACKER_VAR_FILE) database.pkr.hcl
	@echo "All templates are valid!"

format: ## Format Packer templates
	packer fmt .

build-backend: ## Build backend AMI
	@echo "Building backend AMI..."
	packer build -var-file=$(PACKER_VAR_FILE) backend.pkr.hcl

build-database: ## Build database AMI
	@echo "Building database AMI..."
	packer build -var-file=$(PACKER_VAR_FILE) database.pkr.hcl

build-all: build-backend build-database ## Build all AMIs

inspect-backend: ## Inspect backend template
	packer inspect backend.pkr.hcl

inspect-database: ## Inspect database template
	packer inspect database.pkr.hcl

clean: ## Clean up build artifacts
	rm -f manifest.json database-manifest.json
	rm -f *.log

# AWS specific commands
list-amis: ## List AMIs created by Packer
	@echo "Backend AMIs:"
	@aws ec2 describe-images \
		--owners self \
		--filters "Name=tag:Project,Values=homevault" "Name=name,Values=homevault-backend-*" \
		--query 'Images[*].[ImageId,Name,CreationDate]' \
		--output table
	@echo ""
	@echo "Database AMIs:"
	@aws ec2 describe-images \
		--owners self \
		--filters "Name=tag:Project,Values=homevault" "Name=name,Values=homevault-database-*" \
		--query 'Images[*].[ImageId,Name,CreationDate]' \
		--output table

delete-old-amis: ## Delete AMIs older than 30 days (use with caution!)
	@echo "This will delete AMIs older than 30 days. Press Ctrl+C to cancel..."
	@sleep 5
	@./scripts/cleanup-old-amis.sh

test-backend: ## Test backend AMI by launching an instance
	@echo "Testing backend AMI..."
	@./scripts/test-ami.sh backend

test-database: ## Test database AMI by launching an instance
	@echo "Testing database AMI..."
	@./scripts/test-ami.sh database
