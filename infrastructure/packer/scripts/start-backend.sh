#!/bin/bash
set -e

# HomeVault Backend Startup Script

echo "Starting HomeVault Backend..."

# Load environment variables from SSM Parameter Store or environment
export AWS_REGION=${AWS_REGION:-us-east-1}

# Get database credentials from AWS Secrets Manager
if [ -n "$DB_SECRET_ARN" ]; then
    echo "Fetching database credentials from Secrets Manager..."
    DB_CREDS=$(aws secretsmanager get-secret-value --secret-id "$DB_SECRET_ARN" --region "$AWS_REGION" --query SecretString --output text)
    
    export DATABASE_URL=$(echo "$DB_CREDS" | jq -r '.url')
    export PGUSER=$(echo "$DB_CREDS" | jq -r '.username')
    export PGPASSWORD=$(echo "$DB_CREDS" | jq -r '.password')
fi

# Get session secret from Secrets Manager
if [ -n "$APP_SECRET_ARN" ]; then
    echo "Fetching application secrets from Secrets Manager..."
    APP_SECRETS=$(aws secretsmanager get-secret-value --secret-id "$APP_SECRET_ARN" --region "$AWS_REGION" --query SecretString --output text)
    
    export SESSION_SECRET=$(echo "$APP_SECRETS" | jq -r '.session_secret')
fi

# Login to ECR and pull the latest image
echo "Logging in to ECR..."
aws ecr get-login-password --region "$AWS_REGION" | docker login --username AWS --password-stdin "$ECR_REGISTRY"

echo "Pulling latest Docker image..."
docker pull "$ECR_REPOSITORY:latest"

# Stop and remove existing container
echo "Stopping existing container..."
docker stop homevault-backend 2>/dev/null || true
docker rm homevault-backend 2>/dev/null || true

# Start the application container
echo "Starting new container..."
docker run -d \
    --name homevault-backend \
    --restart unless-stopped \
    -p 8080:8080 \
    -e DATABASE_URL="$DATABASE_URL" \
    -e PGUSER="$PGUSER" \
    -e PGPASSWORD="$PGPASSWORD" \
    -e SESSION_SECRET="$SESSION_SECRET" \
    -e AWS_REGION="$AWS_REGION" \
    --log-driver=awslogs \
    --log-opt awslogs-region="$AWS_REGION" \
    --log-opt awslogs-group="/homevault/backend" \
    --log-opt awslogs-stream="$(hostname)" \
    "$ECR_REPOSITORY:latest"

echo "HomeVault Backend started successfully!"

# Health check
sleep 10
if curl -f http://localhost:8080/api/health; then
    echo "Health check passed!"
else
    echo "Health check failed!"
    exit 1
fi
