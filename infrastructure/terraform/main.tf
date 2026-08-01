terraform {
  required_version = ">= 1.10.0" # needed for use_lockfile (S3-native locking, no DynamoDB)

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  backend "s3" {
    bucket       = "homevault-tfstate-302186541954"
    key          = "terraform.tfstate"
    region       = "us-east-1"
    use_lockfile = true
    encrypt      = true
  }
}

provider "aws" {
  region = var.aws_region
}
