# Baseballプロジェクト - アーキテクチャ詳細ドキュメント

## プロジェクト概要

**プロジェクト名**: Baseball Pitcher vs Batter Analysis System  
**技術スタック**: Spring Boot 3.2.5 + Java 17 + Vue.js 2.6 + MySQL  
**デプロイ先**: Heroku  
**主要機能**: 日本プロ野球（NPB）の投手対打者対戦分析システム

---

## コントローラー層 (Controller Layer)

### 1. BaseballController

**ファイル位置**: `src/main/java/com/example/baseball/controller/BaseballController.java`

#### アーキテクチャパターン
- **RESTコントローラー**: `@RestController`アノテーション使用
- **依存性注入**: `@RequiredArgsConstructor`（Lombokによるコンストラクタ注入）
- **リクエストマッピング**: ベースパス `/baseball/api` を統一使用
- **バリデーション**: `@Validated`アノテーションでDTOバリデーション

#### エンドポイント詳細

| エンドポイント | HTTPメソッド | 機能 | レスポンス形式 |
|---|---|---|---|
| `/getInitData` | GET | 初期表示データ（チーム一覧・年度一覧）取得 | ResponseDto |
| `/getPitcherList` | GET | 指定チーム・年度のピッチャー一覧取得 | ResponseDto |
| `/getBatterList` | GET | 指定チーム・年度のバッター一覧取得 | ResponseDto |
| `/matchResultSearch` | GET | 投手対打者の対戦成績検索 | ResponseDto |

#### エラーハンドリングパターン
```java
// データが見つからない場合の標準的な処理
if (pitcherList.isEmpty()) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ResponseDto.builder().message(NO_PITCHERS_FOUND).build());
}
```

#### 特徴的な実装
- **定数による統一メッセージ管理**
- **null安全な文字列解析** (`parseLongOrNull`メソッド)
- **統一レスポンス形式** (`ResponseDto`使用)

### 2. ScrapeBatchController

**ファイル位置**: `src/main/java/com/example/baseball/controller/ScrapeBatchController.java`

#### アーキテクチャパターン
- **バッチ処理専用コントローラー**: `/batch`ベースパス
- **シンプルなエラーハンドリング**: try-catch + String返却

#### エンドポイント
- `GET /batch/runScrape`: NPBデータスクレイピング実行

#### エラーハンドリング
```java
try {
    npbWebScraper.scrapeData();
    return "ScrapeData done.";
} catch (Exception e) {
    e.printStackTrace();
    return "Error: " + e.getMessage();
}
```

---

## サービス層 (Service Layer)

### 1. BaseballPlayerService

**ファイル位置**: `src/main/java/com/example/baseball/service/BaseballPlayerService.java`

#### アーキテクチャパターン
- **@Service**アノテーション使用
- **@Autowired**による依存性注入（フィールド注入）
- **リポジトリパターン**: Repositoryを介したデータアクセス

#### 主要メソッド
- `findAll()`: 全選手取得
- `findById(Long playerId)`: ID検索（Optional処理あり）
- `findByplayerNm(String playerNm)`: 選手名検索
- `findByPlayerNmAndBirthDate()`: 選手名+生年月日検索
- `findByYahooId(Long yahooId)`: Yahoo ID検索
- `findByPlayerProfileWithPhysical()`: プロフィール+身体情報による複合検索

#### 特徴
- **複数の検索条件サポート**: 選手の一意特定のための多様な検索方法
- **null安全**: `orElse(null)`でOptional処理

### 2. BaseballTeamService

**ファイル位置**: `src/main/java/com/example/baseball/service/BaseballTeamService.java`

#### シンプルな構造
- 基本的なCRUD操作のみ
- `findAllBaseballTeam()`: 全チーム取得
- `findByTeamNm(String teamNm)`: チーム名検索

### 3. AtBatStatisticsService

