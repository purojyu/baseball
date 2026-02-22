# ============================================================
#  Baseball App - Makefile
# ============================================================

# バージョン（docker-compose.yml の build args で参照）
export JAVA_VERSION  ?= 17
export POSTGRES_VERSION ?= 16

# ------------------------------------------------------------
#  セットアップ・フルビルド
# ------------------------------------------------------------

.PHONY: setup setup-fe setup-be build-all

setup: setup-be setup-fe ## 初回セットアップ（BE + FE 両方）
	@echo "\n✅ セットアップ完了！ 'make up' でDocker起動できるよ"

setup-be: ## バックエンド初回セットアップ（依存解決）
	./gradlew dependencies --no-daemon

setup-fe: ## フロントエンド初回セットアップ（npm install）
	cd vue-project && npm install

build-all: build fe-build ## フルビルド（BE + FE 両方）
	@echo "\n✅ フルビルド完了"

# ------------------------------------------------------------
#  Docker（ローカル開発）
# ------------------------------------------------------------

.PHONY: up down restart logs ps

up: ## Docker起動（アプリ + DB）
	docker compose up -d --build

down: ## Docker停止 + コンテナ削除
	docker compose down

restart: ## Docker再起動
	docker compose down && docker compose up -d --build

logs: ## Dockerログ（フォロー）
	docker compose logs -f

logs-app: ## アプリログのみ
	docker compose logs -f app

logs-db: ## DBログのみ
	docker compose logs -f db

ps: ## コンテナ状態確認
	docker compose ps

# ------------------------------------------------------------
#  Docker（Lambda イメージビルド）
# ------------------------------------------------------------

.PHONY: build-lambda-api build-lambda-scraper build-lambda-all

build-lambda-api: ## Lambda API用イメージをビルド
	docker build --build-arg JAVA_VERSION=$(JAVA_VERSION) -f Dockerfile.lambda -t baseball-api-lambda .

build-lambda-scraper: ## Lambdaスクレイパー用イメージをビルド
	docker build --build-arg JAVA_VERSION=$(JAVA_VERSION) -f Dockerfile.scraper -t baseball-scraper-lambda .

build-lambda-all: build-lambda-api build-lambda-scraper ## Lambda全イメージをビルド

# ------------------------------------------------------------
#  バックエンド（Gradle）
# ------------------------------------------------------------

.PHONY: build run test clean-backend

build: ## Gradleビルド（jar作成）
	./gradlew bootJar --no-daemon

run: ## Spring Bootをローカル起動
	./gradlew bootRun --no-daemon

test: ## テスト実行
	./gradlew test --no-daemon

clean-backend: ## Gradleビルド成果物を削除
	./gradlew clean --no-daemon

# ------------------------------------------------------------
#  フロントエンド（Vue.js）
# ------------------------------------------------------------

.PHONY: fe-dev fe-build fe-lint

fe-dev: ## Vue.js開発サーバー起動
	cd vue-project && npm run dev

fe-build: ## Vue.jsプロダクションビルド
	cd vue-project && npm run build

fe-lint: ## ESLint実行
	cd vue-project && npm run lint

# ------------------------------------------------------------
#  ローカル開発（Docker不使用）
# ------------------------------------------------------------

.PHONY: dev dev-be dev-fe

dev: ## BE + FE 同時起動（Docker不使用・バックグラウンド）
	@echo "🚀 バックエンド起動中..."
	./gradlew bootRun --no-daemon &
	@echo "🚀 フロントエンド起動中..."
	cd vue-project && npm run dev

# ------------------------------------------------------------
#  DB操作
# ------------------------------------------------------------

.PHONY: db-connect db-reset

db-connect: ## ローカルDBに接続
	docker compose exec db psql -U postgres -d baseball

db-reset: ## ローカルDBをリセット（データ全削除）
	docker compose down -v && docker compose up -d db

# ------------------------------------------------------------
#  ユーティリティ
# ------------------------------------------------------------

.PHONY: clean help

clean: clean-backend ## 全ビルド成果物を削除
	cd vue-project && npm run clean

help: ## このヘルプを表示
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-22s\033[0m %s\n", $$1, $$2}'

.DEFAULT_GOAL := help
