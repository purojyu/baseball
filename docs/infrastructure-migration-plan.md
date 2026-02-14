# 野球サイト インフラ移行計画

## Context
現在 Heroku + Amazon RDS（MySQL）で稼働中の野球対戦成績検索サイトを、AWS サーバーレス構成（CloudFront + API Gateway + Lambda + Neon PostgreSQL + S3）に移行する。コスト削減とスケーラビリティ向上が目的。

```
[CloudFront (CDN)]
       │
       ├── /baseball/api/* → API Gateway (HTTP API) → Lambda (Docker)
       │                                                    │
       │                                               Neon (PostgreSQL)
       │
       ├── /batch/*        → API Gateway → Lambda (Docker)
       │
       └── /*              → S3 (Vue.js 静的ファイル)

[EventBridge Schedule] → Lambda scraper (Docker) → Neon
```

---

## Phase 1: Docker化

### 新規作成ファイル

**`Dockerfile`**（ローカル開発 & Lambda ベース）
- マルチステージビルド（eclipse-temurin:17-jdk → 17-jre）
- `./gradlew bootJar` でビルド → `app.jar` として配置
- EXPOSE 8080

**`Dockerfile.lambda`**（Lambda Web Adapter 用）
- ベースイメージ: `amazoncorretto:17-alpine`
- Lambda Web Adapter: `public.ecr.aws/awsguru/aws-lambda-web-adapter:0.8.4` を `/opt/extensions/` にコピー
- `ENV PORT=8080`
- `CMD ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]`

**`docker-compose.yml`**（ローカル開発用: app + MySQL）
- app: Dockerfile でビルド、ポート 8080
- db: mysql:8.0、ポート 3306
- ※Phase 2 完了後に PostgreSQL 版に差し替え

### 確認方法
- `docker compose up --build` → `curl http://localhost:8080/baseball/api/getInitData`

---

## Phase 2: MySQL → PostgreSQL 移行

### 設定ファイル変更

| ファイル | 変更内容 |
|---------|---------|
| `build.gradle:34` | `mysql:mysql-connector-java:8.0.28` → `org.postgresql:postgresql` |
| `build.gradle:30` | `mybatis-spring-boot-starter` を削除（XMLマッパーなし、実質未使用） |
| `application.properties:4` | `MySQLDialect` → `PostgreSQLDialect` |
| `application-dev.properties` | `jdbc:postgresql://localhost:5432/baseball` + `org.postgresql.Driver` |
| `application-prod.properties` | `org.postgresql.Driver` に変更 |

### ネイティブクエリ変更

**BaseballGameRepository.java:27**
```
YEAR(GAME_DATE) → EXTRACT(YEAR FROM GAME_DATE)
```

**BaseballPlayerHistoryRepository.java:34,56**
```
YEAR(bph.START_DATE) → EXTRACT(YEAR FROM bph.START_DATE)
YEAR(bph.END_DATE)   → EXTRACT(YEAR FROM bph.END_DATE)
```
※ EXTRACT は double precision を返すので `:year` パラメータとの型比較に注意

**VBaseballPlayerHistoryRepository.java:24,48**
```
SUBSTRING_INDEX(vbph.PLAYER_NM, '（', 1) → SPLIT_PART(vbph.PLAYER_NM, '（', 1)
```

**VBaseballPlayerHistoryRepository.java:30,31,55,56**
```
STR_TO_DATE(CONCAT(:year, '-12-31'), '%Y-%m-%d') → TO_DATE(CONCAT(:year, '-12-31'), 'YYYY-MM-DD')
STR_TO_DATE(CONCAT(:year, '-01-01'), '%Y-%m-%d') → TO_DATE(CONCAT(:year, '-01-01'), 'YYYY-MM-DD')
```

**VAtBatGameDetailsRepository.java:32,33**
```
STR_TO_DATE(CONCAT(:selectedYear, '-01-01'), '%Y-%m-%d') → TO_DATE(CONCAT(:selectedYear, '-01-01'), 'YYYY-MM-DD')
STR_TO_DATE(CONCAT(:selectedYear, '-12-31'), '%Y-%m-%d') → TO_DATE(CONCAT(:selectedYear, '-12-31'), 'YYYY-MM-DD')
```

