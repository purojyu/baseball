variable "database_url" {
  description = "Neon PostgreSQL connection string"
  type        = string
  sensitive   = true
}

variable "domain_name" {
  description = "Domain name for the application"
  type        = string
  default     = "baseball-pitcher-vs-batter.com"
}

variable "github_repo" {
  description = "GitHub repository name (owner/repo) for OIDC trust policy"
  type        = string
  default     = "purojyu/baseball"
}

variable "api_lambda_memory" {
  description = "Memory size for API Lambda (MB)"
  type        = number
  default     = 3008
}

variable "api_lambda_timeout" {
  description = "Timeout for API Lambda (seconds)"
  type        = number
  default     = 60
}

variable "scraper_lambda_memory" {
  description = "Memory size for Scraper Lambda (MB)"
  type        = number
  default     = 1024
}

variable "scraper_lambda_timeout" {
  description = "Timeout for Scraper Lambda (seconds)"
  type        = number
  default     = 900
}
