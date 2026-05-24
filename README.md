# Baseball — NPB 投手 vs 打者 対戦成績検索アプリ

NPB（日本プロ野球）の投手 vs 打者の対戦成績を検索・分析できる Web アプリ。
NPB 公式サイト・Yahoo Sports から自動スクレイピングしてデータを蓄積する。

## アーキテクチャ

構成図:
- 📐 [Figma — Baseball Infrastructure Architecture](https://www.figma.com/board/GzVZCM92RjY7qbFGV6fA1q/Baseball-Infrastructure-Architecture?node-id=1-2)
- 🗂️ [draw.io 構成図 (`docs/infrastructure-diagram.drawio`)](docs/infrastructure-diagram.drawio) — VS Code の Draw.io 拡張または [app.diagrams.net](https://app.diagrams.net) で開ける

```
ブラウザ (Vue.js SPA)
   │ HTTPS
   ▼
CloudFront ─── ACM (独自ドメイン baseball-pitcher-vs-batter.com)
   ├── /*           → S3 (静的フロント)
   └── /baseball/api/* → API Gateway HTTP API v2
                          ↓ AWS_PROXY
                         API Lambda (Spring Boot)
                          ↓
                       Neon PostgreSQL ← (DB URL は Secrets Manager 注入)

[EventBridge Scheduler]
  ├─ 毎10分 ──→ API Lambda warmup × 10
  ├─ 10:00 JST ─→ Scraper Lambda (NPB) ─→ NPB公式サイト → Neon
  └─ 10:30 JST ─→ Step Functions
                    ├─ Yahoo List Games Lambda (試合一覧取得)
                    └─ Map MaxConcurrency=6
                         └─ Yahoo Scrape One Game Lambda × 6並列
                              ↓
                            Yahoo Sports (一球速報) → Neon
```

## 技術スタック

### バックエンド
- Java 17 / Spring Boot 3.2.5
- Spring Data JPA / Hibernate
- PostgreSQL (Neon, Launch プラン)
- Jsoup (スクレイピング)
- Gradle

### フロントエンド
- Vue.js 2.6 / Bootstrap Vue
- Axios / Vue-Multiselect

### インフラ (AWS)
- CloudFront + ACM (独自ドメイン)
- API Gateway HTTP API v2
- Lambda × 4 (API + NPB Scraper + Yahoo List Games + Yahoo Scrape One Game)
- Step Functions (Yahoo パイプライン、Map MaxConcurrency=6)
- EventBridge Scheduler (cron 起動)
- S3 (フロント配信)
- ECR × 3 (`baseball-api`, `baseball-scraper`, `baseball-yahoo-lambda`)
- Secrets Manager (DB URL)
- IAM Role (OIDC for GitHub Actions)

### CI/CD
- GitHub Actions (OIDC、AWS access key 不要)
- Terraform (IaC)

## ディレクトリ構成

```
.
├── src/main/java/com/example/         # Spring Boot バックエンド
│   ├── baseball/                      # API ロジック (controller / service / repository / entity)
│   └── scraper/                       # NPB / Yahoo スクレイパ + Lambda Handler
├── src/main/resources/
│   ├── application.properties         # dev デフォルト
│   ├── application-prod.properties    # Lambda 用
│   └── application-dev.properties     # ローカル開発用
├── vue-project/                       # Vue.js フロントエンド
├── terraform/                         # AWS IaC
│   ├── main.tf / lambda.tf / apigateway.tf / cloudfront.tf / ...
│   └── yahoo-pitch-pipeline.tf        # Step Functions + Yahoo Lambda × 2
├── Dockerfile.lambda                  # baseball-api 用
├── Dockerfile.scraper                 # baseball-scraper (NPB) 用
├── Dockerfile.yahoo-lambda            # baseball-yahoo-lambda 用 (共通イメージ)
├── .github/workflows/                 # GitHub Actions
│   ├── deploy-api.yml
│   ├── deploy-frontend.yml
│   └── deploy-scraper.yml
└── docs/                              # 詳細設計ドキュメント
```

## ローカル開発

### 起動 (Spring Boot)

```bash
# ローカル Postgres を docker-compose で起動
docker-compose up -d

# Spring Boot を dev プロファイルで起動 (port 8090)
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 起動 (フロント)

```bash
cd vue-project && npm install && npm run serve
```

### ローカルから本番 Neon に接続して取り込む

```bash
java -jar build/libs/baseball-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=dev \
  --spring.main.allow-bean-definition-overriding=true \
  '--spring.datasource.url=jdbc:postgresql://<neon-host>/neondb?sslmode=require' \
  --spring.datasource.username=<user> \
  --spring.datasource.password=<password> \
  --server.port=8090
```

#### バッチ実行用エンドポイント

| パス | 動作 |
|---|---|
| `GET /batch/runScrape` | NPB 直近3日を取り込む |
| `GET /batch/runBatch?years=2026&months=05` | NPB 指定月を一括取り込み |
| `GET /batch/runYahooPitchScrape?from=YYYY-MM-DD&to=YYYY-MM-DD` | Yahoo 一球速報を期間指定で取り込み |

## デプロイ

### 自動デプロイ (GitHub Actions)

`main` への push で自動デプロイ。

| ワークフロー | トリガーパス | デプロイ先 |
|---|---|---|
| `deploy-api.yml` | `src/**`, `build.gradle`, `Dockerfile.lambda` | baseball-api Lambda |
| `deploy-scraper.yml` | `src/**`, `build.gradle`, `Dockerfile.scraper` | baseball-scraper Lambda |
| `deploy-frontend.yml` | `vue-project/**` | S3 + CloudFront invalidate |

### 🚨 手動デプロイは禁止

**Lambda image を手動で `docker build` → `ECR push` → `aws lambda update-function-code` するのは禁止。**

必ず `main` ブランチへの PR マージ経由で GitHub Actions にデプロイさせる。

理由:
- 手動デプロイすると git 上のソースと実行 Lambda image が乖離する（「動いてるコード」が main に存在しない状態）
- 次に他人が main を更新して CI/CD が走った瞬間、古い main コードが image に上書きされて修正が消える
- 障害時に「実際に動いてるコード」を追えなくなる

正しい手順:
```bash
# 1. ブランチを切る
git checkout -b feat/your-change

# 2. コード修正・コミット
git add . && git commit -m "..."

# 3. push & PR 作成
git push -u origin feat/your-change
gh pr create

# 4. main へマージ → GitHub Actions が自動デプロイ
```

緊急時で例外的に手動デプロイが必要な場合も、確認後すぐに同じコードで PR を上げて main を追従させる。

### Terraform 適用

```bash
cd terraform
terraform init
terraform plan
terraform apply
```

> `image_uri` は `lifecycle.ignore_changes` 指定済み。terraform は image を巻き戻さない。

## 定期実行 (EventBridge Scheduler)

| Schedule 名 | cron (Asia/Tokyo) | 起動先 |
|---|---|---|
| `baseball-scraper-daily` | `0 10 * * ? *` (毎日 10:00) | baseball-scraper Lambda (NPB scrape) |
| `baseball-yahoo-pitch-daily` | `30 10 * * ? *` (毎日 10:30) | Step Functions (Yahoo パイプライン) |
| `baseball-api-warmup-1〜10` | 毎10分 | baseball-api Lambda (warmup) |

NPB が先に動いて baseball_game / at_bat_result を作成し、その後 Yahoo が pitch_result を紐づける順序。

## Yahoo スクレイピングの設計判断

- リクエスト間隔: **7秒/req** (実証: シリアル7.5秒で HTTP 500=0)
- 並列度: Step Functions Map **MaxConcurrency=6**
  - Lambda 6並列は実証で全6個別IP発行 → Yahoo の IP単位レートリミット回避
- チェックポイント保存: 10打席ごとに `pitchResultService.saveAll` → Lambda 15分 timeout 時の部分救済
- existence-check: `at_bat_id` 単位で既登録分を除外 → retry が未処理打席のみ走る

## 関連ドキュメント

- `docs/baseball-project-architecture.md` — アーキテクチャ詳細
- `docs/coding-standards.md` — コーディング規約
- `docs/infrastructure-migration-plan.md` — インフラ移行計画
- `docs/secrets-manager-deploy-procedure.md` — Secrets Manager 運用
- `CLAUDE.md` — AI コラボレーター向けプロジェクト指示
