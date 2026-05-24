package com.example.baseball.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.baseball.entity.BaseballPlayer;
import com.example.baseball.entity.VAtBatGameDetails;
import com.example.baseball.repository.BaseballPlayerRepository;
import com.example.baseball.repository.VAtBatGameDetailsRepository;
import com.example.baseball.util.BaseballUtil;

/**
 * 1選手の全打席を集計して「選手プロフィール」を構築するサービス。
 *
 * 既存 {@link PitchDetailService#buildPitchDetail(List)} を再利用して
 * summary / courseStats(ゾーン別) / pitchTypeStats / atBatLog / yearlyStats を生成し、
 * playerInfo と splits は「対象選手1人視点」に上書き、対戦相手TOP/WORSTを追加で計算する。
 *
 * 投手か打者かは position から自動判定（"P" または「投」を含む → 投手）。
 */
@Service
public class PlayerProfileService {

    /** 対戦相手TOP/WORST から除外する最小サンプル数（PA）。少数だと打率が極端になりやすいため。 */
    private static final int MIN_PA_FOR_OPPONENT_RANKING = 3;

    @Autowired
    private VAtBatGameDetailsRepository vAtBatGameDetailsRepository;

    @Autowired
    private BaseballPlayerRepository baseballPlayerRepository;

    @Autowired
    private PitchDetailService pitchDetailService;

    /**
     * 選手プロフィールを構築する。
     *
     * @param playerId 対象選手ID
     * @param year 年度フィルタ（"通算" もしくは "2026" 等）
     * @return プロフィール Map。選手が存在しない場合 null、対戦データが空の場合は最小限の playerInfo のみ含む Map を返す
     */
    public Map<String, Object> buildProfile(Long playerId, String year) {
        Optional<BaseballPlayer> playerOpt = baseballPlayerRepository.findById(playerId);
        if (playerOpt.isEmpty()) {
            return null;
        }
        BaseballPlayer player = playerOpt.get();
        boolean isPitcher = isPitcherPosition(player.getPosition());

        // 全打席取得（投手なら pitcherId=playerId、打者なら batterId=playerId で1選手分を引く）
        List<VAtBatGameDetails> atBatResults;
        if (isPitcher) {
            atBatResults = vAtBatGameDetailsRepository.findByBatterAndPitcher(
                    0L, 0L, playerId, null, year);
        } else {
            atBatResults = vAtBatGameDetailsRepository.findByBatterAndPitcher(
                    0L, 0L, null, playerId, year);
        }

        // データが空でも、選手プロフィールは最低限返す（フロント側で「データなし」表示できるように）
        if (atBatResults.isEmpty()) {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("playerInfo", buildPlayerInfo(player, isPitcher, atBatResults));
            empty.put("isPitcher", isPitcher);
            empty.put("hasData", false);
            return empty;
        }

        // 既存 buildPitchDetail で summary / courseStats / pitchTypeStats / atBatLog / yearlyStats を取得
        Map<String, Object> profile = new HashMap<>(pitchDetailService.buildPitchDetail(atBatResults));

        // 1選手視点に上書き
        profile.put("playerInfo", buildPlayerInfo(player, isPitcher, atBatResults));
        profile.put("splits", buildRoleSplits(atBatResults, isPitcher));

        // 追加: 対戦相手TOP/WORST
        profile.put("topOpponents", buildOpponents(atBatResults, isPitcher, true));
        profile.put("worstOpponents", buildOpponents(atBatResults, isPitcher, false));

        // メタ情報
        profile.put("isPitcher", isPitcher);
        profile.put("hasData", true);

        return profile;
    }

    /** position から投手判定。"P" または「投」を含むなら投手とみなす。 */
    private boolean isPitcherPosition(String position) {
        if (position == null) return false;
        String p = position.trim();
        return p.equalsIgnoreCase("P") || p.contains("投");
    }

    /** 対象選手1人分の playerInfo。直近の打席から最新所属チームを推定する。 */
    private Map<String, Object> buildPlayerInfo(BaseballPlayer player, boolean isPitcher, List<VAtBatGameDetails> atBatResults) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("playerId", player.getPlayerId());
        info.put("playerNm", player.getPlayerNm());
        info.put("playerNmKana", player.getPlayerNmKana());
        info.put("position", player.getPosition());
        info.put("handed", player.getHanded());
        info.put("thrower", player.getThrower());
        info.put("npbUrl", player.getNpbUrl());
        info.put("birthDate", player.getBirthDate());
        info.put("height", player.getHeight());
        info.put("weight", player.getWeight());
        info.put("isPitcher", isPitcher);

