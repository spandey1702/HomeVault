#!/bin/bash
set -euo pipefail

# HomeVault Backend Startup Script
# Fetches all secrets from AWS Secrets Manager before launching the container.

echo "Starting HomeVault Backend..."
export AWS_REGION="${AWS_REGION:-us-east-1}"

# ── DB credentials ─────────────────────────────────────────────────────────────
if [ -n "${DB_SECRET_ARN:-}" ]; then
    echo "Fetching database credentials from Secrets Manager..."
    DB_CREDS=$(aws secretsmanager get-secret-value \
        --secret-id "$DB_SECRET_ARN" --region "$AWS_REGION" \
        --query SecretString --output text)
    export DATABASE_URL=$(echo "$DB_CREDS" | jq -r '.url')
    export PGUSER=$(echo "$DB_CREDS" | jq -r '.username')
    export PGPASSWORD=$(echo "$DB_CREDS" | jq -r '.password')
fi

# ── App secrets (JWT + notifications) ─────────────────────────────────────────
if [ -n "${APP_SECRET_ARN:-}" ]; then
    echo "Fetching application secrets from Secrets Manager..."
    APP_SECRETS=$(aws secretsmanager get-secret-value \
        --secret-id "$APP_SECRET_ARN" --region "$AWS_REGION" \
        --query SecretString --output text)
    export SESSION_SECRET=$(echo "$APP_SECRETS" | jq -r '.session_secret')
fi

# ── SMTP credentials (SES) ────────────────────────────────────────────────────
if [ -n "${SMTP_SECRET_ARN:-}" ]; then
    echo "Fetching SMTP credentials from Secrets Manager..."
    SMTP_CREDS=$(aws secretsmanager get-secret-value \
        --secret-id "$SMTP_SECRET_ARN" --region "$AWS_REGION" \
        --query SecretString --output text)
    export SMTP_USERNAME=$(echo "$SMTP_CREDS" | jq -r '.smtp_username')
    export SMTP_PASSWORD=$(echo "$SMTP_CREDS" | jq -r '.smtp_password')
    export SMTP_HOST=$(echo "$SMTP_CREDS"     | jq -r '.smtp_host')
    export SMTP_PORT=$(echo "$SMTP_CREDS"     | jq -r '.smtp_port')
    export NOTIFICATIONS_FROM=$(echo "$SMTP_CREDS" | jq -r '.from_address')
fi

# ── Pull latest image from ECR ────────────────────────────────────────────────
echo "Logging in to ECR..."
aws ecr get-login-password --region "$AWS_REGION" \
    | docker login --username AWS --password-stdin "$ECR_REGISTRY"

echo "Pulling latest Docker image..."
docker pull "${ECR_REPOSITORY}:latest"

# ── Restart container ─────────────────────────────────────────────────────────
echo "Stopping existing container (if any)..."
docker stop homevault-backend 2>/dev/null || true
docker rm   homevault-backend 2>/dev/null || true

echo "Starting new container..."
docker run -d \
    --name homevault-backend \
    --restart unless-stopped \
    -p 8080:8080 \
    -e DATABASE_URL="$DATABASE_URL" \
    -e PGUSER="$PGUSER" \
    -e PGPASSWORD="$PGPASSWORD" \
    -e SESSION_SECRET="$SESSION_SECRET" \
    -e NOTIFICATIONS_ENABLED="${NOTIFICATIONS_ENABLED:-false}" \
    -e SMTP_HOST="${SMTP_HOST:-}" \
    -e SMTP_PORT="${SMTP_PORT:-587}" \
    -e SMTP_USERNAME="${SMTP_USERNAME:-}" \
    -e SMTP_PASSWORD="${SMTP_PASSWORD:-}" \
    -e NOTIFICATIONS_FROM="${NOTIFICATIONS_FROM:-noreply@homevault.app}" \
    -e AWS_REGION="$AWS_REGION" \
    --log-driver=awslogs \
    --log-opt awslogs-region="$AWS_REGION" \
    --log-opt awslogs-group="/homevault/backend" \
    --log-opt awslogs-stream="$(hostname)" \
    "${ECR_REPOSITORY}:latest"

echo "HomeVault Backend started."

# ── Health check ──────────────────────────────────────────────────────────────
sleep 15
MAX=5
for i in $(seq 1 $MAX); do
    if curl -sf http://localhost:8080/api/health; then
        echo "Health check passed (attempt $i)."
        exit 0
    fi
    echo "Health check attempt $i/$MAX failed — retrying in 5s..."
    sleep 5
done
echo "Health check failed after $MAX attempts!"
exit 1
