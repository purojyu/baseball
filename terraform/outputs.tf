# GitHub Secrets用
output "iam_role_arn" {
  description = "IAM Role ARN for GitHub Actions OIDC"
  value       = aws_iam_role.github_actions.arn
}

output "s3_bucket_name" {
  description = "S3 bucket name for frontend deployment"
  value       = aws_s3_bucket.frontend.id
}

output "cloudfront_distribution_id" {
  description = "CloudFront distribution ID for cache invalidation"
  value       = aws_cloudfront_distribution.main.id
}

# DNS設定用
output "cloudfront_domain_name" {
  description = "CloudFront domain name for DNS CNAME target"
  value       = aws_cloudfront_distribution.main.domain_name
}

output "acm_validation_records" {
  description = "ACM certificate DNS validation records (add to Cloudflare)"
  value = {
    for dvo in aws_acm_certificate.main.domain_validation_options : dvo.domain_name => {
      name  = dvo.resource_record_name
      type  = dvo.resource_record_type
      value = dvo.resource_record_value
    }
  }
}

# 確認用
output "api_gateway_endpoint" {
  description = "API Gateway endpoint URL"
  value       = aws_apigatewayv2_api.main.api_endpoint
}

output "ecr_api_url" {
  description = "ECR repository URL for API"
  value       = aws_ecr_repository.api.repository_url
}

output "ecr_scraper_url" {
  description = "ECR repository URL for Scraper"
  value       = aws_ecr_repository.scraper.repository_url
}
