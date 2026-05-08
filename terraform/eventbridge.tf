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

  schedule_expression          = "cron(0 10 * * ? *)"
  schedule_expression_timezone = "Asia/Tokyo"

  target {
    arn      = aws_lambda_function.scraper.arn
    role_arn = aws_iam_role.scheduler.arn
  }
}

# API Lambda warmup schedules（コールドスタート対策）
# 5個のスケジュールを同時刻にcron発火させ、warmupエンドポイントが2秒sleepすることで
# 5並列コンテナをwarm維持する。これによりフロントエンドが並列API呼び出ししてもcoldにならない。
resource "aws_scheduler_schedule" "api_warmup" {
  count = 10

  name       = "baseball-api-warmup-${count.index + 1}"
  group_name = "default"

  flexible_time_window {
    mode = "OFF"
  }

  # 毎時 0,10,20,30,40,50分に5個まとめて発火
  schedule_expression = "cron(0/10 * * * ? *)"

  target {
    arn      = aws_lambda_function.api.arn
    role_arn = aws_iam_role.scheduler.arn

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