### 変更不要（PostgreSQL互換あり）
- `CONCAT()` - そのまま使える
- `REPLACE()` - そのまま使える
- `LIMIT 1` - そのまま使える
- `@GeneratedValue(IDENTITY)` - PostgreSQL の SERIAL と互換

### ビュー再作成
- `V_BASEBALL_PLAYER_HISTORY` - MySQL から定義取得 → PostgreSQL 構文に変換
- `V_AT_BAT_GAME_DETAILS` - 同上

### docker-compose.yml 更新
- db を `postgres:16-alpine` に差し替え
- データは `pgloader` または `pg_dump/pg_restore` で移行

### 確認方法
- Docker 上の PostgreSQL でアプリ起動
- 全APIエンドポイント疎通確認（getInitData, getPitcherList, getBatterList, matchResultSearch）
- SPLIT_PART / TO_DATE / EXTRACT の結果が正しいことを確認

---

## Phase 3: Neon セットアップ

### 作業（手動）
1. neon.tech で Free プランのプロジェクト作成（リージョン: Singapore or Tokyo）
2. `baseball` DB 作成
3. ローカル PostgreSQL からスキーマ + データを `pg_dump` → Neon に `pg_restore`
4. ビュー 2つを再作成

### application-prod.properties の最終形
```properties
spring.datasource.url=${DATABASE_URL}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.hikari.maximum-pool-size=5
spring.datasource.hikari.minimum-idle=1
spring.datasource.hikari.idle-timeout=30000
# Lambda用：ファイルログ無効化（stdout → CloudWatch Logs）
logging.file.name=
# コールドスタート対策
spring.main.lazy-initialization=true
spring.jmx.enabled=false
```

### 確認方法
- ローカルアプリから Neon 接続文字列で起動 → 全APIエンドポイント疎通
- `SELECT pg_database_size(current_database())` でストレージ確認（0.5GB制限）

---

## Phase 4: Lambda デプロイ

### Lambda 構成（分離設計）

| Lambda | 用途 | トリガー | タイムアウト | メモリ |
|--------|------|---------|-------------|--------|
| `baseball-api` | Web API | API Gateway (HTTP API) | 30秒 | 1024MB |
| `baseball-scraper` | スクレイピング | EventBridge Schedule | 15分 | 1024MB |

### 新規作成ファイル

**`src/main/java/com/example/scraper/LambdaScraperHandler.java`**
- `RequestHandler<Map, String>` を実装
- Spring Boot を WebApplicationType.NONE で起動
- NPBWebScraper / YahooPitchScraper を呼び出し

**`Dockerfile.scraper`**（スクレイピング用 Lambda Docker）
- Lambda Runtime Interface Client 用ハンドラ設定

### 変更ファイル

**SecurityConfig.java** - CORS オリジンに CloudFront ドメインを追加
```java
configuration.setAllowedOrigins(Arrays.asList(
    "https://baseball-pitcher-vs-batter.com",
    "https://www.baseball-pitcher-vs-batter.com",
    "https://dxxxxxxx.cloudfront.net"  // デプロイ後に実際のドメインに置換
));
```
※ CloudFront 同一ドメイン配下になるため CORS 自体不要になる可能性あり。`.cors(Customizer.withDefaults())` のコメントアウトを解除して有効化

**WebConfig.java** - Heroku URL を削除、CloudFront URL に更新（または SecurityConfig に統一して削除）

### AWS リソース構築手順
1. ECR リポジトリ作成: `baseball-api`, `baseball-scraper`（ap-northeast-1）
2. Docker イメージビルド（arm64）→ ECR プッシュ
3. Lambda 関数作成（コンテナイメージ、arm64、SnapStart 有効化）
4. API Gateway (HTTP API) 作成 → `$default` ルートで Lambda 統合
5. EventBridge Schedule 作成 → scraper Lambda をトリガー

