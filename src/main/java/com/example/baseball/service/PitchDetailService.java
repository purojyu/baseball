package com.example.baseball.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.baseball.entity.BaseballPlayer;
import com.example.baseball.entity.PitchResult;
import com.example.baseball.entity.VAtBatGameDetails;
import com.example.baseball.repository.PitchResultRepository;
import com.example.baseball.util.BaseballUtil;

/**
 * 投手vs打者の詳細投球データを集計するサービス
 * コース別打率、球種別打率、打席ログ、年度別成績、SPLITS等を計算
 */
@Service
public class PitchDetailService {

    private static final Set<String> HIT_RESULTS = Set.of("安", "２", "３", "本");
    private static final Set<String> NON_AB_RESULTS = Set.of("四球", "死球", "犠打", "犠飛", "敬遠", "妨害");
    // ボールカウント加算対象
    private static final Set<String> BALL_RESULTS = Set.of("ボール");
    // ストライクカウント加算対象（見逃し・空振り含む）
    private static final Set<String> STRIKE_RESULTS = Set.of("ストライク", "見逃し", "空振り", "ファウル");

    @Autowired
    PitchResultRepository pitchResultRepository;

    @Autowired
    BaseballPlayerService baseballPlayerService;

    /**
     * 投手vs打者の詳細データを生成
     */
    public Map<String, Object> buildPitchDetail(List<VAtBatGameDetails> atBatResults) {
        Map<String, Object> result = new HashMap<>();

        List<Long> atBatIds = atBatResults.stream()
                .map(VAtBatGameDetails::getAtBatId)
                .collect(Collectors.toList());

        // 空リストの場合はIN句でSQLエラーになるため空リストを返す
        List<PitchResult> pitchResults = atBatIds.isEmpty()
                ? List.of()
                : pitchResultRepository.findByAtBatIdIn(atBatIds);

        // 打席IDごとの投球データマップ（pitchId順にソート済み）
        Map<Long, List<PitchResult>> pitchByAtBat = pitchResults.stream()
                .sorted(Comparator.comparing(PitchResult::getPitchId))
                .collect(Collectors.groupingBy(PitchResult::getAtBatId));

        // 打席IDごとのVAtBatGameDetailsマップ
        Map<Long, VAtBatGameDetails> atBatMap = atBatResults.stream()
                .collect(Collectors.toMap(VAtBatGameDetails::getAtBatId, v -> v, (a, b) -> a));

        result.put("summary", buildSummary(atBatResults));
        result.put("courseStats", buildCourseStats(pitchResults, pitchByAtBat, atBatMap));
        result.put("pitchTypeStats", buildPitchTypeStats(pitchResults, pitchByAtBat, atBatMap));
        result.put("atBatLog", buildAtBatLog(atBatResults, pitchByAtBat));
        result.put("yearlyStats", buildYearlyStats(atBatResults));
        result.put("splits", buildSplits(atBatResults));
        result.put("playerInfo", buildPlayerInfo(atBatResults));

        return result;
    }

    /**
     * 任意の打席集合に対してコース別(5×5ゾーン)成績だけを算出する。
     *
     * 選手プロフィールで「対左/対右」などにフィルタした打席集合から
     * ゾーン別成績を再計算するための公開ヘルパ。投球データを取得し直し、
     * {@link #buildCourseStats} を再利用する。空リストなら全25ゾーンを ab=0 で返す。
     */
    public List<Map<String, Object>> buildCourseStatsForAtBats(List<VAtBatGameDetails> atBatResults) {
        List<Long> atBatIds = atBatResults.stream()
                .map(VAtBatGameDetails::getAtBatId)
                .collect(Collectors.toList());

        List<PitchResult> pitchResults = atBatIds.isEmpty()
                ? List.of()
                : pitchResultRepository.findByAtBatIdIn(atBatIds);

        Map<Long, List<PitchResult>> pitchByAtBat = pitchResults.stream()
                .sorted(Comparator.comparing(PitchResult::getPitchId))
                .collect(Collectors.groupingBy(PitchResult::getAtBatId));

        Map<Long, VAtBatGameDetails> atBatMap = atBatResults.stream()
                .collect(Collectors.toMap(VAtBatGameDetails::getAtBatId, v -> v, (a, b) -> a));

        return buildCourseStats(pitchResults, pitchByAtBat, atBatMap);
    }

