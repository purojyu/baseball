resource "aws_cloudwatch_log_group" "api" {
  name              = "/aws/lambda/baseball-api"
  retention_in_days = 30
}

resource "aws_cloudwatch_log_group" "scraper" {
  name              = "/aws/lambda/baseball-scraper"
  retention_in_days = 30
}
