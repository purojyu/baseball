package com.example.scraper;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.baseball.entity.AtBatResult;
import com.example.baseball.entity.BaseballGame;
import com.example.baseball.entity.BaseballPlayer;
import com.example.baseball.entity.PitchResult;
import com.example.baseball.service.AtBatResultService;
import com.example.baseball.service.BaseballGameService;
import com.example.baseball.service.BaseballPlayerService;
import com.example.baseball.service.PitchResultService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Yahoo! 一球速報スクレイパ（レート制限対応版）
 * 
 * 改善点：
 * 1. リクエスト間隔を大幅に増加
 * 2. ランダム待機時間の追加
 * 3. User-Agentローテーション
 * 4. エラー時の長時間待機
 * 5. リトライ機能の実装
 */
@Component
public class YahooPitchScraper {

    /* ----------------- static const ------------------ */

    private static final Logger log = LoggerFactory.getLogger(YahooPitchScraper.class);

    // URLs
    private static final String YAHOO_BASE_URL = "https://baseball.yahoo.co.jp";
    private static final String SCORE_URL = YAHOO_BASE_URL + "/npb/game/%s/score?index=%s";
    private static final String SCHEDULE_URL = YAHOO_BASE_URL + "/npb/schedule/?date=%s&gameKindIds=%s";
    private static final String GAME_TOP_URL = YAHOO_BASE_URL + "/npb/game/%s/top";
    private static final String PLAYER_URL = YAHOO_BASE_URL + "/npb/player/%d/top";
    
    // Network settings
    private static final int CONNECTION_TIMEOUT = 15000;

    // Regex patterns
    private static final Pattern PLAYER_ID = Pattern.compile("/npb/player/(\\d+)/top");
    private static final Pattern GAME_DATE_PATTERN = Pattern.compile("(\\d{4})年(\\d{1,2})月(\\d{1,2})日");
    private static final Pattern BIRTH_DATE_PATTERN = Pattern.compile("(\\d{4})年(\\d{1,2})月(\\d{1,2})日");
    // 小数点座標にも対応 (例: top:0.0px; left:25.2px;)
    private static final Pattern STYLE_PATTERN = Pattern.compile("top:(-?\\d+(?:\\.\\d+)?)px; left:(-?\\d+(?:\\.\\d+)?)px");

    private static final DateTimeFormatter DF = DateTimeFormatter.ISO_DATE;

    // User-Agentローテーション用
    private static final String[] USER_AGENTS = {
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Safari/605.1.15",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    };

    // Sleep intervals (milliseconds)
    private static final int MIN_REQUEST_INTERVAL = 30000;
    private static final int MAX_REQUEST_INTERVAL = 30000;
    private static final int MIN_GAME_INTERVAL = 15000;
    private static final int MAX_GAME_INTERVAL = 25000;
    private static final int MIN_DAY_INTERVAL = 10000;
    private static final int MAX_DAY_INTERVAL = 15000;
    private static final int DAILY_SLEEP_WITH_GAMES = 600000; // 10分（試合がある日のみ）
    private static final int MIN_PLAYER_INTERVAL = 2000;
    private static final int MAX_PLAYER_INTERVAL = 4000;
    private static final int ERROR_SLEEP_MIN = 30000;
    private static final int ERROR_SLEEP_MAX = 60000;
    private static final int RATE_LIMIT_SLEEP_MIN = 120000;
    private static final int RATE_LIMIT_SLEEP_MAX = 180000;
    private static final int EMERGENCY_SLEEP_MIN = 180000;
    private static final int EMERGENCY_SLEEP_MAX = 300000;
    
    // Game types
    private static final String LEAGUE_GAMES = "1,2";
    private static final String INTERLEAGUE_GAMES = "26";
    
    private final Random random = new Random();
    private int requestCount = 0;
    
    // Yahoo!の実際のレイアウトに基づいて調整
    private static final int CHART_WIDTH = 160;     // .bb-allocationChart width
    private static final int CHART_HEIGHT = 200;    // height
    private static final int GRID_COLS = 5;
    private static final int GRID_ROWS = 5;
    private static final int BORDER_PX = 0;         // パディング調整
    // ボール中心オフセット = border(1px) + margin(2px) + radius(10px) = 13px
    // CSS: .bb-allocationChart { border:1px solid }, .bb-icon__ballCircle { width:20px; height:20px; margin:2px }
    // 絶対配置はborder内側基準なので、5x5グリッド全体(200px)で計算する際はborderの1px加算
    private static final int BALL_RADIUS = 13;

    private static final double CELL_W = (double) CHART_WIDTH / GRID_COLS;  // 32.0px
    private static final double CELL_H = (double) CHART_HEIGHT / GRID_ROWS; // 40.0px

    /* ----------------- DI services ------------------- */

    @Autowired private PitchResultService    pitchResultService;
    @Autowired private BaseballPlayerService baseballPlayerService;
    @Autowired private BaseballGameService   baseballGameService;
    @Autowired private AtBatResultService    atBatResultService;

    /* =================================================
     *  PUBLIC METHODS
     * ================================================= */

    /**
     * 指定された期間のNPB試合データをスクレイピングし、投球結果をデータベースに保存する。
     * レート制限やエラーに対応した安全なスクレイピングを実行。
     * 
     * @param from 開始日（含む）
     * @param to   終了日（含む）
     * @throws IllegalArgumentException 日付がnullまたはfrom > toの場合
     */
    public void scrapeRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("日付はnullにできません");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("開始日は終了日より前である必要があります");
        }
        log.info("スクレイピング開始: {} から {} まで", from, to);