### 確認方法
- API Gateway エンドポイント直接アクセスで API 疎通
- CloudWatch Logs でログ出力確認
- コールドスタート時間計測（SnapStart 有効時）

---

## Phase 5: フロントエンド（Vue.js）デプロイ

### 変更ファイル

**vue-project/vue.config.js**
- `outputDir` を `dist` に変更（S3 デプロイ用、Spring Boot static への出力停止）
- `devServer.proxy` のパスを `/baseball/api` に修正

### 新規作成ファイル

**vue-project/.env.production**
```
VUE_APP_API_BASE_URL=/baseball/api
```
※ 現時点では main.js のハードコード `/baseball/api` で動くが、将来の柔軟性のため

### AWS リソース構築
1. S3 バケット作成（パブリックアクセスブロック ON）
2. CloudFront ディストリビューション作成:
   - デフォルト: S3 オリジン（OAC 経由）
   - `/baseball/api/*`: API Gateway オリジン（キャッシュなし）
   - `/batch/*`: API Gateway オリジン（キャッシュなし）
   - カスタムエラー: 403/404 → `/index.html`（200）
3. OAC 設定
4. ACM 証明書（us-east-1）→ カスタムドメイン設定

### ビルド & デプロイ
```bash
cd vue-project && npm run build
aws s3 sync dist/ s3://バケット名/ --delete
aws cloudfront create-invalidation --distribution-id XXXXX --paths "/*"
```

### 確認方法
- CloudFront ドメインでフロント表示確認
- API 通信が `/baseball/api/*` → API Gateway → Lambda で正しくルーティングされること

---

## Phase 6: CI/CD（GitHub Actions）

### 新規作成ファイル

**`.github/workflows/deploy-api.yml`**
- トリガー: main push（`src/**`, `build.gradle`, `Dockerfile.lambda` 変更時）
- ステップ: Gradle ビルド → Docker ビルド（arm64）→ ECR プッシュ → Lambda 更新

**`.github/workflows/deploy-frontend.yml`**
- トリガー: main push（`vue-project/**` 変更時）
- ステップ: npm ci → npm build → S3 sync → CloudFront キャッシュ無効化

**`.github/workflows/deploy-scraper.yml`**
- トリガー: main push（`src/**/scraper/**` 変更時）
- ステップ: Docker ビルド → ECR プッシュ → Lambda 更新

### GitHub Secrets 設定
- `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`
- `ECR_REGISTRY`, `CLOUDFRONT_DISTRIBUTION_ID`

---

## Phase 7: 旧環境の廃止

### 削除対象ファイル
- `Procfile` - Heroku 用（削除）
- `system.properties` - Heroku 用（削除）
- `vue-project/package.json` の `heroku-postbuild` スクリプト削除

### 手順
1. 新環境で 1-2 週間の並行稼働確認
2. DNS 切り替え確認（baseball-pitcher-vs-batter.com → CloudFront）
3. RDS スナップショット取得 → インスタンス削除
4. Heroku アプリ削除

---

## 重要ファイル一覧

| ファイル | Phase |
|---------|-------|
| `build.gradle` | 2 |
| `src/main/resources/application.properties` | 2 |
| `src/main/resources/application-dev.properties` | 2 |
| `src/main/resources/application-prod.properties` | 2, 3 |
| `src/main/java/.../repository/VBaseballPlayerHistoryRepository.java` | 2 |
| `src/main/java/.../repository/VAtBatGameDetailsRepository.java` | 2 |
| `src/main/java/.../repository/BaseballGameRepository.java` | 2 |
| `src/main/java/.../repository/BaseballPlayerHistoryRepository.java` | 2 |
| `src/main/java/.../repository/BaseballPlayerRepository.java` | 2 |
| `src/main/java/.../config/SecurityConfig.java` | 4 |
| `src/main/java/.../config/WebConfig.java` | 4 |
| `vue-project/vue.config.js` | 5 |
| `vue-project/src/main.js` | 5 |

## 実装順序
Phase 1 → 2 → 3 → 4 → 5 → 6 → 7 の順に、各 Phase 完了後に動作確認してから次に進む。