**ファイル位置**: `src/main/java/com/example/baseball/service/AtBatStatisticsService.java`

#### 高度な統計計算ロジック
- **関数型プログラミング**: StreamAPI活用
- **動的データ処理**: 投手/打者指定パターンによる条件分岐
- **複雑な集計処理**: 打率、出塁率、長打率、OPS等の統計指標計算

#### 重要メソッド
```java
public List<MatchResult> retrieveAtBatResults(List<VAtBatGameDetails> atBatResults, 
                                             Long pitcherId, Long batterId) {
    if (pitcherId == null) {
        return processResults(atBatResults, VAtBatGameDetails::getPitcherId, 
                            MatchResult::getPitcherTeamId);
    } else if (batterId == null) {
        return processResults(atBatResults, VAtBatGameDetails::getBatterId, 
                            MatchResult::getBatterTeamId);
    } else {
        return Collections.singletonList(calcAtBatResult(atBatResults));
    }
}
```

#### 特徴的パターン
- **移籍対応**: 選手が複数チームに所属していた場合の処理
- **関数型アプローチ**: `Function<T, R>`を使った汎用的処理
- **統計計算の委譲**: `BaseballUtil`クラスへの計算ロジック委譲

---

## エラーハンドリングパターン

### 1. コントローラー層でのエラーハンドリング

#### 標準パターン
```java
// データ不存在時
if (dataList.isEmpty()) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ResponseDto.builder().message(ERROR_MESSAGE).build());
}

// 正常時
ResponseDto response = ResponseDto.builder()
        .data("key", dataList)
        .message("Success")
        .build();
return ResponseEntity.ok(response);
```

#### 特徴
- **統一されたレスポンス形式**: 必ず`ResponseDto`でラップ
- **適切なHTTPステータス**: データ不存在時は404
- **定数化されたエラーメッセージ**: ハードコーディング回避

### 2. サービス層でのエラーハンドリング

#### null安全パターン
```java
public BaseballPlayer findById(Long playerId) {
    return baseballPlayerRepository.findById(playerId).orElse(null);
}
```

#### 特徴
- **Optional活用**: JPA標準のOptional型を適切に処理
- **null返却**: 見つからない場合は明示的にnull返却

### 3. バッチ処理でのエラーハンドリング

#### シンプルなcatch-all パターン
```java
try {
    npbWebScraper.scrapeData();
    return "ScrapeData done.";
} catch (Exception e) {
    e.printStackTrace();
    return "Error: " + e.getMessage();
}
```

---

## 設定クラス詳細・競合問題

### 1. CORS設定の重複と競合リスク

#### WebConfig.java vs SecurityConfig.java の競合
**問題**: 2つのクラスでCORS設定が重複し、予期しない動作を引き起こす可能性

**WebConfig.java (ルートレベル)**:
```java
@Configuration
public class WebConfig {
    @Bean
    WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("https://base-ball-3c86afa3058c.herokuapp.com")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
```

**SecurityConfig.java (Securityフィルター)**:
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList(
        "https://baseball-pitcher-vs-batter.com",
        "https://www.baseball-pitcher-vs-batter.com"));
    // より詳細な設定...
}
```

#### 設定競合の問題点
- **異なるドメイン**: WebConfigとSecurityConfigで許可ドメインが異なる
- **フィルター優先順位**: Spring Securityのフィルターが先に適用される
- **開発環境の混乱**: コメントアウトされたlocalhost設定の存在

#### 改善提案
- WebConfig.javaのCORS設定を削除し、SecurityConfig.javaに統一
- 環境別設定ファイルでドメインを管理

---

## 高度なデータアクセスパターン

### 1. JPA Projectionインターフェース

**ファイル位置**: `src/main/java/com/example/baseball/entity/PlayerProjection.java`

#### 軽量データ取得戦略
```java
public interface PlayerProjection {
    Long getPlayerId();
    String getPlayerNm();
    String getPlayerNmKana();
}
```

#### 特徴・利点
- **パフォーマンス最適化**: 必要なカラムのみを取得
- **メモリ効率**: 大きなエンティティの一部のみロード
- **型安全性**: インターフェースによる型保証
- **JPA標準**: Spring Data JPAの標準機能活用

#### 使用パターン
```java
// リポジトリでの活用例
List<PlayerProjection> findPitcherByTeamIdAndYear(long teamId, String year);
```

### 2. データベースビューエンティティ

**ファイル位置**: `src/main/java/com/example/baseball/entity/VAtBatGameDetails.java`

#### 非正規化ビューの活用
```java
@Entity
@Table(name = "V_AT_BAT_GAME_DETAILS")
public class VAtBatGameDetails {
    @Id
    @Column(name = "AT_BAT_ID")
    private Long atBatId;
    