        boolean isFirstDay = true;
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            try {
                log.info("処理中: {}", d);
                boolean hasGames = false;

                // リーグ戦をチェック
                if (fetchScheduleForKind(d, LEAGUE_GAMES)) {
                    hasGames = true;
                } else {
                    // 交流戦をチェック
                    if (fetchScheduleForKind(d, INTERLEAGUE_GAMES)) {
                        hasGames = true;
                    }
                }

                // 試合がある日だけ長時間待機（レート制限対策）
                // ただし、最初の日は待機しない
                if (hasGames) {
                    if (isFirstDay) {
                        log.info("試合あり（初日のため待機なし）");
                        isFirstDay = false;
                    } else {
                        log.info("試合あり。レート制限対策のため10分待機します...");
                        safeSleep(DAILY_SLEEP_WITH_GAMES, DAILY_SLEEP_WITH_GAMES);
                    }
                } else {
                    log.info("試合なし。短時間待機");
                    safeSleep(MIN_DAY_INTERVAL, MAX_DAY_INTERVAL);
                    isFirstDay = false; // 試合がなくても次回からは通常処理
                }

            } catch (Exception e) {
                log.error("scrapeRange → {} の処理で致命的エラー", d, e);
                throw new RuntimeException("スクレイピング処理を中断します", e);
            }
        }
        
        log.info("全スクレイピング処理完了。総リクエスト数: {}", requestCount);
    }

    /* =================================================
     *  SCHEDULE PROCESSING
     * ================================================= */

    private boolean fetchScheduleForKind(LocalDate date, String kindIds) {
        safeSleep(MIN_REQUEST_INTERVAL, MAX_REQUEST_INTERVAL);
        String url = String.format(SCHEDULE_URL, DF.format(date), kindIds);
        boolean success = false; 

        try {
            Document doc = connectSafely(url);
            int gameCount = 0;

            for (Element a : doc.select("a.bb-score__content[href*=/game/]")) {
                String gameId = a.attr("href").replaceAll(".*/game/(\\d+)/.*", "$1");

                // 試合終了かどうか
                String stateTxt = connectSafely(String.format(GAME_TOP_URL, gameId))
                        .selectFirst("p.bb-gameCard__state")
                        .text();
                        
                if (!stateTxt.contains("試合終了")) continue;

                gameCount++;

                // 試合をスクレイピング
                scrapeGame(gameId);

                safeSleep(MIN_GAME_INTERVAL, MAX_GAME_INTERVAL);
            }
            log.info("{}日の試合取り込みが終了しました", date);
            success = true;
        } catch (HttpStatusException e) {
            if (e.getStatusCode() == 429 || e.getStatusCode() == 403) {
                log.warn("レート制限検出 - 長時間待機: HTTP {} {}", e.getStatusCode(), url);
                safeSleep(RATE_LIMIT_SLEEP_MIN, RATE_LIMIT_SLEEP_MAX); // 2-3分待機
            } else {
                log.warn("schedule fetch: HTTP {} {}", e.getStatusCode(), url);
            }
        } catch (Exception e) {
            // 存在しないURLの時に表示される可能性が高い(交流戦と通常試合のどっちかではエラーになるので改修)
            log.warn("schedule fetch failed: date={}, kindIds={}, url={}",
                    date, kindIds, url, e);
        }
        return success;
    }

    /* =================================================
     *  GAME PROCESSING
     * ================================================= */

    private void scrapeGame(String gameId) throws IOException, InterruptedException {

        BaseballGame game = resolveGameFromYahoo(gameId);
        List<AtBatResult> atBats =
            new ArrayList<>(atBatResultService.findByGameId(game.getGameId()));

        if (atBats.isEmpty()) {
            log.warn("AT_BAT_RESULT 空: gameId={}", game.getGameId());
            return;
        }

        String index = fetchStartIndex(gameId);   // 基本は「0110100」始まり
        
        log.info("index："+ index);
        
        List<PitchResult> prList = new ArrayList<>();
        AtBatResult currentAB = null;
        int pitchCount = 0;
        
        // チーム別の最後のバッター情報を管理
        Map<String, AtBatResult> lastBatterByTeam = new HashMap<>();
        
        while (index != null && !atBats.isEmpty()) {

            try {
                /* ---- 打席ページ取得 ---- */
                safeSleep(MIN_REQUEST_INTERVAL, MAX_REQUEST_INTERVAL);
                Document doc = connectSafely(String.format(SCORE_URL, gameId, index));

                long pitId = extractPlayerId(doc, true);
                long batId = extractPlayerId(doc, false);
                
                // 投手交代など、1球ずつの結果が存在しない場合
                if (pitId == 0 || batId == 0) {
                    index = getNextIndex(doc);
                    continue;
                }
                
                BaseballPlayer pit = resolvePlayer(pitId);
                BaseballPlayer bat = resolvePlayer(batId);
                
                // 現在の攻撃チームを取得
                String attackingTeam = extractAttackingTeam(doc);
                AtBatResult lastBatterForTeam = lastBatterByTeam.get(attackingTeam);
                
                
                
                // 1. まず継続処理の条件をチェック（連続打席の優先処理）
                if (lastBatterForTeam != null && 
                    lastBatterForTeam.getBatterId().equals(bat.getPlayerId()) && 
                    lastBatterForTeam.getPitcherId().equals(pit.getPlayerId())) {
                    // 同じチームの同じ打席の継続（既に処理済みの打席）
                    log.info("🔄 同一打席継続処理開始");
                    log.info("継続打席条件チェック: lastBatter.batterId={} == Yahoo.batterId={} ? {}", 
                            lastBatterForTeam.getBatterId(), bat.getPlayerId(), 
                            lastBatterForTeam.getBatterId().equals(bat.getPlayerId()));
                    log.info("継続打席条件チェック: lastBatter.pitcherId={} == Yahoo.pitcherId={} ? {}", 
                            lastBatterForTeam.getPitcherId(), pit.getPlayerId(), 
                            lastBatterForTeam.getPitcherId().equals(pit.getPlayerId()));
                    
                    // 既存のPitchResultを削除
                    int beforeSize = prList.size();
                    prList.removeIf(pr -> pr.getAtBatId().equals(lastBatterForTeam.getAtBatId()));
                    int afterSize = prList.size();
                    log.info("既存PitchResult削除: {} → {} 件 (削除={} 件)", 
                            beforeSize, afterSize, (beforeSize - afterSize));
                    
                    currentAB = lastBatterForTeam;
                    log.info("✅ 同一打席継続成功: team={}, batter={}, pitcher={}, atBatId={}", 
                            attackingTeam, bat.getPlayerNm(), pit.getPlayerNm(), currentAB.getAtBatId());
                } else {
                    // 2. 継続処理ではない場合、ID一致で検索
                    currentAB = findAndPopAtBat(atBats, bat.getPlayerId(), pit.getPlayerId());
                    
                    
                    if (currentAB != null) {
                        // チーム別最後バッター情報を更新
                        lastBatterByTeam.put(attackingTeam, currentAB);
                    }
                }
                
                /* ---- DB の打席と照合 ---- */
                if (currentAB == null) {
                    // 代打交代等で該当する打席が存在しない場合（正常な状況）
                    String currentUrl = String.format(SCORE_URL, gameId, index);
                    log.warn("打席マッチング失敗（代打交代等の可能性）: gameId={}, idx={}, team={}, Yahoo打者={}(ID={}), Yahoo投手={}(ID={}), URL={}",
                            gameId, index, attackingTeam, 
                            bat.getPlayerNm(), bat.getPlayerId(), 
                            pit.getPlayerNm(), pit.getPlayerId(), 
                            currentUrl);
                    
                    log.warn("検索条件: batterId={}, pitcherId={} での完全一致検索を実行",
                            bat.getPlayerId(), pit.getPlayerId());
                    
                    // 残りの打席候補を詳細ログで出力
                    log.warn("残り打席候補 {} 件:", atBats.size());
                    for (int i = 0; i < Math.min(atBats.size(), 5); i++) { // 最大5件まで表示
                        AtBatResult candidate = atBats.get(i);
                        try {
                            BaseballPlayer candidateBatter = baseballPlayerService.findById(candidate.getBatterId());
                            BaseballPlayer candidatePitcher = baseballPlayerService.findById(candidate.getPitcherId());
                            log.warn("  候補{}: atBatId={}, batter={}, pitcher={}",
                                    i + 1, candidate.getAtBatId(),
                                    candidateBatter != null ? candidateBatter.getPlayerNm() : "Unknown(" + candidate.getBatterId() + ")",
                                    candidatePitcher != null ? candidatePitcher.getPlayerNm() : "Unknown(" + candidate.getPitcherId() + ")");
                        } catch (Exception e) {
                            log.warn("  候補{}: atBatId={}, batterId={}, pitcherId={} (選手情報取得エラー)",
                                    i + 1, candidate.getAtBatId(), candidate.getBatterId(), candidate.getPitcherId());
                        }
                    }
                    // 次の打席に進む（代打交代等は正常な状況のため継続処理）
                    Document tmp = connectSafely(String.format(SCORE_URL, gameId, index));
                    index = getNextIndex(tmp);
                    continue;
                }

                /* ---- 投球詳細を保存 ---- */
                Element section = doc.select("section.bb-splits__item").get(1);
                prList.addAll(parsePitchRow(section, currentAB.getAtBatId()));

            } catch (Exception ex) {
                log.error("scrapeGame error: gameId={}, index={}, pitchCount={}", gameId, index, pitchCount, ex);
                throw ex;  // 上位のscrapeGameWithRetryでリトライさせる
            }
            Document tmp = connectSafely(String.format(SCORE_URL, gameId, index));
            index = getNextIndex(tmp);
        }
        
        if (!prList.isEmpty()) pitchResultService.saveAll(prList);

        // 敬遠四球を除外（Yahoo!に投球データが存在しないため）
        List<AtBatResult> intentionalWalks = new ArrayList<>();
        atBats.removeIf(ab -> {
            if (ab.getResult() != null && ab.getResult().contains("敬遠")) {
                intentionalWalks.add(ab);
                return true;
            }
            return false;
        });

        if (!intentionalWalks.isEmpty()) {
            log.info("敬遠四球をスキップ: {} 件（Yahoo!に投球詳細データが存在しないため）", intentionalWalks.size());
        }

        if (!atBats.isEmpty()) {
            log.error("⚠️⚠️⚠️ gameId={} : 本当の未処理 atBat={} 件（敬遠四球を除く）", gameId, atBats.size());
            log.error("未処理打席の詳細情報:");

            // 未処理打席の詳細情報をログ出力
            int count = 1;
            for (AtBatResult ab : atBats) {
                try {
                    BaseballPlayer batter = baseballPlayerService.findById(ab.getBatterId());
                    BaseballPlayer pitcher = baseballPlayerService.findById(ab.getPitcherId());

                    String batterInfo = batter != null ?
                        String.format("%s(ID=%d)", batter.getPlayerNm(), batter.getPlayerId()) :
                        "Unknown(ID=" + ab.getBatterId() + ")";
                    String pitcherInfo = pitcher != null ?
                        String.format("%s(ID=%d)", pitcher.getPlayerNm(), pitcher.getPlayerId()) :
                        "Unknown(ID=" + ab.getPitcherId() + ")";

                    // Yahoo IDの有無もチェック
                    String batterYahooInfo = (batter != null && batter.getYahooId() != null) ?
                        "YahooID=" + batter.getYahooId() : "YahooID未登録";
                    String pitcherYahooInfo = (pitcher != null && pitcher.getYahooId() != null) ?
                        "YahooID=" + pitcher.getYahooId() : "YahooID未登録";

                    log.error("  未処理{}:  atBatId={}, 結果={}, 打者={}[{}], 投手={}[{}]",
                            count, ab.getAtBatId(), ab.getResult(),
                            batterInfo, batterYahooInfo,
                            pitcherInfo, pitcherYahooInfo);
                } catch (Exception e) {
                    log.error("  未処理{}: atBatId={}, batterId={}, pitcherId={} (選手情報取得エラー: {})",
                            count, ab.getAtBatId(), ab.getBatterId(), ab.getPitcherId(), e.getMessage());
                }
                count++;
            }

            log.error("推測される未処理の理由:");
            log.error("  1. Yahoo!に投球詳細データが存在しない（代打交代等）");
            log.error("  2. 選手のYahoo IDが未登録でマッチングに失敗");
            log.error("  3. Yahoo!とNPBのデータに不一致がある");
        }
    }

    /* =================================================
     *  NETWORK UTILITIES
     * ================================================= */

    private Document connectSafely(String url) throws IOException {
        requestCount++;
        
        String userAgent = USER_AGENTS[random.nextInt(USER_AGENTS.length)];
        
        log.debug("Request #{}: {}", requestCount, url);
        
        try {
            return Jsoup.connect(url)
                    .userAgent(userAgent)
                    .referrer("https://baseball.yahoo.co.jp/")
                    .timeout(CONNECTION_TIMEOUT)
                    .get();
        } catch (HttpStatusException e) {
            if (e.getStatusCode() == 429) {
                log.warn("レート制限検出 - 緊急待機");
                safeSleep(EMERGENCY_SLEEP_MIN, EMERGENCY_SLEEP_MAX); // 3-5分待機
                throw e;
            }
            throw e;
        }
    }

    /**
     * レート制限回避のためのランダム待機
     * @param minMs 最小待機時間(ミリ秒)
     * @param maxMs 最大待機時間(ミリ秒)
     * @throws RuntimeException 待機中に中断された場合
     */
    private void safeSleep(int minMs, int maxMs) {
        try {
            int sleepTime = minMs + random.nextInt(maxMs - minMs + 1);
            log.debug("待機中: {}ms", sleepTime);
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("待機中断", e);
        }
    }

    private AtBatResult findAndPopAtBat(List<AtBatResult> list, Long batterId, Long pitcherId) {
        for (Iterator<AtBatResult> it = list.iterator(); it.hasNext();) {
            AtBatResult ab = it.next();
            if (ab.getBatterId().equals(batterId) && ab.getPitcherId().equals(pitcherId)) {
                it.remove();
                return ab;
            }
        }
        return null;
    }
    
    // 打者のみマッチング機能は削除 - 投手・打者の完全一致のみをサポート

    private List<PitchResult> parsePitchRow(Element section, Long atBatId) {

        Map<Integer, Integer> courseMap = buildCourseMap(section);
        Element pitchTable = section.selectFirst("table.bb-splitsTable:has(th:matchesOwn(投球数))");
        if (pitchTable == null) return Collections.emptyList();

        List<PitchResult> list = new ArrayList<>();
        
        // 投球テーブルから最初の球番号を取得してオフセットを計算
        Elements tableRows = pitchTable.select("tbody tr");
        if (tableRows.isEmpty()) return Collections.emptyList();
        
        int firstPitchNo = -1;
        for (Element tr : tableRows) {
            Elements td = tr.select("td");
            if (td.size() >= 4) {
                firstPitchNo = Integer.parseInt(td.get(1).text());
                break;
            }
        }
        
        if (firstPitchNo == -1) return Collections.emptyList();
        
        // オフセット = 投球テーブルの最初の球番号 - 1
        int pitchOffset = firstPitchNo - 1;

        for (Element tr : tableRows) {

            Elements td = tr.select("td");
            if (td.size() < 4) continue;

            int pitchNo   = Integer.parseInt(td.get(1).text());
            String type   = td.get(2).text();

            Integer spd   = tryParseKm(td.get(3).text());
            if (spd == null) spd = 0;

            String result = (td.size() >= 5 ? td.get(4) : td.get(3))
                                .text().replace("\n", " ").trim();

            // コースマップ用の球番号 = 投球テーブルの球番号 - オフセット
            int courseMapPitchNo = pitchNo - pitchOffset;
            int course = courseMap.getOrDefault(courseMapPitchNo, -1);
            
            // course値が-1の場合のデバッグログ（必要に応じて有効化）
            // if (course == -1) {
            //     log.debug("球{}: コースマップに存在しません - courseMapPitchNo={}, courseMap={}, atBatId={}", 
            //             pitchNo, courseMapPitchNo, courseMap, atBatId);
            // }

            PitchResult pr = new PitchResult(null, atBatId, type, course,
                                             result, spd,
                                             LocalDateTime.now(), LocalDateTime.now());
            list.add(pr);
        }
        return list;
    }
  
    /**
     * HTML セクションから投球コースマップを構築。
     * Yahoo!の.bb-allocationChart内のボールアイコンの座標から5x5グリッドのゾーン番号(1-25)を算出。
     *
     * @param section 1打席分のHTML section要素
     * @return 球番号→ゾーン番号のマッピング。コースチャートがない場合は空マップ
     */
    public Map<Integer, Integer> buildCourseMap(Element section) {
        // 「詳しい投球内容」テーブル内のチャート（td.bb-splitsTable__allocationChartBg配下）を優先選択。
        // リプレイ用の小さい図（座標系が異なる）を取得しないようにする。
        Element chart = section.selectFirst("td[class*=allocationChartBg] .bb-allocationChart");
        if (chart == null) {
            // フォールバック: 任意の.bb-allocationChartを使用
            chart = section.selectFirst(".bb-allocationChart");
        }
        if (chart == null) {
            return Collections.emptyMap();
        }

        boolean isLeftBatter = isLeftBatter(chart);
        Map<Integer, Integer> result = new HashMap<>();

        for (Element span : chart.select("span.bb-icon__ballCircle")) {
            Matcher m = STYLE_PATTERN.matcher(span.attr("style"));
            if (!m.find()) continue;

            // 小数点座標にも対応
            double top = Double.parseDouble(m.group(1));
            double left = Double.parseDouble(m.group(2));

            // 1) アイコン中心座標
            double cx = left + BALL_RADIUS;
            double cy = top + BALL_RADIUS;

            // 2) 行・列 (0〜4)
            int col = (int) ((cx - BORDER_PX) / CELL_W);
            int row = (int) ((cy - BORDER_PX) / CELL_H);

            // 3) マイナス座標や範囲外の処理
            if (top < 0) {
                // マイナス座標の場合は最上段（row=0）に配置
                row = 0;
            }
            if (left < 0) {
                // マイナス座標の場合は最左列（col=0）に配置
                col = 0;
            }

            col = clamp(col, 0, GRID_COLS - 1);
            row = clamp(row, 0, GRID_ROWS - 1);

            // 4) 打者目線でのゾーン番号計算
            // Yahoo!の座標系はそのまま使用（反転処理は不要）
            int zone = row * GRID_COLS + col + 1;

            // 5) 球番号と紐付け
            int pitchNo = Integer.parseInt(span.selectFirst(".bb-icon__number").text());
            result.put(pitchNo, zone);
        }
        return result;
    }

    /* =================================================
     *  PRIVATE HELPER METHODS
     * ================================================= */

    /** 左打者判定（クラス名に --leftBatter or --left があれば左打ち） */
    private boolean isLeftBatter(Element chart) {
        String cls = chart.closest("td").className();
        return cls.contains("--leftBatter") || cls.contains("--left");
    }

    /** 値を指定範囲にクランプ */
    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
    
    /**
     * テスト用：座標からゾーン番号を直接計算
     * @param top Y座標(ピクセル)
     * @param left X座標(ピクセル)
     * @param isLeftBatter 左打者かどうか
     * @return ゾーン番号(1-25)
     */
    public int calculateZone(int top, int left, boolean isLeftBatter) {
        double cx = left + BALL_RADIUS;
        double cy = top + BALL_RADIUS;
        
        int col = (int) ((cx - BORDER_PX) / CELL_W);
        int row = (int) ((cy - BORDER_PX) / CELL_H);
        col = clamp(col, 0, GRID_COLS - 1);
        row = clamp(row, 0, GRID_ROWS - 1);
        
        if (!isLeftBatter) col = GRID_COLS - 1 - col;
        
        return row * GRID_COLS + col + 1;
    }
    
    private BaseballGame resolveGameFromYahoo(String gameId) throws IOException {

        String url = String.format(GAME_TOP_URL, gameId);

        Document doc = connectSafely(url);
        
        /* ---------- 1) 試合日 ---------- */
        String title = doc.title();

        /* 1-A 日付 */
        Matcher t = GAME_DATE_PATTERN.matcher(title);
        if (!t.find()) throw new IllegalStateException("日付が取れません: " + title);

        LocalDate localDate = LocalDate.of(
                Integer.parseInt(t.group(1)),
                Integer.parseInt(t.group(2)),
                Integer.parseInt(t.group(3)));
        Date gameDate = java.sql.Date.valueOf(localDate);

        /* 1-B チーム名  */
        String tail = title.substring(t.end()).trim();
        String[] parts = tail.split("vs\\.|vs");
        if (parts.length < 2)
            throw new IllegalStateException("タイトルからチームが切れません: " + title);

        long homeId = convTeam(parts[0].trim());
        long awayId = convTeam(parts[1].split(" - ")[0].trim());

        if (homeId == 0 || awayId == 0)
            throw new IllegalStateException("チーム変換失敗: " + parts[0] + " / " + parts[1]);

        /* ---------- 3) DB へ問い合わせ ---------- */
        List<BaseballGame> list =
            baseballGameService.findByGameDateAndTeamId(gameDate, homeId, awayId);

        if (list.isEmpty()) {
            throw new IllegalStateException(
                "試合テーブルに存在しません : " + gameDate + " home=" + homeId + " away=" + awayId);
        }
        if (list.size() > 1) {
            throw new IllegalStateException(
                "複数ヒットしました : " + gameDate + " home=" + homeId + " away=" + awayId);
        }
        return list.get(0);
    }

    private String fetchStartIndex(String gameId) throws IOException {
        String url = YAHOO_BASE_URL + "/npb/game/" + gameId + "/score";
        log.info("fetchStartIndex: URL={}", url);

        Document doc = connectSafely(url);
        log.info("fetchStartIndex: Document取得成功");

        // 方法1: 新しいHTML構造に対応（bb-gameScoreTable__scoreクラスから取得）
        Elements scoreLinks = doc.select("a.bb-gameScoreTable__score[href*=index]");
        if (!scoreLinks.isEmpty()) {
            Element firstLink = scoreLinks.first();
            String href = firstLink.attr("href");
            // href="/npb/game/2021029040/score?index=0110100" から index を抽出
            Pattern indexPattern = Pattern.compile("index=(\\d+)");
            Matcher matcher = indexPattern.matcher(href);
            if (matcher.find()) {
                String index = matcher.group(1);
                log.info("fetchStartIndex: 成功（新方式） - index={}", index);
                return index;
            }
        }

        // 方法2: 古いHTML構造（フォールバック）
        Element a = doc.selectFirst("a#inn_score[index]");
        if (a != null) {
            String index = a.attr("index");
            log.info("fetchStartIndex: 成功（旧方式） - index={}", index);
            return index;
        }

        // どちらの方法でも取得できなかった場合
        log.warn("fetchStartIndex: index取得失敗");
        log.warn("デバッグ: bb-gameScoreTable__scoreリンク数={}", scoreLinks.size());

        // デバッグ: index属性を持つ要素を探す
        Elements indexElements = doc.select("[index]");
        log.warn("デバッグ: [index]属性を持つ要素数={}", indexElements.size());
        for (int i = 0; i < Math.min(indexElements.size(), 3); i++) {
            Element elem = indexElements.get(i);
            log.warn("  要素{}: tag={}, id={}, class={}, index={}",
                    i + 1, elem.tagName(), elem.id(), elem.className(), elem.attr("index"));
        }

        return null;
    }
    
    /**
     * 次の打席のindexを取得
     */
    private String getNextIndex(Document doc) {
        Element next = doc.selectFirst("a#btn_next[index]");
        return (next != null) ? next.attr("index") : null;
    }
    
    /**
     * 現在攻撃中のチームを取得
     * @param doc YahooスコアページのDocument
     * @return チーム名（例：「楽天」、「阪神」）
     */
    private String extractAttackingTeam(Document doc) {
        try {
            // "○○攻撃中" パターンでチーム名を抽出
            Elements attackElements = doc.select("p:contains(攻撃中)");
            for (Element elem : attackElements) {
                String text = elem.text(); // 例: "楽天攻撃中"
                if (text.endsWith("攻撃中")) {
                    String teamName = text.replace("攻撃中", ""); // "楽天"
                    log.debug("攻撃チーム: {}", teamName);
                    return teamName;
                }
            }
            
            // フォールバック: leftBox内のテキストから抽出
            Element leftBox = doc.selectFirst(".leftBox");
            if (leftBox != null) {
                String text = leftBox.text();
                if (text.contains("攻撃中")) {
                    // "楽天攻撃中 打者６ 前へ 次へ" -> "楽天"
                    String[] parts = text.split("攻撃中");
                    if (parts.length > 0) {
                        return parts[0].trim();
                    }
                }
            }
            
            log.warn("攻撃チームの取得に失敗: HTML構造が変更された可能性");
            return "UNKNOWN";
            
        } catch (Exception e) {
            log.warn("攻撃チーム抽出エラー", e);
            return "UNKNOWN";
        }
    }

    private long extractPlayerId(Document doc, boolean wantPitcher) {

        Element gm = doc.selectFirst("table#gm_rslt");
        if (gm == null) return 0;

        Elements heads  = gm.select("thead th");
        boolean firstIsPitcher = heads.first().text().contains("投手");

        int anchorIdx = (wantPitcher ^ firstIsPitcher) ? 1 : 0;

        Elements anchors = gm.select("tbody tr td a");
        if (anchors.size() <= anchorIdx) return 0;

        Matcher m = PLAYER_ID.matcher(anchors.get(anchorIdx).attr("href"));
        return m.find() ? Long.parseLong(m.group(1)) : 0;
    }

    private int parsePx(String style, String prop) {
        Matcher m = Pattern.compile(prop + "\\s*:\\s*(\\d+)px").matcher(style);
        return m.find() ? Integer.parseInt(m.group(1)) : -1;
    }
    
    private BaseballPlayer resolvePlayer(long yahooId) throws IOException {

        BaseballPlayer bp = baseballPlayerService.findByYahooId(yahooId);
        if (bp != null) return bp;

        // 選手情報取得前に待機
        safeSleep(MIN_PLAYER_INTERVAL, MAX_PLAYER_INTERVAL);
        
        PlayerProfile prof = fetchPlayerProfileFromYahoo(yahooId);

        // まず身長・体重を含めた精密検索を試行
        bp = baseballPlayerService.findByPlayerProfileWithPhysical(
                prof.getName(), prof.getBirthDate(), prof.getHeight(), prof.getWeight());
        
        // 精密検索で見つからない場合は名前と生年月日のみで検索
        if (bp == null) {
            bp = baseballPlayerService.findByPlayerNmAndBirthDateByYahooNm(
                    prof.getName(), prof.getBirthDate());
            
            if (bp != null) {
                log.info("物理情報ミスマッチでも名前・生年月日で登録: yahooId={}, name={}, "
                    + "Yahoo(身長={}cm, 体重={}kg) vs DB(身長={}cm, 体重={}kg)", 
                    yahooId, prof.getName(), prof.getHeight(), prof.getWeight(), 
                    bp.getHeight(), bp.getWeight());
            }
        }

        if (bp == null) {
            throw new IllegalStateException(
                    "BaseballPlayer に未登録: yahooId=" + yahooId + ", name=" + prof.getName() 
                    + ", height=" + prof.getHeight() + "cm, weight=" + prof.getWeight() + "kg");
        }

        bp.setYahooId(yahooId);
        baseballPlayerService.savePlayer(bp);
        return bp;
    }
    
    private PlayerProfile fetchPlayerProfileFromYahoo(long yahooId) throws IOException {

        String url = String.format(PLAYER_URL, yahooId);
        try {
            Document doc = connectSafely(url);

            String fullName = doc.selectFirst("ruby.bb-profile__ruby").text();
            String lastName;
            if (fullName.contains(" ")) {
                lastName = fullName.split(" ")[0]; // 姓のみ取得
            } else if (fullName.contains("　")) {
                lastName = fullName.split("　")[0]; // 全角スペースの場合
            } else {
                lastName = fullName; // スペースがない場合はそのまま
            }

            Elements dds = doc.select("dl.bb-profile__list dd.bb-profile__text");
            if (dds.size() < 4) throw new IllegalStateException("プロフィール情報が不足: " + url);

            // 生年月日の取得
            String birthTxt = dds.get(1).text();
            Matcher m = BIRTH_DATE_PATTERN.matcher(birthTxt);
            if (!m.find())
                throw new IllegalStateException("生年月日パース失敗: " + birthTxt);

            LocalDate birth = LocalDate.of(
                Integer.parseInt(m.group(1)),
                Integer.parseInt(m.group(2)),
                Integer.parseInt(m.group(3))
            );

            // 身長・体重の取得
            String heightTxt = dds.get(2).text(); // "172cm"
            String weightTxt = dds.get(3).text(); // "67kg"
            
            Integer height = null;
            Integer weight = null;
            
            try {
                height = Integer.parseInt(heightTxt.replace("cm", ""));
            } catch (Exception e) {
                log.warn("身長パース失敗: {} (yahooId={})", heightTxt, yahooId);
            }
            
            try {
                weight = Integer.parseInt(weightTxt.replace("kg", ""));
            } catch (Exception e) {
                log.warn("体重パース失敗: {} (yahooId={})", weightTxt, yahooId);
            }

            return new PlayerProfile(lastName, birth, height, weight);
        } catch (Exception e) {
            log.error("選手ページ取得失敗: yahooId={}, url={}", yahooId, url, e);
            throw e;
        }
    }
    
    /**
     * チーム名からチームIDへ変換
     * @param team チーム名
     * @return チームID（見つからない場合は0）
     */
    private int convTeam(String team) {
        if (team == null || team.trim().isEmpty()) {
            return 0;
        }
        
        // チーム名でマッチング
        if (team.contains("西武") || team.contains("西　武")) return 9;  // 西武ライオンズ
        if (team.contains("ソフトバンク")) return 7;                    // ソフトバンクホークス
        if (team.contains("日本ハム")) return 8;                        // 北海道日本ハムファイターズ
        if (team.contains("ロッテ")) return 11;                           // 千葉ロッテマリーンズ
        if (team.contains("オリックス")) return 10;                      // オリックスバファローズ
        if (team.contains("楽天")) return 12;                            // 東北楽天ゴールデンイーグルス
        if (team.contains("中日")) return 6;                             // 中日ドラゴンズ
        if (team.contains("ヤクルト")) return 1;                        // 東京ヤクルトスワローズ
        if (team.contains("読売") || team.contains("巨人")) return 2;    // 読売ジャイアンツ
        if (team.contains("阪神")) return 4;                             // 阪神タイガース
        if (team.contains("広島")) return 5;                             // 広島東洋カープ
        if (team.contains("横浜") || team.contains("横　浜") || team.contains("DeNA")) return 3; // 横浜DeNAベイスターズ
        
        log.warn("未知のチーム名: {}", team);
        return 0;
    }

    private Integer tryParseKm(String s) {
        try {
            return Integer.parseInt(s.replace("km/h", ""));
        } catch (Exception e) {
            return null;
        }
    }

    @RequiredArgsConstructor @Getter
    private static class PlayerProfile {
        private final String name;
        private final LocalDate birthDate;
        private final Integer height; // cm
        private final Integer weight; // kg
    }
}