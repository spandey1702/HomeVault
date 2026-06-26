# 🏠 HomeVault

![Java CI](https://github.com/spandey1702/HomeVault/actions/workflows/test.yml/badge.svg)

> **Cloud-native family inventory manager** — track items, share with your household, and get notified before things expire.

Spring Boot · React · PostgreSQL · AWS ECS Fargate · Terraform · Packer · JWT · AWS SES

---

## What it does

| Feature | Details |
|---|---|
| **Family sharing** | Create a household, invite members by email, manage inventory together |
| **Inventory tracking** | Full CRUD with category, location, expiry date, brand, price, and notes |
| **Expiry alerts** | Daily 08:00 email (AWS SES) for items expiring within 7 days |
| **Family reminders** | Due-dated reminders scoped to a household or personal |
| **Live dashboard** | Stat cards — total items, expiring soon, expired, members, pending reminders |
| **Secure auth** | Real JWT (jjwt 0.12.3), BCrypt passwords, stateless Spring Security filter chain |

---

## Architecture

```
  Browser ──HTTPS──► ALB (public subnets, 2 AZs)
                          │
                    ECS Fargate (private subnets)
                    Spring Boot 3.2 · Port 8080
                          │
              ┌───────────┴────────────┐
              ▼                        ▼
        RDS PostgreSQL          Secrets Manager
        (Multi-AZ, db subnets)  JWT secret · SMTP creds
                                        │
                                  AWS SES SMTP
                                  Daily expiry +
                                  reminder emails

Supporting: ECR · CloudWatch (8 alarms) · KMS · SNS · IAM · VPC (10.0.0.0/16)
```

**Network:**
```
VPC 10.0.0.0/16
├── Public  10.0.1/2.0/24   → ALB, NAT Gateways
├── Private 10.0.10/11.0/24 → ECS Tasks
└── DB      10.0.20/21.0/24 → RDS Primary + Standby
```

Infrastructure: **44 Terraform resources** across 2 availability zones. `terraform apply` provisions everything — zero manual console steps.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.2, Spring Security, JPA/Hibernate |
| Frontend | React 18, TypeScript, Material UI v5, React Router |
| Database | PostgreSQL 15 (AWS RDS Multi-AZ) |
| Auth | JWT (jjwt 0.12.3), BCrypt |
| Email | Spring Mail → AWS SES SMTP |
| Infrastructure | Terraform 1.0+, Packer (Ubuntu 22.04 AMIs) |
| Container | Docker, AWS ECS Fargate, ECR |
| Observability | CloudWatch Logs, Dashboards, 8 Alarms, SNS ops topic |
| Secrets | AWS Secrets Manager (KMS-encrypted, auto-rotation) |

---

## Project Structure

```
homevault/
├── backend/src/main/java/com/homevault/
│   ├── security/        # JwtUtil, JwtAuthenticationFilter
│   ├── config/          # SecurityConfig, PasswordEncoderConfig
│   ├── controller/      # Auth, Items, Household, Reminders, Dashboard
│   ├── service/         # Business logic + NotificationService (@Scheduled)
│   ├── entity/          # User, Household, Item, Reminder
│   ├── repository/      # JPA repositories with custom JPQL queries
│   └── dto/             # Request / response DTOs
├── frontend/src/
│   ├── pages/           # Dashboard, Items, Household, Reminders, Login, Register
│   └── config/api.ts    # Axios base config
├── database/init.sql    # Full schema + indexes + triggers + sample data
└── infrastructure/
    ├── terraform/       # vpc, ecs, rds, alb, ecr, ses, monitoring, secrets
    └── packer/          # Backend AMI + DB AMI build configs
```

---

## Quick Start (Local)

**Prerequisites:** Java 17+, Maven, Docker, Node 18+

```bash
# 1. Start PostgreSQL
docker run -d --name homevault-db \
  -e POSTGRES_DB=homevault -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 postgres:15-alpine

# 2. Initialise schema
psql -h localhost -U postgres -d homevault -f database/init.sql

# 3. Backend — set env vars then run
export DATABASE_URL=jdbc:postgresql://localhost:5432/homevault
export PGUSER=postgres && export PGPASSWORD=postgres
export SESSION_SECRET=$(openssl rand -base64 64)   # min 32 bytes, Base64-encoded
cd backend && ./mvnw spring-boot:run

# 4. Frontend
cd frontend && npm install && npm start
# → http://localhost:3000
```

---

## AWS Deployment

```bash
cd infrastructure/terraform

# 1. Configure
cp terraform.tfvars.example terraform.tfvars
# Fill in: aws_region, db_password, session_secret, ops_email

# 2. Deploy all 44 resources
terraform init && terraform apply

# 3. Build & push Docker image
aws ecr get-login-password | docker login --username AWS --password-stdin $(terraform output -raw ecr_repository_url | cut -d/ -f1)
docker build -t homevault-backend -f backend/Dockerfile.backend .
docker tag homevault-backend:latest $(terraform output -raw ecr_repository_url):latest
docker push $(terraform output -raw ecr_repository_url):latest

# 4. Force ECS redeploy
aws ecs update-service \
  --cluster $(terraform output -raw ecs_cluster_name) \
  --service $(terraform output -raw ecs_service_name) \
  --force-new-deployment

# 5. Verify
curl $(terraform output -raw application_url)/api/health
```

---

## API Reference

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Register — returns signed JWT |
| `POST` | `/api/auth/login` | Login — returns signed JWT |
| `GET`  | `/api/auth/me` | Authenticated user profile |
| `GET`  | `/api/dashboard/stats` | Family dashboard aggregate |
| `GET`  | `/api/items` | Personal items |
| `GET`  | `/api/items/household` | All household items |
| `GET`  | `/api/items/expiring?days=7` | Items expiring within N days |
| `POST/PUT/DELETE` | `/api/items/{id}` | Create / update / delete item |
| `GET`  | `/api/households` | My households |
| `POST` | `/api/households` | Create household |
| `POST` | `/api/households/{id}/invite` | Invite member by email |
| `DELETE` | `/api/households/{id}/members/{uid}` | Remove member |
| `DELETE` | `/api/households/{id}/leave` | Leave household |
| `GET/POST` | `/api/reminders` | List / create reminders |
| `PATCH` | `/api/reminders/{id}/done` | Mark reminder as done |
| `DELETE` | `/api/reminders/{id}` | Delete reminder |

---

## Key Environment Variables

| Variable | Required | Description |
|---|---|---|
| `DATABASE_URL` | ✅ | JDBC PostgreSQL connection URL |
| `SESSION_SECRET` | ✅ | Base64-encoded JWT signing key (≥ 32 bytes) |
| `NOTIFICATIONS_ENABLED` | — | `true` to enable SES daily emails (default: `false`) |
| `SMTP_HOST / SMTP_USERNAME / SMTP_PASSWORD` | — | AWS SES SMTP credentials |

---

## License

MIT
