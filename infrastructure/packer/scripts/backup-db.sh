#!/bin/bash
set -e

# Database Backup Script for HomeVault

BACKUP_DIR="/var/backups/postgresql"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
DATABASE="homevault"
S3_BUCKET="${BACKUP_S3_BUCKET:-homevault-db-backups}"
AWS_REGION="${AWS_REGION:-us-east-1}"

echo "Starting database backup at $TIMESTAMP"

# Create backup directory if it doesn't exist
mkdir -p "$BACKUP_DIR"

# Backup filename
BACKUP_FILE="$BACKUP_DIR/${DATABASE}_${TIMESTAMP}.sql.gz"

# Perform backup
echo "Creating backup: $BACKUP_FILE"
sudo -u postgres pg_dump "$DATABASE" | gzip > "$BACKUP_FILE"

# Upload to S3
echo "Uploading backup to S3..."
aws s3 cp "$BACKUP_FILE" "s3://$S3_BUCKET/backups/" --region "$AWS_REGION"

# Keep only last 7 days of local backups
echo "Cleaning up old local backups..."
find "$BACKUP_DIR" -name "*.sql.gz" -mtime +7 -delete

echo "Backup completed successfully!"

# Send CloudWatch metric
aws cloudwatch put-metric-data \
    --namespace "HomeVault/Database" \
    --metric-name "BackupSuccess" \
    --value 1 \
    --region "$AWS_REGION"
