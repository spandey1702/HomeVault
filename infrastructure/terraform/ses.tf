# ── AWS SES — Transactional email for expiry & reminder notifications ─────────

# Verified identity (domain-level verification — works with any subdomain address)
resource "aws_ses_domain_identity" "homevault" {
  count  = var.domain_name != "" ? 1 : 0
  domain = var.domain_name
}

resource "aws_ses_domain_dkim" "homevault" {
  count  = var.domain_name != "" ? 1 : 0
  domain = aws_ses_domain_identity.homevault[0].domain
}

# Route 53 DKIM records (only created when domain_name is set)
resource "aws_route53_record" "ses_dkim" {
  count   = var.domain_name != "" ? 3 : 0
  zone_id = data.aws_route53_zone.main[0].zone_id
  name    = "${aws_ses_domain_dkim.homevault[0].dkim_tokens[count.index]}._domainkey"
  type    = "CNAME"
  ttl     = 600
  records = ["${aws_ses_domain_dkim.homevault[0].dkim_tokens[count.index]}.dkim.amazonses.com"]
}

# SES SMTP credentials (IAM user + access key → stored in Secrets Manager)
resource "aws_iam_user" "ses_smtp" {
  name = "${var.project_name}-ses-smtp-user"
  tags = { Name = "${var.project_name}-ses-smtp-user" }
}

resource "aws_iam_user_policy" "ses_smtp_send" {
  name = "${var.project_name}-ses-send-policy"
  user = aws_iam_user.ses_smtp.name

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = ["ses:SendEmail", "ses:SendRawEmail"]
      Resource = "*"
    }]
  })
}

resource "aws_iam_access_key" "ses_smtp" {
  user = aws_iam_user.ses_smtp.name
}

# Store SMTP credentials in Secrets Manager (backend reads them at startup)
resource "aws_secretsmanager_secret" "smtp_credentials" {
  name        = "${var.project_name}/smtp-credentials"
  description = "SES SMTP credentials for HomeVault notification service"
  kms_key_id  = aws_kms_key.main.arn

  tags = { Name = "${var.project_name}-smtp-credentials" }
}

resource "aws_secretsmanager_secret_version" "smtp_credentials" {
  secret_id = aws_secretsmanager_secret.smtp_credentials.id
  secret_string = jsonencode({
    smtp_username = aws_iam_access_key.ses_smtp.id
    # SES SMTP password is derived from the secret access key — see AWS docs.
    # Store the derived password here after running `aws sesv2 create-email-identity`.
    smtp_password = aws_iam_access_key.ses_smtp.ses_smtp_password_v4
    smtp_host     = "email-smtp.${var.aws_region}.amazonaws.com"
    smtp_port     = "587"
    from_address  = "noreply@${var.domain_name != "" ? var.domain_name : "homevault.app"}"
  })
}

# ── SNS topic — used for ops alerts (expiry alarm, etc.) ─────────────────────

resource "aws_sns_topic" "notifications" {
  name = "${var.project_name}-notifications"
  tags = { Name = "${var.project_name}-notifications" }
}

resource "aws_sns_topic_subscription" "ops_email" {
  count     = var.ops_email != "" ? 1 : 0
  topic_arn = aws_sns_topic.notifications.arn
  protocol  = "email"
  endpoint  = var.ops_email
}

# CloudWatch alarm → SNS when expiry check fails (ECS task exits unexpectedly)
resource "aws_cloudwatch_metric_alarm" "ecs_task_stopped" {
  alarm_name          = "${var.project_name}-ecs-task-stopped"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "StoppedTaskCount"
  namespace           = "ECS/ContainerInsights"
  period              = 300
  statistic           = "Sum"
  threshold           = 0
  alarm_description   = "Fires when an ECS task stops unexpectedly"
  alarm_actions       = [aws_sns_topic.notifications.arn]

  dimensions = {
    ClusterName = aws_ecs_cluster.main.name
    ServiceName = aws_ecs_service.backend.name
  }
}
