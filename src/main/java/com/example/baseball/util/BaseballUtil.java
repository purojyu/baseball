package com.example.baseball.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import java.util.Objects;

import com.example.baseball.entity.VAtBatGameDetails;

public class BaseballUtil {

    // 定数の導入
    private static final String SINGLE_RESULT = "安";
    private static final String DOUBLE_RESULT = "２";
    private static final String TRIPLE_RESULT = "３";
    private static final String HOME_RUN_RESULT = "本";
    private static final String FOUR_BALL_RESULT = "四";
    private static final String HIT_BALL_RESULT = "死　球";
    private static final String SACRIFICE_HIT_RESULT = "犠打";
    private static final String SACRIFICE_FLY_RESULT = "犠飛";
    // 打数(AB)除外用: 犠打・犠飛に加え、犠打失敗系(投犠失/投犠野/一犠失 等)も含めて一括除外する。
    // NPB公式は犠打企図(失策・野選含む)を打数に数えないため、"犠" の部分一致で揃える。
    private static final String SACRIFICE_RESULT = "犠";
    private static final String STRIKEOUT_RESULT = "三　振";
    // 振り逃げ(振　逃)も三振としてカウントする（NPB公式は振り逃げを三振に計上する）
    private static final String STRIKEOUT_REACH_RESULT = "振　逃";
    // 打数(AB)除外用: 妨害による出塁(捕守妨/打妨出/走妨出 ①付き表記含む)は打数に数えない。
    // NPB公式は打撃・守備・走塁妨害での出塁を打数にも出塁率の分母にも含めないため、"妨" の部分一致で揃える。
    // なお「違　反」(反則打球・打順違反)は打者アウト＝打数に計上されるため除外しない。
    private static final String INTERFERENCE_RESULT = "妨";

    /**
     * NPB打数(AB)に数えない打席かを判定する。
     * 四球・死球・犠打/犠飛(犠打失敗系含む)・妨害出塁を除外する。
     * 「違　反」(反則打球・打順違反)は打者アウト=打数計上のため除外しない。
     * 打撃集計(本クラス)と球種別/コース別集計(PitchDetailService)で同一定義を共有する。
     * @param result 打席結果文字列
     * @return 打数に数えない打席なら true
     */
    public static boolean isNonAtBat(String result) {
        return result.contains(FOUR_BALL_RESULT)
                || result.contains(HIT_BALL_RESULT)
                || result.contains(SACRIFICE_RESULT)
                || result.contains(INTERFERENCE_RESULT);
    }

