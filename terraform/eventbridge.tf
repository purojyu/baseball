# Scheduler IAM role
resource "aws_iam_role" "scheduler" {
  name = "baseball-scheduler-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "scheduler.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy" "scheduler_invoke_lambda" {
  name = "invoke-scraper-lambda"
  role = aws_iam_role.scheduler.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = "lambda:InvokeFunction"
        Resource = [
          aws_lambda_function.scraper.arn,
          aws_lambda_function.api.arn,
        ]
      }
    ]
  })
}

# Daily schedule
resource "aws_scheduler_schedule" "scraper_daily" {
  name       = "baseball-scraper-daily"
  group_name = "default"

  flexible_time_window {
    mode = "OFF"
  }

  schedule_expression          = "cron(0 2 * * ? *)"
  schedule_expression_timezone = "Asia/Tokyo"

  target {
    arn      = aws_lambda_function.scraper.arn
    role_arn = aws_iam_role.scheduler.arn
  }
}

# API Lambda warmup schedule（コールドスタート対策、10分おき）
resource "aws_scheduler_schedule" "api_warmup" {
  name       = "baseball-api-warmup"
  group_name = "default"

  flexible_time_window {
    mode = "OFF"
  }

  schedule_expression = "rate(10 minutes)"

  target {
    arn      = aws_lambda_function.api.arn
    role_arn = aws_iam_role.scheduler.arn

    # ヘルスチェックパス用のダミーAPI Gatewayイベント
    input = jsonencode({
      requestContext = {
        http = {
          method = "GET"
          path   = "/baseball/api/warmup"
        }
      }
      rawPath = "/baseball/api/warmup"
      headers = {
        "x-warmup" = "true"
      }
    })
  }
}
