package com.example.baseball.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.thymeleaf.util.StringUtils;

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
    private static final String STRIKEOUT_RESULT = "三　振";

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
            // フォアボール、デッドボール、犠打、犠牲フライは無視する
            else if (!result.contains(FOUR_BALL_RESULT) &&
                     !StringUtils.equals(result, HIT_BALL_RESULT) &&
                     !result.contains(SACRIFICE_HIT_RESULT) &&
                     !result.contains(SACRIFICE_FLY_RESULT)) {
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

        if (vAtBatGameDetails.size() == 0) {
            return BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
        }

        // 出塁率の計算
        return BigDecimal.valueOf(hitCount + fourHitBallCount)
                .divide(BigDecimal.valueOf(vAtBatGameDetails.size()), 3, RoundingMode.HALF_UP);
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
            String result = detail.getResult();
            if (result.contains(FOUR_BALL_RESULT) || StringUtils.equals(result, HIT_BALL_RESULT) ||
                result.contains(SACRIFICE_HIT_RESULT) || result.contains(SACRIFICE_FLY_RESULT)) {
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
        return countExactOccurrences(vAtBatGameDetails, HIT_BALL_RESULT);
    }

    /**
     * 四死球数
     */
    public static int calculateFourHitBallNumber(List<VAtBatGameDetails> vAtBatGameDetails) {
        return countExactOccurrences(vAtBatGameDetails, HIT_BALL_RESULT) +
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
        return countExactOccurrences(vAtBatGameDetails, SACRIFICE_FLY_RESULT);
    }

    /**
     * 三振数
     */
    public static int calculateStrikeoutsNumber(List<VAtBatGameDetails> vAtBatGameDetails) {
        return countExactOccurrences(vAtBatGameDetails, STRIKEOUT_RESULT);
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
            if (StringUtils.equals(detail.getResult(), result)) {
                count++;
            }
        }
        return count;
    }
}