    /**
     * 打率を計算するメソッド。
     * 部分一致を使用してヒット判定を行います。
     * @param vAtBatGameDetails 打席結果のリスト
     * @return 打率
     */
    public static BigDecimal calculateBattingAverage(List<VAtBatGameDetails> vAtBatGameDetails) {
        int hits = 0;
        int atBats = 0;
        for (VAtBatGameDetails detail : vAtBatGameDetails) {
            String result = detail.getResult();
            // 部分一致によるヒット判定
            if (result.contains(SINGLE_RESULT) || result.contains(DOUBLE_RESULT) ||
                result.contains(TRIPLE_RESULT) || result.contains(HOME_RUN_RESULT)) {
                hits++;
                atBats++;
            } 
            // フォアボール、デッドボール、犠打・犠飛（犠打失敗系含む）、妨害出塁は打数に数えない
            else if (!isNonAtBat(result)) {
                atBats++;
            }
        }
        if (atBats == 0) {
            return BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(hits)
            .divide(BigDecimal.valueOf(atBats), 3, RoundingMode.HALF_UP);
    }

    /**
     * 出塁率を計算するメソッド。
     * @param vAtBatGameDetails 打席結果のリスト
     * @return 出塁率
     */
    public static BigDecimal calculateOnBasePercentage(List<VAtBatGameDetails> vAtBatGameDetails) {
        int hitCount = calculateHitNumber(vAtBatGameDetails);
        int fourHitBallCount = calculateFourHitBallNumber(vAtBatGameDetails);
        // NPB定義: 出塁率 = (安打 + 四球 + 死球) / (打数 + 四球 + 死球 + 犠飛)
        // 分母に全打席(犠打含む)を使うと犠打の多い選手で過小になるため、犠飛のみ加算する
        int denominator = calculateStrokesNumber(vAtBatGameDetails)
                + fourHitBallCount
                + calculateSacrificeFly(vAtBatGameDetails);

        if (denominator == 0) {
            return BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
        }

        return BigDecimal.valueOf(hitCount + fourHitBallCount)
                .divide(BigDecimal.valueOf(denominator), 3, RoundingMode.HALF_UP);
    }

    /**
     * 長打率を計算するメソッド。
     * @param vAtBatGameDetails 打席結果のリスト
     * @return 長打率
     */
    public static BigDecimal calculateSluggingPercentage(List<VAtBatGameDetails> vAtBatGameDetails) {
        int baseHitsNumber = calculateBaseHitsNumber(vAtBatGameDetails);
        int strokesNumber = calculateStrokesNumber(vAtBatGameDetails);

        if (strokesNumber == 0) {
            return BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
        }

        // 長打率の計算
        return BigDecimal.valueOf(baseHitsNumber)
                .divide(BigDecimal.valueOf(strokesNumber), 3, RoundingMode.HALF_UP);
    }

    /**
     * OPSを計算するメソッド。
     * @param vAtBatGameDetails 打席結果のリスト
     * @return OPS
     */
    public static BigDecimal calculateOps(List<VAtBatGameDetails> vAtBatGameDetails) {
        BigDecimal sluggingPercentage = calculateSluggingPercentage(vAtBatGameDetails);
        BigDecimal onBasePercentage = calculateOnBasePercentage(vAtBatGameDetails);

        // OPSの計算
        return sluggingPercentage.add(onBasePercentage);
    }

    /**
     * 打数を計算するメソッド。
     * @param vAtBatGameDetails 打席結果のリスト
     * @return 打数
     */
    public static int calculateStrokesNumber(List<VAtBatGameDetails> vAtBatGameDetails) {
        int count = 0;
        for (VAtBatGameDetails detail : vAtBatGameDetails) {
            if (isNonAtBat(detail.getResult())) {
                count++;
            }
        }
        return vAtBatGameDetails.size() - count;
    }

    /**
     * ヒット数を計算するメソッド。
     * 部分一致でヒット判定。
     * @param vAtBatGameDetails
     * @return ヒット数
     */
    public static int calculateHitNumber(List<VAtBatGameDetails> vAtBatGameDetails) {
        int count = 0;
        for (VAtBatGameDetails detail : vAtBatGameDetails) {
            String result = detail.getResult();
            if (result.contains(SINGLE_RESULT) || result.contains(DOUBLE_RESULT) ||
                result.contains(TRIPLE_RESULT) || result.contains(HOME_RUN_RESULT)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 単打数
     */
    public static int calculateSinglesNumber(List<VAtBatGameDetails> vAtBatGameDetails) {
        return countOccurrences(vAtBatGameDetails, SINGLE_RESULT);
    }

    /**
     * 二塁打数
     */
    public static int calculateDoublesNumber(List<VAtBatGameDetails> vAtBatGameDetails) {
        return countOccurrences(vAtBatGameDetails, DOUBLE_RESULT);
    }

    /**
     * 三塁打数
     */
    public static int calculateTriplesNumber(List<VAtBatGameDetails> vAtBatGameDetails) {
        return countOccurrences(vAtBatGameDetails, TRIPLE_RESULT);
    }

    /**
     * 本塁打数
     */
    public static int calculateHomeRun(List<VAtBatGameDetails> vAtBatGameDetails) {
        return countOccurrences(vAtBatGameDetails, HOME_RUN_RESULT);
    }

    /**
     * 四球数
     */
    public static int calculateFourBallNumber(List<VAtBatGameDetails> vAtBatGameDetails) {
        return countOccurrences(vAtBatGameDetails, FOUR_BALL_RESULT);
    }

    /**
     * 死球数
     */
    public static int calculateHitBallNumber(List<VAtBatGameDetails> vAtBatGameDetails) {
        return countOccurrences(vAtBatGameDetails, HIT_BALL_RESULT);
    }

    /**
     * 四死球数
     */
    public static int calculateFourHitBallNumber(List<VAtBatGameDetails> vAtBatGameDetails) {
        return countOccurrences(vAtBatGameDetails, HIT_BALL_RESULT) +
               countOccurrences(vAtBatGameDetails, FOUR_BALL_RESULT);
    }

    /**
     * 犠打数
     */
    public static int calculateSacrificeHit(List<VAtBatGameDetails> vAtBatGameDetails) {
        return countExactOccurrences(vAtBatGameDetails, SACRIFICE_HIT_RESULT);
    }

    /**
     * 犠飛数
     */
    public static int calculateSacrificeFly(List<VAtBatGameDetails> vAtBatGameDetails) {
        return countOccurrences(vAtBatGameDetails, SACRIFICE_FLY_RESULT);
    }

    /**
     * 三振数
     */
    public static int calculateStrikeoutsNumber(List<VAtBatGameDetails> vAtBatGameDetails) {
        return countExactOccurrences(vAtBatGameDetails, STRIKEOUT_RESULT)
                + countExactOccurrences(vAtBatGameDetails, STRIKEOUT_REACH_RESULT);
    }

    /**
     * 打点(RBI)数。
     * NPB/Yahoo の打席結果は打点を末尾の丸数字(①②③④)で表す(例: 中前安①, 左越本④=満塁弾,
     * 四　球①=押出, 二ゴロ①=進塁打)。打点が無い打席は丸数字を持たない。
     * 末尾の丸数字を打点として全打席分を合計する。
     * @param vAtBatGameDetails 打席結果のリスト
     * @return 打点数
     */
    public static int calculateRbi(List<VAtBatGameDetails> vAtBatGameDetails) {
        int rbi = 0;
        for (VAtBatGameDetails detail : vAtBatGameDetails) {
            rbi += rbiFromResult(detail.getResult());
        }
        return rbi;
    }

    /**
     * 打席結果文字列の末尾の丸数字(①=U+2460〜)を打点として返す。丸数字でなければ 0。
     */
    private static int rbiFromResult(String result) {
        if (result == null || result.isEmpty()) {
            return 0;
        }
        char last = result.charAt(result.length() - 1);
        // 丸数字 ①(U+2460)〜⑳(U+2473) は連続コードポイント。1打席の打点は最大4(満塁本塁打)。
        if (last >= '①' && last <= '⑳') {
            return last - '①' + 1;
        }
        return 0;
    }

    /**
     * 塁打数
     */
    public static int calculateBaseHitsNumber(List<VAtBatGameDetails> vAtBatGameDetails) {
        int singleCount = calculateSinglesNumber(vAtBatGameDetails);
        int doubleCount = calculateDoublesNumber(vAtBatGameDetails) * 2;
        int tripleCount = calculateTriplesNumber(vAtBatGameDetails) * 3;
        int homeRunCount = calculateHomeRun(vAtBatGameDetails) * 4;
        return singleCount + doubleCount + tripleCount + homeRunCount;
    }

    /**
     * 指定された結果が含まれる打席数をカウントするユーティリティメソッド(部分一致)。
     */
    private static int countOccurrences(List<VAtBatGameDetails> vAtBatGameDetails, String result) {
        int count = 0;
        for (VAtBatGameDetails detail : vAtBatGameDetails) {
            if (detail.getResult().contains(result)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 指定された結果が正確に一致する打席数をカウントするユーティリティメソッド。
     */
    private static int countExactOccurrences(List<VAtBatGameDetails> vAtBatGameDetails, String result) {
        int count = 0;
        for (VAtBatGameDetails detail : vAtBatGameDetails) {
            if (Objects.equals(detail.getResult(), result)) {
                count++;
            }
        }
        return count;
    }
}