    // 試合情報
    @Column(name = "HOME_TEAM_ID")
    private Long homeTeamId;
    @Column(name = "GAME_DATE")
    private Date gameDate;
    @Column(name = "STADIUM")
    private String stadium;
    
    // 打者情報（非正規化）
    @Column(name = "BATTER_NM")
    private String batterNm;
    @Column(name = "BATTER_NPB_URL")
    private String batterNpbUrl;
    
    // 投手情報（非正規化）
    @Column(name = "PITCHER_NM")
    private String pitcherNm;
    // 約50個のカラムマッピング...
}
```

#### 設計戦略
- **パフォーマンス重視**: 複雑JOINをビューで事前実行
- **読み取り専用**: 更新不要な参照専用エンティティ
- **レポート特化**: 統計計算・分析クエリに最適化

---

## スクレイピング・データ収集アーキテクチャ

### 1. 高度なレート制限システム (YahooPitchScraper)

**ファイル位置**: `src/main/java/com/example/scraper/YahooPitchScraper.java`

#### 多層防御のレート制限
```java
// User-Agentローテーション
private static final String[] USER_AGENTS = {
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36...",
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15...",
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36..."
};

// 段階的待機戦略
private static final int MIN_DELAY_MS = 5000;   // 最小5秒
private static final int MAX_DELAY_MS = 8000;   // 最大8秒
private static final int ERROR_DELAY_MS = 30000; // エラー時30秒
```

#### レート制限実装パターン
- **ランダム遅延**: 5-8秒のランダム待機で人間的な動作模擬
- **User-Agentローテーション**: 3種類のブラウザを循環使用
- **エラー段階制御**: HTTP 429等での段階的待機時間延長
- **緊急停止機能**: 連続エラー時の最大5分間休止

#### 倫理的スクレイピング実装
```java
// 適切な待機処理
private void randomDelay() {
    try {
        int delay = MIN_DELAY_MS + random.nextInt(MAX_DELAY_MS - MIN_DELAY_MS);
        Thread.sleep(delay);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
}
```

### 2. スクレイピング専用データ変換エンティティ

**ファイル位置**: `src/main/java/com/example/scraper/entity/BatterPitcherInfoList.java`

#### 中間データ構造設計
```java
@Data
public class BatterPitcherInfoList {
    private List<BatterResults> topBatterResults;      // 先攻打者データ
    private List<PitcherResults> topPitcherResults;    // 先攻投手データ
    private List<BatterResults> bottomBatterResults;   // 後攻打者データ
    private List<PitcherResults> bottomPitcherResults; // 後攻投手データ
}
```

#### データ変換パイプライン
1. **生データ取得**: HTML解析による一次データ抽出
2. **中間変換**: スクレイピング専用エンティティへの変換
3. **最終変換**: 正規化されたDBエンティティへの変換
4. **データ統合**: 既存データとの重複チェック・統合

---

## デプロイメント・インフラ設定

### 1. Heroku本番環境設定

#### Procfile: 動的ポート対応
```
web: java -Dserver.port=$PORT -jar build/libs/baseball-0.0.1-SNAPSHOT.jar
```
- **$PORT変数**: Herokuが動的に割り当てるポート番号を使用
- **JAR実行**: Spring Bootの埋め込みTomcatで実行

#### system.properties: Java Runtime指定
```
java.runtime.version=17
```
- **Java 17**: プロジェクトのJava仕様バージョンを明示指定

### 2. Gradle詳細設定分析

#### 重要な依存関係
```gradle
dependencies {
    // MyBatis統合
    implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:2.1.4'
    
    // HTML解析
    implementation 'org.jsoup:jsoup:1.13.1'
    
    // MySQL接続
    runtimeOnly 'mysql:mysql-connector-java:8.0.28'
    
    // セキュリティ
    implementation 'org.springframework.boot:spring-boot-starter-security'
}
```

#### アプリケーション設定
```gradle
application {
    mainClassName = 'com.example.baseball.BaseballApplication'
}
```

---

## ユーティリティ・設定コンポーネント

### 1. BaseballUtil (ユーティリティクラス)

**ファイル位置**: `src/main/java/com/example/baseball/util/BaseballUtil.java`

#### 設計パターン
- **static utilityクラス**: 全メソッドがstatic
- **定数駆動**: 野球結果の文字列定数を使用
- **BigDecimal精度計算**: 小数点以下3桁まで正確な計算

#### 主要統計計算メソッド
- `calculateBattingAverage()`: 打率計算
- `calculateOnBasePercentage()`: 出塁率計算
- `calculateSluggingPercentage()`: 長打率計算
- `calculateOps()`: OPS計算
- 各種カウント計算（ヒット数、本塁打数、三振数等）

#### 特徴的実装
```java
// 部分一致によるヒット判定
if (result.contains(SINGLE_RESULT) || result.contains(DOUBLE_RESULT) ||
    result.contains(TRIPLE_RESULT) || result.contains(HOME_RUN_RESULT)) {
    hits++;
    atBats++;
}
```

### 2. SecurityConfig (セキュリティ設定)

**ファイル位置**: `src/main/java/com/example/baseball/config/SecurityConfig.java`

#### 設定パターン
- **プロファイル別設定**: 開発環境と本番環境の条件分岐
- **CORS設定**: Vue.jsフロントエンドとの連携用
- **HTTPS強制**: 本番環境でのセキュリティ確保
- **ForwardedHeaderFilter**: Herokuリバースプロキシ対応

#### 本番環境設定例
```java
.requiresChannel(channel -> channel.anyRequest().requiresSecure()) // HTTPS強制
.cors(Customizer.withDefaults()) // CORS有効
.authorizeHttpRequests(auth -> auth.anyRequest().permitAll()) // 認証なし
```

### 3. ResponseDto (レスポンスDTO)

**ファイル位置**: `src/main/java/com/example/baseball/dto/ResponseDto.java`

#### 設計パターン
- **Builderパターン**: 流れるようなAPI構築
- **immutableオブジェクト**: 生成後の変更不可
- **Map-based data**: 柔軟なデータ構造対応

#### 使用例
```java
ResponseDto response = ResponseDto.builder()
        .data("baseballTeam", baseballTeamList)
        .data("years", years)
        .message(SUCCESS_MESSAGE)
        .build();
```

---

## アーキテクチャの特徴と設計方針

### 1. レイヤー構造

```
Controller Layer (REST API) 
    ↓
Service Layer (ビジネスロジック)
    ↓
Repository Layer (データアクセス)
    ↓
Database (MySQL)
```

### 2. 依存性注入パターン

- **コントローラー**: `@RequiredArgsConstructor`（コンストラクタ注入）
- **サービス**: `@Autowired`（フィールド注入）
- **一貫性**: Spring標準の@Serviceと@Repositoryアノテーション使用

### 3. データアクセスパターン

- **JPA使用**: Spring Data JPA + Hibernate
- **複雑クエリ対応**: データベースビュー活用（V_AT_BAT_GAME_DETAILS等）
- **関係定義なし**: エンティティ間でJPA関係定義せず、サービス層で結合

### 4. エラーハンドリング戦略

- **レイヤー別責任分離**: 
  - Controller: HTTPステータス + ResponseDto
  - Service: null返却 + Optional処理
  - Repository: JPA標準例外

### 5. 統計計算アーキテクチャ

- **関心の分離**: Utilityクラスへのstaticメソッド集約
- **精度保証**: BigDecimalによる高精度計算
- **拡張性**: 新しい統計指標の追加が容易

---

## フロントエンド層 (Vue.js Layer)

### 1. メインアプリケーションコンポーネント (Baseball.vue)

**ファイル位置**: `vue-project/src/Baseball.vue`

#### アーキテクチャパターン
- **コンテナコンポーネント**: 全体の状態管理とAPI通信を担当
- **子コンポーネントとの通信**: props down, events up パターン
- **非同期処理**: async/await + try-catch-finally構造

#### 状態管理パターン
```javascript
data() {
  return {
    baseballTeamList: [],      // マスターデータ
    pitcherList: [],           // 動的データ（チーム・年度選択で更新）
    batterList: [],            // 動的データ（チーム・年度選択で更新）
    matchResultList: [],       // 検索結果データ
    errorMessage: "",          // エラー状態管理
    years: [],                 // 年度マスター
    isLoading: false,          // ローディング状態
  };
}
```

#### API通信パターン
- **統一エラーハンドリング**: response.data.message を活用
- **ローディング管理**: try-catch-finally で確実なローディング状態制御
- **ResponseDto対応**: `response.data.data` でデータアクセス

#### 特徴的実装
```javascript
// 初期化時の年度リスト拡張
this.years.unshift("通算");  // 「通算」オプションを先頭に追加

// エラー状態の分岐処理
if (error.response && error.response.data && error.response.data.message) {
    this.errorMessage = error.response.data.message;
} else {
    this.errorMessage = "対戦結果の取得に失敗しました";
}
```

### 2. 検索コンポーネント (SearchBaseball.vue)

**ファイル位置**: `vue-project/src/components/SearchBaseball.vue`

#### アーキテクチャパターン
- **プレゼンテーションコンポーネント**: UI表示とユーザーインタラクションに専念
- **イベント駆動**: $emit でコンテナコンポーネントに通知
- **複合検索UI**: マルチセレクト + 条件分岐表示

#### 高度な検索機能実装
```javascript
// 日本語対応検索（漢字・カナ両対応）
filteredPitcherList() {
  const normalizedQuery = this.searchQueryPitcher.replace(/[\s\u3000]/g, "").toLowerCase();
  return this.localPitcherList.filter((player) => {
    const playerNm = player.playerNm ? player.playerNm.toLowerCase().replace(/[\s\u3000]/g, "") : "";
    const playerNmKana = player.playerNmKana ? player.playerNmKana.toLowerCase().replace(/[\s\u3000]/g, "") : "";
    return playerNm.includes(normalizedQuery) || playerNmKana.includes(normalizedQuery);
  });
}
```

#### Vue-Multiselect高度活用
- **動的オプション生成**: computed プロパティでチームオプション構築
- **検索無効化制御**: `:filterable="false"` + `:internal-search="false"`
- **カスタム検索**: `@search-change` イベントでの独自検索ロジック

#### バリデーションパターン
```javascript
// 検索可能条件の動的判定
isSearchEnabled() {
  return (this.selectPitcherOptions || this.selectBatterOptions) && this.selectedYear;
}
```

### 3. 検索結果コンポーネント (SearchResultBaseball.vue)

**ファイル位置**: `vue-project/src/components/SearchResultBaseball.vue`

#### 高度なテーブル機能
- **動的ソート機能**: 全カラムでの昇順・降順ソート対応
- **チーム色分け表示**: プロ野球12球団の公式カラーによるセル背景色
- **数値フォーマット**: 野球統計に特化した小数点表示制御

#### ソート実装パターン
```javascript
sortedMatchResultList() {
  if (!this.sortKey) return this.matchResultList;
  return [...this.matchResultList].sort((a, b) => {
    const aVal = a[this.sortKey];
    const bVal = b[this.sortKey];
    if (typeof aVal === "number" && typeof bVal === "number") {
      return this.sortAsc ? aVal - bVal : bVal - aVal;
    } else {
      return this.sortAsc ? aVal.localeCompare(bVal) : bVal.localeCompare(aVal);
    }
  });
}
```

#### チーム色分けシステム
```javascript
// プロ野球12球団のブランドカラー管理
getTeamClass(teamId) {
  const teamClasses = {
    1: "team-yakult",    // 東京ヤクルトスワローズ
    2: "team-giants",    // 読売ジャイアンツ
    3: "team-dena",      // 横浜DeNAベイスターズ
    // ... 12球団すべてのカラー定義
  };
  return teamClasses[teamId] || "default-team";
}
```

---

## データ層 (Data Layer)

### 1. エンティティクラスの実装パターン

#### 標準的なJPAエンティティ構造
```java
@Entity
@Data                    // Lombokによるgetter/setter自動生成
@NoArgsConstructor       // デフォルトコンストラクタ
@AllArgsConstructor      // 全フィールドコンストラクタ
@Table(name = "BASEBALL_PLAYER")  // 物理テーブル名（UPPER_CASE）
public class BaseballPlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PLAYER_ID")
    private Long playerId;
    
    // 各フィールドに@Column(name = "PHYSICAL_NAME")でマッピング
}
```

#### 特徴的な設計方針
- **JPA関係定義なし**: @OneToMany, @ManyToOneを使用せず、リポジトリ層で結合
- **命名規則統一**: 物理名UPPER_CASE、論理名camelCase
- **Lombokフル活用**: ボイラープレートコード削減
- **Yahoo ID連携**: `@Column(name = "YAHOO_ID", unique = true)` でクロスプラットフォーム連携

### 2. リポジトリクラスの実装パターン

#### Spring Data JPA + カスタムクエリパターン
```java
@Repository
public interface BaseballPlayerRepository extends JpaRepository<BaseballPlayer, Long> {
    