    /**
     * SUMMARY: 打席数、打数、安打、打率、OPS等
     */
    private Map<String, Object> buildSummary(List<VAtBatGameDetails> atBatResults) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("pa", atBatResults.size());
        summary.put("ab", BaseballUtil.calculateStrokesNumber(atBatResults));
        summary.put("h", BaseballUtil.calculateHitNumber(atBatResults));
        summary.put("doubles", BaseballUtil.calculateDoublesNumber(atBatResults));
        summary.put("triples", BaseballUtil.calculateTriplesNumber(atBatResults));
        summary.put("hr", BaseballUtil.calculateHomeRun(atBatResults));
        summary.put("rbi", 0); // RBIは打席結果からは取得不可
        summary.put("so", BaseballUtil.calculateStrikeoutsNumber(atBatResults));
        summary.put("bb", BaseballUtil.calculateFourBallNumber(atBatResults));
        summary.put("ba", BaseballUtil.calculateBattingAverage(atBatResults));
        summary.put("obp", BaseballUtil.calculateOnBasePercentage(atBatResults));
        summary.put("slg", BaseballUtil.calculateSluggingPercentage(atBatResults));
        summary.put("ops", BaseballUtil.calculateOps(atBatResults));
        return summary;
    }

    /**
     * コース別打率: 5×5ゾーン (course 1-25)
     * 各ゾーンの打率と打数を返す
     */
    private List<Map<String, Object>> buildCourseStats(
            List<PitchResult> allPitches,
            Map<Long, List<PitchResult>> pitchByAtBat,
            Map<Long, VAtBatGameDetails> atBatMap) {

        // 各ゾーンごとに「最終球がそのゾーンだった打席」の打率を計算
        Map<Integer, List<String>> courseToResults = new HashMap<>();

        for (Map.Entry<Long, List<PitchResult>> entry : pitchByAtBat.entrySet()) {
            Long atBatId = entry.getKey();
            List<PitchResult> pitches = entry.getValue();
            VAtBatGameDetails atBat = atBatMap.get(atBatId);

            if (atBat == null || pitches.isEmpty()) continue;
            // 四球・死球等は打数に含まない
            if (NON_AB_RESULTS.stream().anyMatch(r -> atBat.getResult().contains(r))) continue;

            // 最終球のコースを使用
            PitchResult lastPitch = pitches.get(pitches.size() - 1);
            Integer course = lastPitch.getCourse();
            if (course == null || course < 1 || course > 25) continue;
            courseToResults.computeIfAbsent(course, k -> new ArrayList<>()).add(atBat.getResult());
        }

        List<Map<String, Object>> courseStats = new ArrayList<>();
        for (int zone = 1; zone <= 25; zone++) {
            Map<String, Object> stat = new HashMap<>();
            stat.put("zone", zone);
            List<String> results = courseToResults.getOrDefault(zone, List.of());
            int ab = results.size();
            long hits = results.stream().filter(r -> HIT_RESULTS.stream().anyMatch(r::contains)).count();
            stat.put("ab", ab);
            stat.put("h", hits);
            stat.put("avg", ab > 0 ? new BigDecimal(hits).divide(new BigDecimal(ab), 3, RoundingMode.HALF_UP) : null);
            courseStats.add(stat);
        }
        return courseStats;
    }

    /**
     * 球種別打率: 球種ごとの打数、安打数、打率
     */
    private List<Map<String, Object>> buildPitchTypeStats(
            List<PitchResult> allPitches,
            Map<Long, List<PitchResult>> pitchByAtBat,
            Map<Long, VAtBatGameDetails> atBatMap) {

        // 最終球の球種ごとに打率を計算
        Map<String, List<String>> typeToResults = new LinkedHashMap<>();

        for (Map.Entry<Long, List<PitchResult>> entry : pitchByAtBat.entrySet()) {
            Long atBatId = entry.getKey();
            List<PitchResult> pitches = entry.getValue();
            VAtBatGameDetails atBat = atBatMap.get(atBatId);

            if (atBat == null || pitches.isEmpty()) continue;
            if (NON_AB_RESULTS.stream().anyMatch(r -> atBat.getResult().contains(r))) continue;

            PitchResult lastPitch = pitches.get(pitches.size() - 1);
            typeToResults.computeIfAbsent(lastPitch.getPitchType(), k -> new ArrayList<>()).add(atBat.getResult());
        }

        return typeToResults.entrySet().stream()
                .sorted((a, b) -> b.getValue().size() - a.getValue().size())
                .map(entry -> {
                    Map<String, Object> stat = new HashMap<>();
                    stat.put("pitchType", entry.getKey());
                    int ab = entry.getValue().size();
                    long hits = entry.getValue().stream().filter(r -> HIT_RESULTS.stream().anyMatch(r::contains)).count();
                    stat.put("ab", ab);
                    stat.put("h", hits);
                    stat.put("avg", ab > 0 ? new BigDecimal(hits).divide(new BigDecimal(ab), 3, RoundingMode.HALF_UP) : BigDecimal.ZERO);
                    return stat;
                })
                .collect(Collectors.toList());
    }

    /**
     * 打席ログ: 各打席の詳細（日付、結果、配球情報）
     */
    private List<Map<String, Object>> buildAtBatLog(
            List<VAtBatGameDetails> atBatResults,
            Map<Long, List<PitchResult>> pitchByAtBat) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");

        return atBatResults.stream()
                .sorted(Comparator.comparing(VAtBatGameDetails::getGameDate).reversed()
                        .thenComparing(Comparator.comparing(VAtBatGameDetails::getAtBatId).reversed()))
                .map(atBat -> {
                    Map<String, Object> log = new LinkedHashMap<>();
                    log.put("date", sdf.format(atBat.getGameDate()));
                    log.put("result", atBat.getResult());
                    log.put("stadium", atBat.getStadium());

                    // 投球詳細
                    List<PitchResult> pitches = pitchByAtBat.getOrDefault(atBat.getAtBatId(), List.of());
                    int balls = 0;
                    int strikes = 0;
                    List<Map<String, Object>> pitchDetails = new ArrayList<>();
                    for (PitchResult p : pitches) {
                        Map<String, Object> pd = new HashMap<>();
                        pd.put("pitchType", p.getPitchType());
                        pd.put("speed", p.getVelocity());
                        pd.put("course", p.getCourse());
                        pd.put("result", p.getResult());
                        pd.put("balls", balls);
                        pd.put("strikes", strikes);

                        // カウント更新
                        String pitchResult = p.getResult();
                        boolean isBall = BALL_RESULTS.stream().anyMatch(pitchResult::contains);
                        boolean isFoul = pitchResult.contains("ファウル");
                        boolean isStrike = STRIKE_RESULTS.stream().anyMatch(pitchResult::contains);

                        if (isBall) {
                            balls = Math.min(balls + 1, 3);
                        } else if (isStrike) {
                            // ファウルは2ストライク後はカウント加算しない
                            if (!(isFoul && strikes >= 2)) {
                                strikes = Math.min(strikes + 1, 2);
                            }
                        }
                        pitchDetails.add(pd);
                    }
                    log.put("pitchCount", pitches.size());
                    log.put("pitches", pitchDetails);

                    // 最終球の情報
                    if (!pitches.isEmpty()) {
                        PitchResult lastPitch = pitches.get(pitches.size() - 1);
                        log.put("lastPitchType", lastPitch.getPitchType());
                        log.put("lastPitchSpeed", lastPitch.getVelocity());
                    }

                    return log;
                })
                .collect(Collectors.toList());
    }

    /**
     * 年度別成績
     */
    @SuppressWarnings("deprecation")
    private List<Map<String, Object>> buildYearlyStats(List<VAtBatGameDetails> atBatResults) {
        Map<Integer, List<VAtBatGameDetails>> byYear = atBatResults.stream()
                .collect(Collectors.groupingBy(v -> v.getGameDate().getYear() + 1900));

        return byYear.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(entry -> {
                    Map<String, Object> stat = new LinkedHashMap<>();
                    List<VAtBatGameDetails> yearResults = entry.getValue();
                    stat.put("year", entry.getKey());
                    stat.put("pa", yearResults.size());
                    stat.put("ab", BaseballUtil.calculateStrokesNumber(yearResults));
                    stat.put("h", BaseballUtil.calculateHitNumber(yearResults));
                    stat.put("hr", BaseballUtil.calculateHomeRun(yearResults));
                    stat.put("so", BaseballUtil.calculateStrikeoutsNumber(yearResults));
                    stat.put("bb", BaseballUtil.calculateFourBallNumber(yearResults));
                    stat.put("ba", BaseballUtil.calculateBattingAverage(yearResults));
                    return stat;
                })
                .collect(Collectors.toList());
    }

    /**
     * SPLITS: ホーム/ビジター、得点圏等
     */
    private Map<String, Object> buildSplits(List<VAtBatGameDetails> atBatResults) {
        if (atBatResults.isEmpty()) return Map.of();

        Map<String, Object> splits = new LinkedHashMap<>();
        Long pitcherId = atBatResults.get(0).getPitcherId();

        // ホーム: 投手のチームがホームチーム
        List<VAtBatGameDetails> home = atBatResults.stream()
                .filter(v -> v.getHomeTeamId().equals(v.getPitcherTeamId()))
                .collect(Collectors.toList());
        List<VAtBatGameDetails> away = atBatResults.stream()
                .filter(v -> !v.getHomeTeamId().equals(v.getPitcherTeamId()))
                .collect(Collectors.toList());

        splits.put("home", buildSplitLine(home));
        splits.put("away", buildSplitLine(away));

        return splits;
    }

    private Map<String, Object> buildSplitLine(List<VAtBatGameDetails> results) {
        Map<String, Object> line = new HashMap<>();
        if (results.isEmpty()) {
            line.put("ba", null);
            line.put("summary", "-");
            return line;
        }
        int ab = BaseballUtil.calculateStrokesNumber(results);
        int h = BaseballUtil.calculateHitNumber(results);
        int hr = BaseballUtil.calculateHomeRun(results);
        BigDecimal ba = BaseballUtil.calculateBattingAverage(results);
        line.put("ba", ba);
        line.put("ab", ab);
        line.put("h", h);
        line.put("hr", hr);
        line.put("summary", String.format(".%s (%d-%d-%d)",
            ba != null ? ba.toPlainString().replace("0.", "") : "---", h, ab, hr));
        return line;
    }

    /**
     * 選手情報
     */
    private Map<String, Object> buildPlayerInfo(List<VAtBatGameDetails> atBatResults) {
        if (atBatResults.isEmpty()) return Map.of();

        VAtBatGameDetails first = atBatResults.get(0);
        Map<String, Object> info = new LinkedHashMap<>();

        info.put("pitcherId", first.getPitcherId());
        info.put("pitcherNm", first.getPitcherNm());
        info.put("pitcherTeamNm", first.getPitcherTeamShortNm());
        info.put("pitcherNpbUrl", first.getPitcherNpbUrl());

        info.put("batterId", first.getBatterId());
        info.put("batterNm", first.getBatterNm());
        info.put("batterTeamNm", first.getBatterTeamShortNm());
        info.put("batterNpbUrl", first.getBatterNpbUrl());

        BaseballPlayer batter = baseballPlayerService.findById(first.getBatterId());
        info.put("batterHanded", batter != null ? batter.getHanded() : null);

        return info;
    }
}
