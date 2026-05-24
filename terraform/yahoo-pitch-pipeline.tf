# ============================================================
# Yahoo Pitch Pipeline (Step Functions + Lambda × 2)
# 毎日 10:30 JST に起動し、前日試合分の Yahoo 一球速報を取り込む。
# ============================================================

# ----- ECR (Yahoo Lambda 共通イメージ) -----
resource "aws_ecr_repository" "yahoo_lambda" {
  name                 = "baseball-yahoo-lambda"
  image_tag_mutability = "MUTABLE"
  force_delete         = true

  image_scanning_configuration {
    scan_on_push = true
  }
}

resource "aws_ecr_lifecycle_policy" "yahoo_lambda" {
  repository = aws_ecr_repository.yahoo_lambda.name
  policy = jsonencode({
    rules = [{
      rulePriority = 1
      description  = "Keep last 5 images"
      selection = {
        tagStatus   = "any"
        countType   = "imageCountMoreThan"
        countNumber = 5
      }
      action = { type = "expire" }
    }]
  })
}

# ----- CloudWatch Log Groups -----
resource "aws_cloudwatch_log_group" "yahoo_list_games" {
  name              = "/aws/lambda/baseball-yahoo-list-games"
  retention_in_days = 14
}

resource "aws_cloudwatch_log_group" "yahoo_scrape_one_game" {
  name              = "/aws/lambda/baseball-yahoo-scrape-one-game"
  retention_in_days = 14
}

resource "aws_cloudwatch_log_group" "yahoo_state_machine" {
  name              = "/aws/vendedlogs/states/baseball-yahoo-pitch-pipeline"
  retention_in_days = 14
}

# ----- Lambda1: list-target-games -----
resource "aws_lambda_function" "yahoo_list_games" {
  function_name = "baseball-yahoo-list-games"
  role          = aws_iam_role.lambda_execution.arn
  package_type  = "Image"
  image_uri     = "${aws_ecr_repository.yahoo_lambda.repository_url}:latest"
  architectures = ["arm64"]
  memory_size   = 1024
  timeout       = 300

  image_config {
    command = ["com.example.scraper.YahooListGamesHandler::handleRequest"]
  }

  environment {
    variables = {
      DATABASE_URL_SECRET_NAME = "baseball/database-url"
      SPRING_PROFILES_ACTIVE   = "prod"
    }
  }

  lifecycle {
    ignore_changes = [image_uri]
  }

  depends_on = [
    aws_cloudwatch_log_group.yahoo_list_games,
    aws_iam_role_policy_attachment.lambda_basic_execution,
    aws_iam_role_policy.lambda_secrets_manager,
  ]
}

# ----- Lambda2: scrape-one-game -----
resource "aws_lambda_function" "yahoo_scrape_one_game" {
  function_name = "baseball-yahoo-scrape-one-game"
  role          = aws_iam_role.lambda_execution.arn
  package_type  = "Image"
  image_uri     = "${aws_ecr_repository.yahoo_lambda.repository_url}:latest"
  architectures = ["arm64"]
  memory_size   = 1024
  timeout       = 900 # 15分 (1試合: 60打席 × 7.5秒 = 7.5min 余裕)

  image_config {
    command = ["com.example.scraper.YahooScrapeOneGameHandler::handleRequest"]
  }

  environment {
    variables = {
      DATABASE_URL_SECRET_NAME = "baseball/database-url"
      SPRING_PROFILES_ACTIVE   = "prod"
    }
  }

  lifecycle {
    ignore_changes = [image_uri]
  }

  depends_on = [
    aws_cloudwatch_log_group.yahoo_scrape_one_game,
    aws_iam_role_policy_attachment.lambda_basic_execution,
    aws_iam_role_policy.lambda_secrets_manager,
  ]
}