    // JPQLクエリ
    @Query("SELECT bp FROM BaseballPlayer bp WHERE bp.playerNm = :playerNm")
    BaseballPlayer findByplayerNm(@Param("playerNm") String playerNm);
    
    // ネイティブクエリ（複雑な検索条件）
    @Query(value = "SELECT * FROM BASEBALL_PLAYER " +
                   "WHERE PLAYER_NM LIKE CONCAT('%', :cleanedName, '%') " +
                   "AND BIRTH_DATE = :birthDate", nativeQuery = true)
    BaseballPlayer findByPlayerNmAndBirthDateByYahooNm(
        @Param("cleanedName") String cleanedName,
        @Param("birthDate") LocalDate birthDate
    );
}
```

#### 複雑な検索クエリ実装
- **パラメータ条件分岐**: `(:param = 0 OR column = :param)` パターン
- **日付範囲検索**: `STR_TO_DATE` + `BETWEEN` での年度検索
- **LIKE検索**: 部分一致検索での選手名マッチング

### 3. DTOクラスの実装パターン

#### Bean Validation活用
```java
@Getter
@Setter
public class GetPlayerListRequest {
    @NotBlank(message = "teamIdは必須です")
    @Pattern(regexp = "\\d+", message = "teamIdは数値で入力してください")
    @Size(max = 2, message = "teamIdは2桁以下で入力してください")
    private String teamId;
    
