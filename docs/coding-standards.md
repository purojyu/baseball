# Baseball プロジェクト コーディング規約・アーキテクチャルール

## 目次
1. [Javaコーディング規約](#javaコーディング規約)
2. [アーキテクチャルール](#アーキテクチャルール)
3. [Vue.jsフロントエンド規約](#vuejsフロントエンド規約)
4. [データベース設計ルール](#データベース設計ルール)
5. [スクレイピング・データ収集ルール](#スクレイピングデータ収集ルール)
6. [設定・デプロイルール](#設定デプロイルール)

---

## Javaコーディング規約

### 1. 命名規則

#### クラス名パターン
```java
// Entity: 接尾辞なし、名詞形
BaseballPlayer.java
BaseballTeam.java
AtBatResult.java

// Service: Service接尾辞
BaseballPlayerService.java
BaseballTeamService.java

// Repository: Repository接尾辞
BaseballPlayerRepository.java
BaseballTeamRepository.java

// Controller: Controller接尾辞
BaseballController.java
ScrapeBatchController.java

// DTO/Request: Request, Dto接尾辞
GetPlayerListRequest.java
ResponseDto.java

// Configuration: Config接尾辞
SecurityConfig.java
WebConfig.java

// Util: Util接尾辞
BaseballUtil.java

// View Entity: V接頭辞
VAtBatGameDetails.java
VBaseballPlayerHistory.java
```

#### メソッド名パターン
```java
// Repository: 検索系
findByPlayerNm()
findAll()
findByYahooId()

// Repository: 保存系
save()

// Service: 検索系
findById()
findAll()

// Service: 保存系
savePlayer()

// Service: 計算系
calculateBattingAverage()
calculateOps()

// Controller: REST API
getInitData()
getPitcherList()
getBatterList()

// Util: 計算系
calculateBattingAverage()
calculateOnBasePercentage()
```

#### 変数名パターン
```java
// キャメルケース統一
Long playerId;           // ID系
String playerNm;         // 名前系（Nmは Name の略）
LocalDate birthDate;     // 日付系
Long height, weight;     // 物理的属性
String npbUrl;           // URL系
Long yahooId;           // 外部ID系
```

#### 定数名パターン
```java
// スネークケース（大文字）統一
private static final String SUCCESS_MESSAGE = "Success";
private static final String NO_PITCHERS_FOUND = "ピッチャーの取得に失敗しました";

// 野球用語定数
private static final String SINGLE_RESULT = "安";
private static final String DOUBLE_RESULT = "２";
private static final String HOME_RUN_RESULT = "本";
```

### 2. パッケージ構造ルール
```
com.example.baseball/
├── controller/     # REST API エンドポイント
├── dto/           # データ転送オブジェクト
├── entity/        # JPA エンティティ
├── repository/    # データアクセス層
├── service/       # ビジネスロジック層
├── util/          # ユーティリティクラス
└── config/        # 設定クラス

com.example.scraper/
├── entity/        # スクレイピング専用エンティティ
└── bk/           # バックアップファイル
```

### 3. Lombokアノテーション使用ルール

#### Entity クラス
```java
@Entity
@Data                    // getter/setter自動生成
@NoArgsConstructor       // デフォルトコンストラクタ
@AllArgsConstructor      // 全引数コンストラクタ
@Table(name = "BASEBALL_PLAYER")
public class BaseballPlayer {
    // フィールド定義
}
```

#### Controller クラス
```java
@RestController
@RequiredArgsConstructor // final フィールドコンストラクタ注入
@RequestMapping("/baseball/api")
public class BaseballController {
    private final BaseballTeamService baseballTeamService; // final で依存性注入
}
```

#### DTO クラス
```java
// Request: Getter/Setter個別指定
@Getter
@Setter
public class GetPlayerListRequest {
    // フィールド定義
}

// Response: Builderパターンと併用
@Getter
public class ResponseDto {
    // Builder パターン実装
}
```

### 4. インポート順序ルール
```java
// 1. Java標準ライブラリ
import java.time.LocalDate;
import java.util.List;

// 2. Jakarta系
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

// 3. Spring系
import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;

// 4. 外部ライブラリ
import lombok.*;

// 5. プロジェクト内パッケージ
import com.example.baseball.entity.*;
import com.example.baseball.service.*;
```

### 5. アノテーション使用ルール

#### Spring系アノテーション
```java
@SpringBootApplication  // メインクラス
@Entity                // JPA エンティティ
@Service               // サービス層
@Repository            // リポジトリ層
@RestController        // REST API コントローラ
@Configuration         // 設定クラス
@Bean                  // Bean定義
```

#### JPA系アノテーション
```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY) // 自動採番統一
@Column(name = "PLAYER_ID")                        // 物理名大文字
@Table(name = "BASEBALL_PLAYER")                   // テーブル名大文字
```

#### Validation系アノテーション
```java
@NotBlank(message = "teamIdは必須です")
@Pattern(regexp = "\\d+", message = "teamIdは数値で入力してください")
@Size(max = 2, message = "teamIdは2桁以下で入力してください")
@Validated // コントローラでの検証有効化
```

---

## アーキテクチャルール

### 1. レイヤー分離ルール

#### Controller 層の責務
```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/baseball/api")
public class BaseballController {
    // ✅ やるべきこと
    // - HTTPリクエストの受け取り
    // - パラメータ検証 (@Validated)
    // - Service層への処理委譲
    // - 統一レスポンス形式での返却
    // - エラーハンドリング（404返却等）
    
    // ❌ やってはいけないこと
    // - ビジネスロジックの実装
    // - 直接的なデータベースアクセス
    // - 複雑な計算処理
}
```

#### Service 層の責務
```java
@Service
public class BaseballPlayerService {
    // ✅ やるべきこと
    // - ビジネスロジックの実装
    // - Repository層への処理委譲
    // - データ変換・加工
    // - トランザクション境界の設定
    
    // ❌ やってはいけないこと
    // - HTTPリクエストの直接処理
    // - レスポンス形式の決定
}
```

#### Repository 層の責務
```java
@Repository
public interface BaseballPlayerRepository extends JpaRepository<BaseballPlayer, Long> {
    // ✅ やるべきこと
    // - データアクセスロジック
    // - 複雑クエリの実装 (@Query)
    // - Projectionによる最適化
    
    // ❌ やってはいけないこと
    // - ビジネスロジックの実装
    // - UI関連の処理
}
```

### 2. 依存性注入ルール

#### Controller 層: コンストラクタ注入
```java
@RestController
@RequiredArgsConstructor
public class BaseballController {
    private final BaseballTeamService baseballTeamService; // final + @RequiredArgsConstructor
}
```

#### Service 層: フィールド注入
```java
@Service
public class BaseballPlayerService {
    @Autowired
    BaseballPlayerRepository baseballPlayerRepository; // @Autowired フィールド注入
}
```

### 3. エンティティ設計ルール

#### JPA関係定義禁止
```java
@Entity
@Table(name = "BASEBALL_PLAYER")
public class BaseballPlayer {
    @Id
    @Column(name = "PLAYER_ID")
    private Long playerId;
    
    // ✅ 外部キーとして Long 型で保持
    @Column(name = "TEAM_ID")
    private Long teamId;
    
    // ❌ JPA関係定義は使用しない
    // @OneToMany
    // @ManyToOne
    // @JoinColumn
}
```

#### テーブル・カラム命名統一
```java
@Entity
@Table(name = "BASEBALL_PLAYER")        // テーブル名: 大文字スネークケース
public class BaseballPlayer {
    @Column(name = "PLAYER_ID")          // カラム名: 大文字スネークケース
    private Long playerId;               // フィールド名: キャメルケース
    
    @Column(name = "PLAYER_NM")
    private String playerNm;
}
```

### 4. エラーハンドリングアーキテクチャ

#### Controller 層でのエラー処理
```java
@GetMapping("/getPitcherList")
public ResponseEntity<ResponseDto> getPitcherList(@Validated GetPlayerListRequest request) {
    List<PlayerProjection> pitcherList = service.findPitchers(teamId, year);
    
    // 空データチェック
    if (pitcherList.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseDto.builder().message(NO_PITCHERS_FOUND).build());
    }
    
    // 成功レスポンス
    ResponseDto response = ResponseDto.builder()
            .data("pitcherList", pitcherList)
            .message("Success")
            .build();
    return ResponseEntity.ok(response);
}
```

#### Service 層でのエラー処理
```java
@Service
public class BaseballPlayerService {
    public BaseballPlayer findById(Long playerId) {
        return baseballPlayerRepository.findById(playerId).orElse(null); // null 返却統一
    }
}
```

### 5. 統一レスポンス形式
```java
// 成功時
ResponseDto response = ResponseDto.builder()
        .data("key", value)
        .message("Success")
        .build();

// エラー時
ResponseDto errorResponse = ResponseDto.builder()
        .message("エラーメッセージ")
        .build();
```

---

## Vue.jsフロントエンド規約

### 1. ファイル・コンポーネント命名規則

#### ファイル名
```
src/
├── Baseball.vue              # ルートコンポーネント (PascalCase)
├── components/
│   ├── SearchBaseball.vue    # 機能名 + 対象名
│   ├── SearchResultBaseball.vue
│   └── AppFooter.vue         # App + 機能名
└── main.js
```

#### コンポーネント名
```javascript
export default {
  name: "SearchBaseball",           // PascalCase統一
  name: "SearchResultBaseball",
  name: "AppFooter",
}
```

### 2. コンポーネント構成ルール

#### Vue ファイル構成順序
```vue
<template>
  <!-- HTML テンプレート -->
</template>

<script>
// インポート
import ComponentName from "./ComponentName.vue";

export default {
  name: "ComponentName",
  components: { ComponentName },     // 使用コンポーネント
  props: { ... },                   // 親からのデータ
  data() { return { ... }; },       // ローカル状態
  computed: { ... },                // 算出プロパティ
  watch: { ... },                   // ウォッチャー
  mounted() { ... },                // ライフサイクル
  methods: { ... },                 // メソッド
};
</script>

<style>
/* グローバルスタイル */
</style>

<style scoped>
/* コンポーネント固有スタイル */
</style>
```

### 3. コンポーネントアーキテクチャパターン

#### Container/Presentational パターン
```javascript
// Container コンポーネント (Baseball.vue)
export default {
  name: "App",
  data() {
    return {
      baseballTeamList: [],    // データ管理
      pitcherList: [],
      matchResultList: [],
      isLoading: false,        // 状態管理
    };
  },
  methods: {
    async getInitData() {      // API通信
      // データ取得処理
    }
  }
}

// Presentational コンポーネント (SearchBaseball.vue)
export default {
  name: "SearchBaseball",
  props: {
    baseballTeamList: Array,   // 親からのデータ
    pitcherList: Array,
  },
  methods: {
    getPitcherList() {
      this.$emit("getPitcherList", teamId, year); // 親への通知
    }
  }
}
```

### 4. API通信パターン

#### axios 使用ルール
```javascript
// main.js でのグローバル設定
const axiosInstance = axios.create({
  baseURL: '/baseball/api',
});
Vue.prototype.$axios = axiosInstance;

// コンポーネントでの使用
async getInitData() {
  this.isLoading = true;
  try {
    const response = await this.$axios.get("/getInitData");
    if (response.status === 200) {
      const responseData = response.data.data; // ResponseDto.data アクセス
      this.baseballTeamList = responseData.baseballTeam || [];
    }
  } catch (error) {
    // 統一エラーハンドリング
    if (error.response && error.response.data && error.response.data.message) {
      this.errorMessage = error.response.data.message;
    } else {
      this.errorMessage = "デフォルトエラーメッセージ";
    }
  } finally {
    this.isLoading = false; // 必ず finally でローディング解除
  }
}
```

### 5. スタイリング規約

#### CSS クラス命名規則
```css
/* 機能ベース命名 */
.search-button
.loading-overlay
.footer-container

/* BEM風命名 */
.multiselect__tags
.multiselect__input

/* 状態クラス */
.search-button:disabled
.th-active

/* チーム固有クラス */
.team-yakult
.team-giants
```

#### レスポンシブデザインルール
```css
/* デスクトップファースト */
.search-button {
  min-width: 120px;
  font-size: 16px;
}

@media (max-width: 767px) {
  /* タブレット・スマートフォン */
  .search-button {
    min-width: 100px;
    font-size: 14px;
  }
}

@media (max-width: 575px) {
  /* スマートフォン専用 */
  .search-button {
    font-size: 12px;
  }
}
```

### 6. JavaScript/ES6 使用ルール

#### async/await 統一
```javascript
// ✅ 全API通信で async/await 使用
async getInitData() {
  const response = await this.$axios.get("/getInitData");
}

// ❌ Promise.then() は使用しない
```

#### アロー関数 vs function 使い分け
```javascript
// Vue メソッド: function
methods: {
  getPitcherList() {     // function 使用
    // 処理
  }
}

// イベントハンドラー: アロー関数
@input="() => {        // アロー関数 使用
  this.getPitcherList();
}"

// 配列メソッド: アロー関数
this.list.filter((item) => item.id > 0)
```

---

## データベース設計ルール

### 1. テーブル命名規則

#### テーブル名
```sql
-- 大文字スネークケース統一
BASEBALL_PLAYER
BASEBALL_TEAM
AT_BAT_RESULT
PITCH_RESULT

-- ビューテーブル: V_ プレフィックス
V_AT_BAT_GAME_DETAILS
V_BASEBALL_PLAYER_HISTORY
```

#### カラム命名規則
```sql
-- 大文字スネークケース統一
PLAYER_ID           -- ID系
PLAYER_NM           -- 名前系（NMは Name の略）
BIRTH_DATE          -- 日付系
HEIGHT, WEIGHT      -- 物理属性
NPB_URL             -- URL系
YAHOO_ID            -- 外部ID系
```

### 2. 主キー・外部キー設計

#### 主キー設計
```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY) // 自動採番統一
@Column(name = "PLAYER_ID")
private Long playerId;
```

#### 外部キー設計
```java
// JPA関係定義は使用せず、Long型の外部キーで表現
@Column(name = "TEAM_ID")
private Long teamId;     // ✅ 外部キーとして保持

// ❌ JPA関係定義は禁止
// @ManyToOne
// @JoinColumn(name = "TEAM_ID")
// private BaseballTeam team;
```

### 3. ビューテーブル活用ルール

#### 非正規化ビューの使用
```java
// 複雑JOIN回避のためのビューエンティティ
@Entity
@Table(name = "V_AT_BAT_GAME_DETAILS")
public class VAtBatGameDetails {
    // 試合情報 + 打者情報 + 投手情報を非正規化
    // パフォーマンス最適化された参照専用エンティティ
}
```

---

## スクレイピング・データ収集ルール

### 1. レート制限必須ルール

#### 基本的な待機時間
```java
private static final int MIN_DELAY_MS = 5000;    // 最小5秒待機
private static final int MAX_DELAY_MS = 8000;    // 最大8秒待機
private static final int ERROR_DELAY_MS = 30000; // エラー時30秒待機

// ランダム待機実装
private void randomDelay() {
    try {
        int delay = MIN_DELAY_MS + random.nextInt(MAX_DELAY_MS - MIN_DELAY_MS);
        Thread.sleep(delay);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
}
```

#### User-Agent ローテーション
```java
private static final String[] USER_AGENTS = {
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36...",
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15...",
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36..."
};

// 使用時にランダム選択
String userAgent = USER_AGENTS[random.nextInt(USER_AGENTS.length)];
```

### 2. データ変換パイプライン

#### 段階的変換処理
```java
// 1. 生データ取得（HTML解析）
Document doc = Jsoup.connect(url).get();

// 2. 中間エンティティへの変換
BatterPitcherInfoList infoList = parseHtmlToInfoList(doc);

// 3. 正規化エンティティへの変換
List<AtBatResult> results = convertToAtBatResults(infoList);

// 4. データ統合・保存
saveResults(results);
```

### 3. エラー制御・リトライ戦略

#### 段階的エラー制御
```java
// HTTPエラー時の段階的待機
catch (HttpStatusException e) {
    if (e.getStatusCode() == 429) {      // Too Many Requests
        Thread.sleep(ERROR_DELAY_MS);    // 30秒待機
    } else if (e.getStatusCode() >= 500) { // Server Error
        Thread.sleep(ERROR_DELAY_MS * 2); // 60秒待機
    }
}
```

---

## 設定・デプロイルール

### 1. 環境別設定管理

#### プロファイル構成
```
src/main/resources/
├── application.properties           # 共通設定
├── application-dev.properties       # 開発環境設定
└── application-prod.properties      # 本番環境設定
```

#### 設定内容分離
```properties
# application.properties (共通)
spring.application.name=baseball
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# application-dev.properties (開発環境)
spring.datasource.url=jdbc:mysql://localhost:3306/baseball
spring.datasource.username=root
spring.datasource.password=Junpei74

# application-prod.properties (本番環境)
# DATABASE_URL 環境変数を使用
```

### 2. CORS設定統一ルール

#### SecurityConfig.java で一元管理
```java
@Configuration
public class SecurityConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 本番ドメインを設定
        configuration.setAllowedOrigins(Arrays.asList(
            "https://baseball-pitcher-vs-batter.com",
            "https://www.baseball-pitcher-vs-batter.com"
        ));
        return source;
    }
}

// ❌ WebConfig.java での重複設定は削除
```

### 3. Heroku デプロイ設定

#### 必須ファイル
```
# Procfile
web: java -Dserver.port=$PORT -jar build/libs/baseball-0.0.1-SNAPSHOT.jar

# system.properties
java.runtime.version=17

# package.json (Vue.js)
"heroku-postbuild": "npm run build && cp -R dist/* ../src/main/resources/static/"
```

### 4. 依存関係管理ルール

#### Gradle 設定
```gradle
dependencies {
    // Spring Boot 標準
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    
    // MyBatis 統合
    implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:2.1.4'
    
    // HTML解析
    implementation 'org.jsoup:jsoup:1.13.1'
    
    // MySQL接続
    runtimeOnly 'mysql:mysql-connector-java:8.0.28'
    
    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
}
```

---

## デュアルアプリケーション設計ルール

### 1. アプリケーション分離戦略

#### BaseballApplication (メインWebアプリ)
```java
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.example.baseball",    // メインアプリケーション
    "com.example.scraper"      // スクレイピング機能も統合
})
public class BaseballApplication {
    public static void main(String[] args) {
        SpringApplication.run(BaseballApplication.class, args);
    }
}
```

**責務**:
- REST API提供 (`/baseball/api` ベースパス)
- Vue.jsフロントエンドのホスト
- バッチ処理のマニュアルトリガー (`/batch/runScrape`)

#### NPBWebScraperApplication (独立スクレイピングアプリ)
```java
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.baseball", "com.example.scraper"})
public class NPBWebScraperApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(NPBWebScraperApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE); // Webサーバー無効
        app.run(args);
    }
}
```

**責務**:
- 独立バッチ実行
- データベースエンティティ共有
- スケジュール実行対応

### 2. 2段階データ収集システム

#### フェーズ1: NPB公式サイトスクレイピング
```java
// NPBWebScraper.java
@Component
public class NPBWebScraper {
    
    // 段階1: 基本試合データ・打席結果の収集
    public void scrapeNPBBasicData() {
        // BaseballGame, AtBatResult エンティティ作成
        // 選手の自動マッチング・作成
    }
    
    // 選手マッチングロジック
    private BaseballPlayer findOrCreatePlayer(String playerName, LocalDate birthDate) {
        // 1. 名前完全一致検索
        // 2. 名前+生年月日検索
        // 3. 身体情報込み検索
        // 4. 自動選手レコード作成
    }
}
```

#### フェーズ2: Yahoo Sports詳細スクレイピング
```java
// YahooPitchScraper.java  
@Component
public class YahooPitchScraper {
    
    // 段階2: 球種別詳細データの収集
    public void scrapeYahooDetailData() {
        // PitchResult エンティティ作成
        // 球種、球速、コース座標を記録
        // 既存AtBatResultとの関連付け
    }
}
```

### 3. データ統合・整合性管理

#### エンティティ関係統一ルール
```java
// 共有エンティティの活用
@Entity
@Table(name = "BASEBALL_PLAYER")
public class BaseballPlayer {
    @Column(name = "YAHOO_ID", unique = true)
    private Long yahooId;  // ✅ 2つのデータソース連携キー
}

// AtBatResult (NPB由来) ↔ PitchResult (Yahoo由来) の関連付け
@Entity
@Table(name = "PITCH_RESULT")
public class PitchResult {
    @Column(name = "AT_BAT_ID")
    private Long atBatId;  // AtBatResultとの外部キー関係
}
```

---

## 履歴データ整合性管理ルール

### 1. 移籍選手履歴管理

#### 履歴レコード管理戦略
```java
// NPBWebScraper.java
private void createOrUpdatePlayerHistory(Long teamId, Long playerId, Date gameDate) {
    
    // 1. 現在のアクティブ履歴を検索
    BaseballPlayerHistory activeHistory = historyService.findActiveHistory(playerId);
    
    // 2. チーム変更検出
    if (activeHistory != null && !activeHistory.getTeamId().equals(teamId)) {
        // 既存履歴を終了
        activeHistory.setEndDate(gameDate);
        historyService.save(activeHistory);
        
        // 新しい履歴レコード作成
        BaseballPlayerHistory newHistory = new BaseballPlayerHistory();
        newHistory.setPlayerId(playerId);
        newHistory.setTeamId(teamId);
        newHistory.setStartDate(gameDate);
        // endDate は null (アクティブ状態)
        historyService.save(newHistory);
    }
}
```

### 2. データ整合性検証ルール

#### 選手データの自動検証・修正
```java
// 段階的選手マッチング
private BaseballPlayer findPlayerWithFallback(String playerName, LocalDate birthDate, 
                                             Integer height, Integer weight) {
    
    // レベル1: 名前完全一致
    BaseballPlayer player = playerService.findByPlayerNm(playerName);
    if (player != null) return player;
    
    // レベル2: 名前+生年月日
    player = playerService.findByPlayerNmAndBirthDate(playerName, birthDate);
    if (player != null) return player;
    
    // レベル3: ファジーマッチング+身体情報
    player = playerService.findByPlayerProfileWithPhysical(
        cleanPlayerName(playerName), birthDate, height, weight);
    if (player != null) return player;
    
    // レベル4: 自動選手レコード作成
    return createNewPlayerRecord(playerName, birthDate, height, weight);
}
```

---

## JVM・システムリソース管理ルール

### 1. JVMクラッシュ対応ルール

#### スクレイピング処理のリソース管理
```java
// YahooPitchScraper.java
private static final int CONNECTION_TIMEOUT = 15000;  // 15秒タイムアウト
private static final int MAX_RETRIES = 3;             // 最大3回リトライ

// メモリリーク対策
private void cleanupResources() {
    try {
        // 明示的なGC提案（重処理後）
        System.gc();
        
        // コネクション適切クローズ
        if (connection != null) {
            connection.disconnect();
        }
    } catch (Exception e) {
        log.error("リソースクリーンアップエラー", e);
    }
}
```

#### JVMエラー監視・対応
```java
// hs_err_pid*.log の監視対応
// VM Arguments推奨設定:
// -Xmx2g -Xms1g 
// -XX:+HeapDumpOnOutOfMemoryError 
// -XX:HeapDumpPath=/tmp/
// -XX:+PrintGCDetails
```

### 2. レート制限による負荷制御

#### スクレイピング負荷分散
```java
// 段階的レート制限
private void smartRateLimit(int errorCount) {
    try {
        if (errorCount == 0) {
            // 通常時: 5-8秒のランダム待機
            int delay = MIN_DELAY_MS + random.nextInt(MAX_DELAY_MS - MIN_DELAY_MS);
            Thread.sleep(delay);
        } else if (errorCount < 3) {
            // 軽微エラー: 30秒待機
            Thread.sleep(ERROR_DELAY_MS);
        } else {
            // 重大エラー: 5分間休止
            Thread.sleep(ERROR_DELAY_MS * 10);
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("処理が中断されました", e);
    }
}
```

---

## 高度な検索・フィルタリング実装パターン

### 1. 日本語対応選手検索

#### 漢字・カナ両対応検索機能
```javascript
// SearchBaseball.vue
computed: {
  filteredPitcherList() {
    if (!this.searchQueryPitcher) {
      return this.localPitcherList;
    }
    
    // 全角・半角スペース正規化
    const normalizedQuery = this.searchQueryPitcher
      .replace(/[\s\u3000]/g, "")  // \u3000 = 全角スペース
      .toLowerCase();
    
    return this.localPitcherList.filter((player) => {
      // 漢字名での検索
      const playerNm = player.playerNm ? 
        player.playerNm.toLowerCase().replace(/[\s\u3000]/g, "") : "";
      
      // カナ名での検索
      const playerNmKana = player.playerNmKana ? 
        player.playerNmKana.toLowerCase().replace(/[\s\u3000]/g, "") : "";
      
      // 両方での部分一致検索
      return playerNm.includes(normalizedQuery) || 
             playerNmKana.includes(normalizedQuery);
    });
  }
}
```

### 2. 動的条件分岐検索

#### 複雑な検索条件の統一処理
```java
// VAtBatGameDetailsRepository.java
@Query(value = "SELECT * FROM V_AT_BAT_GAME_DETAILS vag " +
       "WHERE (:batterTeamId = 0 OR vag.BATTER_TEAM_ID = :batterTeamId) " +  // 0 = 全て
       "AND (:pitcherTeamId = 0 OR vag.PITCHER_TEAM_ID = :pitcherTeamId) " +  
       "AND (:batterId IS NULL OR vag.BATTER_ID = :batterId) " +              // NULL = 指定なし
       "AND (:pitcherId IS NULL OR vag.PITCHER_ID = :pitcherId) " +
       "AND (" +
       "    :selectedYear = '通算' OR (" +                                     // 通算検索
       "        vag.GAME_DATE BETWEEN STR_TO_DATE(CONCAT(:selectedYear, '-01-01'), '%Y-%m-%d') " +
       "        AND STR_TO_DATE(CONCAT(:selectedYear, '-12-31'), '%Y-%m-%d')" +
       "    )" +
       ")", nativeQuery = true)
List<VAtBatGameDetails> findByBatterAndPitcher(
    @Param("pitcherTeamId") Long pitcherTeamId,
    @Param("batterTeamId") Long batterTeamId,
    @Param("pitcherId") Long pitcherId,
    @Param("batterId") Long batterId,
    @Param("selectedYear") String selectedYear
);
```

---

## 本番環境モニタリング・ログ管理ルール

### 1. 環境別設定管理詳細

#### プロファイル自動検出
```properties
# application.properties
spring.profiles.active=dev  # デフォルト開発環境

# application-dev.properties (開発環境)
spring.datasource.url=jdbc:mysql://localhost:3306/baseball
spring.datasource.username=root
spring.datasource.password=Junpei74
# ローカル環境用設定

# application-prod.properties (本番環境)
# DATABASE_URL環境変数を自動検出
# Heroku PostgreSQL/MySQL自動設定
```

#### セキュリティ設定の環境分離
```java
// SecurityConfig.java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    
    // 本番環境: 厳格なドメイン制限
    if (Arrays.asList(environment.getActiveProfiles()).contains("prod")) {
        configuration.setAllowedOrigins(Arrays.asList(
            "https://baseball-pitcher-vs-batter.com",
            "https://www.baseball-pitcher-vs-batter.com"
        ));
    } else {
        // 開発環境: localhost許可
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:8080",
            "http://localhost:8081"
        ));
    }
    
    return source;
}
```

### 2. エラーログ・デバッグ情報管理

#### スクレイピングエラーの詳細ログ
```java
// NPBWebScraper.java
try {
    // スクレイピング処理
} catch (Exception e) {
    // 詳細なコンテキスト情報をログ出力
    System.out.println("=== エラー詳細情報 ===");
    System.out.println("エラーメッセージ: " + e.getMessage());
    System.out.println("試合情報: " + baseballGame);
    System.out.println("打者情報: " + batterInfo);
    System.out.println("投手情報: " + pitcherInfo);
    System.out.println("URL: " + currentUrl);
    
    // スタックトレース出力
    e.printStackTrace();
}
```

---

## CORS設定競合問題・解決ルール

### 1. 設定競合の詳細分析

#### 問題のある重複設定
```java
// WebConfig.java (問題のある設定)
@Configuration
public class WebConfig {
    @Bean
    WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        // ❌ 古いHerokuドメイン
                        .allowedOrigins("https://base-ball-3c86afa3058c.herokuapp.com")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}

// SecurityConfig.java (正しい設定)
@Configuration
public class SecurityConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // ✅ 現在の本番ドメイン
        configuration.setAllowedOrigins(Arrays.asList(
            "https://baseball-pitcher-vs-batter.com",
            "https://www.baseball-pitcher-vs-batter.com"));
        return source;
    }
}
```

### 2. 統一解決策

#### 推奨される設定統一
```java
// ✅ SecurityConfig.java のみで管理
@Configuration  
public class SecurityConfig {
    
    @Value("${app.cors.allowed-origins:http://localhost:8080}")
    private String[] allowedOrigins;
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

// ❌ WebConfig.java は削除または無効化
```

#### 環境別CORS設定
```properties
# application-dev.properties
app.cors.allowed-origins=http://localhost:8080,http://localhost:8081

# application-prod.properties  
app.cors.allowed-origins=https://baseball-pitcher-vs-batter.com,https://www.baseball-pitcher-vs-batter.com
```

---

## まとめ

このコーディング規約・アーキテクチャルールに従うことで：

✅ **一貫性**: 全開発者が同じパターンで実装  
✅ **保守性**: 可読性が高く、変更が容易  
✅ **拡張性**: 新機能追加が規約に沿って実装可能  
✅ **品質**: エラーハンドリングやセキュリティが統一  
✅ **パフォーマンス**: JPA Projection、ビュー活用で最適化  
✅ **安定性**: デュアルアプリケーション、リソース管理で高可用性  
✅ **国際化**: 日本語データの適切な処理とフィルタリング  
✅ **運用性**: 環境別設定、ログ管理、監視体制の確立

プロジェクトの技術的負債を減らし、持続可能な開発を実現します。