        // 直近所属チーム: 最新 gameDate を持つ打席のチーム
        if (!atBatResults.isEmpty()) {
            VAtBatGameDetails latest = atBatResults.stream()
                    .max(Comparator.comparing(VAtBatGameDetails::getGameDate))
                    .orElse(atBatResults.get(0));
            if (isPitcher) {
                info.put("teamId", latest.getPitcherTeamId());
                info.put("teamNm", latest.getPitcherTeamNm());
                info.put("teamShortNm", latest.getPitcherTeamShortNm());
            } else {
                info.put("teamId", latest.getBatterTeamId());
                info.put("teamNm", latest.getBatterTeamNm());
                info.put("teamShortNm", latest.getBatterTeamShortNm());
            }
        }
        return info;
    }

    /**
     * 対象選手のチームを基準に home/away を判定する SPLITS。
     * 既存 PitchDetailService の splits は投手のチーム基準だが、ここでは
     * 投手/打者どちらの選手ページかに応じて判定基準を切り替える。
     */
    private Map<String, Object> buildRoleSplits(List<VAtBatGameDetails> atBatResults, boolean isPitcher) {
        if (atBatResults.isEmpty()) return Map.of();

        Map<String, Object> splits = new LinkedHashMap<>();
        List<VAtBatGameDetails> home = atBatResults.stream()
                .filter(v -> {
                    Long teamId = isPitcher ? v.getPitcherTeamId() : v.getBatterTeamId();
                    return teamId != null && teamId.equals(v.getHomeTeamId());
                })
                .collect(Collectors.toList());
        List<VAtBatGameDetails> away = atBatResults.stream()
                .filter(v -> {
                    Long teamId = isPitcher ? v.getPitcherTeamId() : v.getBatterTeamId();
                    return teamId != null && !teamId.equals(v.getHomeTeamId());
                })
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
     * 対戦相手TOP/WORST を計算する。
     *
     * - 投手ページ: 対戦打者ごとに被打率を集計
     * - 打者ページ: 対戦投手ごとに打率を集計
     * - PA が {@link #MIN_PA_FOR_OPPONENT_RANKING} 未満の対戦は除外（少数サンプルのノイズ排除）
     * - top=true → 打率高い順5件 / top=false → 打率低い順5件
     * - 同率の場合はサンプル数(PA)の多い順で安定化
     */
    private List<Map<String, Object>> buildOpponents(
            List<VAtBatGameDetails> atBatResults, boolean isPitcher, boolean top) {

        Map<Long, List<VAtBatGameDetails>> byOpponent = atBatResults.stream()
                .collect(Collectors.groupingBy(isPitcher
                        ? VAtBatGameDetails::getBatterId
                        : VAtBatGameDetails::getPitcherId));

        List<Map<String, Object>> opponents = new ArrayList<>();
        for (Map.Entry<Long, List<VAtBatGameDetails>> entry : byOpponent.entrySet()) {
            List<VAtBatGameDetails> matches = entry.getValue();
            if (matches.size() < MIN_PA_FOR_OPPONENT_RANKING) continue;

            int ab = BaseballUtil.calculateStrokesNumber(matches);
            if (ab == 0) continue;

            BigDecimal avg = BaseballUtil.calculateBattingAverage(matches);
            VAtBatGameDetails sample = matches.get(0);

            Map<String, Object> opp = new LinkedHashMap<>();
            opp.put("opponentId", entry.getKey());
            if (isPitcher) {
                opp.put("opponentNm", sample.getBatterNm());
                opp.put("opponentTeamShortNm", sample.getBatterTeamShortNm());
                opp.put("opponentNpbUrl", sample.getBatterNpbUrl());
            } else {
                opp.put("opponentNm", sample.getPitcherNm());
                opp.put("opponentTeamShortNm", sample.getPitcherTeamShortNm());
                opp.put("opponentNpbUrl", sample.getPitcherNpbUrl());
            }
            opp.put("pa", matches.size());
            opp.put("ab", ab);
            opp.put("h", BaseballUtil.calculateHitNumber(matches));
            opp.put("hr", BaseballUtil.calculateHomeRun(matches));
            opp.put("avg", avg);
            opponents.add(opp);
        }

        opponents.sort((a, b) -> {
            BigDecimal aAvg = (BigDecimal) a.get("avg");
            BigDecimal bAvg = (BigDecimal) b.get("avg");
            int cmp = top ? bAvg.compareTo(aAvg) : aAvg.compareTo(bAvg);
            if (cmp != 0) return cmp;
            Integer aPa = (Integer) a.get("pa");
            Integer bPa = (Integer) b.get("pa");
            return bPa.compareTo(aPa);
        });

        return opponents.stream().limit(5).collect(Collectors.toList());
    }
}