    @NotBlank(message = "yearは必須です")
    @Size(max = 4, message = "yearは4桁以下で入力してください")
    private String year;
}
```

#### 任意項目の高度なバリデーション
```java
// グループバリデーション活用
@Pattern(regexp = "\\d+", message = "pitcherIdは数値で入力してください", 
         groups = OptionalCheck.class)
private String pitcherId;

public interface OptionalCheck {}  // バリデーショングループ定義
```

---

## スクレイピング・データ収集層

### 1. NPBWebScraperの実装パターン

**ファイル位置**: `src/main/java/com/example/scraper/NPBWebScraper.java`

#### 日本語データ処理特化
```java
// 複数の日付フォーマットサポート
DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy年M月d日", Locale.JAPANESE);
DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.JAPANESE);
```

#### Jsoup活用パターン
- **要素選択**: CSSセレクタによる精密なHTML要素抽出
- **データクリーニング**: 正規表現による日本語テキスト処理
- **エラー耐性**: null チェックと例外処理の徹底

#### 選手マッチングロジック
- **段階的マッチング**: 名前 → 名前+生年月日 → 身体情報込み検索
- **自動選手登録**: 新規選手の自動BaseballPlayerエンティティ作成

---

## ビルド・デプロイ設定

### 1. Vue.js ビルド設定 (vue.config.js)

```javascript
module.exports = defineConfig({
  // Spring Boot統合設定
  outputDir: path.resolve(__dirname, "../src/main/resources/static"),
  
  // 開発プロキシ設定
  devServer: {
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
        pathRewrite: { "^/api": "" },
      },
    },
  },
  
  // プロダクション最適化
  productionSourceMap: false,
});
```

### 2. Herokuデプロイ自動化

```json
// package.json
"scripts": {
  "heroku-postbuild": "npm run build && cp -R dist/* ../src/main/resources/static/"
}
```

#### 特徴
- **シングルデプロイ**: Spring BootアプリにVue.jsを組み込み
- **環境別プロキシ**: 開発時のCORS問題解決
- **自動ビルドパイプライン**: Herokuデプロイ時の自動フロントエンドビルド

---

## プロファイル設定戦略

### 1. 環境別設定ファイル

#### 開発環境 (application-dev.properties)
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/baseball
spring.datasource.username=root
spring.datasource.password=Junpei74
```