# ----- IAM Role for Step Functions -----
resource "aws_iam_role" "yahoo_state_machine" {
  name = "baseball-yahoo-state-machine-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "states.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy" "yahoo_state_machine_invoke_lambda" {
  name = "invoke-yahoo-lambdas"
  role = aws_iam_role.yahoo_state_machine.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Action = "lambda:InvokeFunction"
      Resource = [
        aws_lambda_function.yahoo_list_games.arn,
        "${aws_lambda_function.yahoo_list_games.arn}:*",
        aws_lambda_function.yahoo_scrape_one_game.arn,
        "${aws_lambda_function.yahoo_scrape_one_game.arn}:*",
      ]
      }, {
      # CloudWatch Logs (Logging)
      Effect = "Allow"
      Action = [
        "logs:CreateLogDelivery",
        "logs:GetLogDelivery",
        "logs:UpdateLogDelivery",
        "logs:DeleteLogDelivery",
        "logs:ListLogDeliveries",
        "logs:PutResourcePolicy",
        "logs:DescribeResourcePolicies",
        "logs:DescribeLogGroups",
      ]
      Resource = "*"
    }]
  })
}

# ----- Step Functions State Machine -----
resource "aws_sfn_state_machine" "yahoo_pitch_pipeline" {
  name     = "baseball-yahoo-pitch-pipeline"
  role_arn = aws_iam_role.yahoo_state_machine.arn

  definition = jsonencode({
    Comment = "Yahoo pitch_result 取り込みパイプライン (前日試合を1試合ずつLambdaで並列取得)"
    StartAt = "ListTargetGames"
    States = {
      ListTargetGames = {
        Type     = "Task"
        Resource = "arn:aws:states:::lambda:invoke"
        Parameters = {
          FunctionName = aws_lambda_function.yahoo_list_games.arn
          "Payload.$"  = "$"
        }
        ResultSelector = {
          "games.$" = "$.Payload.games"
          "date.$"  = "$.Payload.date"
        }
        Retry = [{
          ErrorEquals     = ["States.ALL"]
          IntervalSeconds = 30
          MaxAttempts     = 2
          BackoffRate     = 2.0
        }]
        Next = "CheckGameCount"
      }
      CheckGameCount = {
        Type = "Choice"
        Choices = [{
          Variable  = "$.games[0]"
          IsPresent = true
          Next      = "ProcessGames"
        }]
        Default = "NoGames"
      }
      NoGames = {
        Type   = "Pass"
        Result = "No games to scrape"
        End    = true
      }
      ProcessGames = {
        Type           = "Map"
        ItemsPath      = "$.games"
        MaxConcurrency = 6
        Iterator = {
          StartAt = "ScrapeOneGame"
          States = {
            ScrapeOneGame = {
              Type     = "Task"
              Resource = "arn:aws:states:::lambda:invoke"
              Parameters = {
                FunctionName = aws_lambda_function.yahoo_scrape_one_game.arn
                "Payload.$"  = "$"
              }
              Retry = [{
                ErrorEquals     = ["States.ALL"]
                IntervalSeconds = 60
                MaxAttempts     = 3
                BackoffRate     = 2.0
              }]
              Catch = [{
                ErrorEquals = ["States.ALL"]
                Next        = "ScrapeOneGameFailed"
              }]
              End = true
            }
            ScrapeOneGameFailed = {
              Type   = "Pass"
              Result = "failed"
              End    = true
            }
          }
        }
        End = true
      }
    }
  })

  logging_configuration {
    log_destination        = "${aws_cloudwatch_log_group.yahoo_state_machine.arn}:*"
    include_execution_data = true
    level                  = "ALL"
  }

  depends_on = [
    aws_iam_role_policy.yahoo_state_machine_invoke_lambda,
  ]
}

# ----- EventBridge Scheduler: 10:30 JST 毎日 -----
resource "aws_iam_role_policy" "scheduler_invoke_state_machine" {
  name = "invoke-yahoo-state-machine"
  role = aws_iam_role.scheduler.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = "states:StartExecution"
      Resource = aws_sfn_state_machine.yahoo_pitch_pipeline.arn
    }]
  })
}

resource "aws_scheduler_schedule" "yahoo_pitch_daily" {
  name       = "baseball-yahoo-pitch-daily"
  group_name = "default"

  flexible_time_window {
    mode = "OFF"
  }

  schedule_expression          = "cron(30 10 * * ? *)" # 10:30 JST
  schedule_expression_timezone = "Asia/Tokyo"

  target {
    arn      = aws_sfn_state_machine.yahoo_pitch_pipeline.arn
    role_arn = aws_iam_role.scheduler.arn

    # date 未指定 → Lambda 内で前日(JST)を自動算出
    input = jsonencode({})
  }
}
