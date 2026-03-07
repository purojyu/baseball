# IAM execution role
resource "aws_iam_role" "lambda_execution" {
  name = "baseball-lambda-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "lambda_basic_execution" {
  role       = aws_iam_role.lambda_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_iam_role_policy" "lambda_secrets_manager" {
  name = "baseball-lambda-secrets-manager"
  role = aws_iam_role.lambda_execution.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect   = "Allow"
        Action   = "secretsmanager:GetSecretValue"
        Resource = aws_secretsmanager_secret.database_url.arn
      }
    ]
  })
}

# API Lambda
resource "aws_lambda_function" "api" {
  function_name = "baseball-api"
  role          = aws_iam_role.lambda_execution.arn
  package_type  = "Image"
  image_uri     = "${aws_ecr_repository.api.repository_url}:latest"
  architectures = ["arm64"]
  memory_size   = var.api_lambda_memory
  timeout       = var.api_lambda_timeout

  environment {
    variables = {
      DATABASE_URL_SECRET_NAME = "baseball/database-url"
      PORT                     = "8080"
    }
  }

  lifecycle {
    ignore_changes = [image_uri]
  }

  depends_on = [
    aws_cloudwatch_log_group.api,
    aws_iam_role_policy_attachment.lambda_basic_execution,
    aws_iam_role_policy.lambda_secrets_manager,
  ]
}

# Scraper Lambda
resource "aws_lambda_function" "scraper" {
  function_name = "baseball-scraper"
  role          = aws_iam_role.lambda_execution.arn
  package_type  = "Image"
  image_uri     = "${aws_ecr_repository.scraper.repository_url}:latest"
  architectures = ["arm64"]
  memory_size   = var.scraper_lambda_memory
  timeout       = var.scraper_lambda_timeout

  environment {
    variables = {
      DATABASE_URL_SECRET_NAME = "baseball/database-url"
    }
  }

  lifecycle {
    ignore_changes = [image_uri]
  }

  depends_on = [
    aws_cloudwatch_log_group.scraper,
    aws_iam_role_policy_attachment.lambda_basic_execution,
    aws_iam_role_policy.lambda_secrets_manager,
  ]
}