#### 本番環境設定
- **環境変数ベース**: `DATABASE_URL` での接続情報管理
- **セキュリティ強化**: HTTPS強制、CORS制限
- **プロファイル自動検出**: 手動設定不要

---

## 今後の改善提案

### 1. エラーハンドリングの統一
- グローバル例外ハンドラー（@ControllerAdvice）の導入
- カスタム例外クラスの作成

### 2. 依存性注入の統一
- フィールド注入からコンストラクタ注入への統一

### 3. バリデーション強化
- Bean Validationの活用拡大
- カスタムバリデーターの実装

### 4. ログ戦略の改善
- 構造化ログの導入
- 適切なログレベル設定

### 5. フロントエンド改善
- Vue 3への移行検討
- TypeScript導入
- 状態管理ライブラリ（Vuex/Pinia）の導入

### 6. テスト戦略
- ユニットテストカバレッジ向上
- E2Eテストの導入
- APIテストの自動化

### 7. 設定競合解決
- **CORS設定統一**: WebConfig.javaの削除、SecurityConfig.javaに一元化
- **環境変数管理**: 開発環境パスワード等の機密情報を環境変数化
- **ドメイン設定**: 本番・開発環境で異なるドメイン設定の統一管理

### 8. アーキテクチャ改善
- **Projectionパターン拡張**: 他エンティティでの軽量データ取得適用
- **レート制限の一般化**: スクレイピング以外でも使える汎用レート制限コンポーネント
- **データ変換パイプライン**: より構造化されたETL処理の実装