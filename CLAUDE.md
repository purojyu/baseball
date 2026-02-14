# Claude設定

## アプリ概要
- NPB（日本プロ野球）の投手 vs 打者の対戦成績を検索・分析するWebアプリ
- NPB公式サイト・Yahoo Sportsからデータを自動スクレイピングして蓄積

## 技術スタック
### バックエンド
- Java 17 / Spring Boot 3.2.5
- Spring Data JPA
- PostgreSQL（Neon）
- Jsoup（スクレイピング）
- Gradle

### フロントエンド
- Vue.js 2.6 / Bootstrap Vue
- Axios / Vue-Multiselect

### インフラ
- AWS（CloudFront + API Gateway + Lambda + S3）
- Neon PostgreSQL（データベース）
- GitHub Actions（CI/CD）

## ドキュメント参照ルール
- **設計・アーキテクチャ検討時**: 必ず `docs/baseball-project-architecture.md` を読み込んで既存のアーキテクチャパターンを確認
- **コーディング実装時**: 必ず `docs/coding-standards.md` を読み込んで既存のコーディング規約に従う
- **インフラ移行作業時**: 必ず `docs/infrastructure-migration-plan.md` を読み込んで移行計画を確認
- **重要**: 新機能や変更は既存のアーキテクチャ・規約との整合性を保つこと
