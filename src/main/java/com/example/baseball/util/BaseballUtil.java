package com.example.baseball.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.thymeleaf.util.StringUtils;

public class BaseballUtil {

    /**
     * 打率を計算するメソッド。
     * @param results 打席結果のリスト
     * @return 打率
     */
    public static BigDecimal calculateBattingAverage(List<String> results) {
        int hits = 0;
        int atBats = 0;

        for (String result : results) {
            if (result.contains("安") || result.contains("２") || result.contains("３") || result.contains("本")) {
                hits++;
                atBats++;
            } // フォアボール、デッドボール、犠打、犠牲フライは無視する
            else if (!result.contains("四") && !StringUtils.equals(result, "死　球") && !result.contains("犠")) {
                atBats++;
            }
        }
        if (atBats == 0) {
            return BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
        }
        BigDecimal battingAverage = BigDecimal.valueOf(hits).divide(BigDecimal.valueOf(atBats), 3, RoundingMode.HALF_UP);
        return battingAverage;
    }

    
    /**
     * 出塁率
     * @param results
     * @return
     */
    public static BigDecimal calculateOnBasePercentage(List<String> results) {
        int hitCount = calculateHitNumber(results);
        int fourHitBallCount = calculateFourHitBallNumber(results);

        if (results.size() == 0) {
            return BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
        }

        // 出塁率の計算
        BigDecimal onBasePercentage = BigDecimal.valueOf(hitCount + fourHitBallCount)
                .divide(BigDecimal.valueOf(results.size()), 3, RoundingMode.HALF_UP);

        return onBasePercentage;
    }

    /**
     * 長打率
     * @param results
     * @return
     */
    public static BigDecimal calculateSluggingPercentage(List<String> results) {
        int baseHitsNumber = calculateBaseHitsNumber(results);
        int strokesNumber = calculateStrokesNumber(results);

        if (strokesNumber == 0) {
            return BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
        }

        // 長打率の計算
        BigDecimal sluggingPercentage = BigDecimal.valueOf(baseHitsNumber)
                .divide(BigDecimal.valueOf(strokesNumber), 3, RoundingMode.HALF_UP);

        return sluggingPercentage;
    }

    /**
     * OPS
     * @param results
     * @return
     */
    public static BigDecimal calculateOps(List<String> results) {
        BigDecimal sluggingPercentage = calculateSluggingPercentage(results);
        BigDecimal onBasePercentage = calculateOnBasePercentage(results);

        // OPSの計算
        BigDecimal ops = sluggingPercentage.add(onBasePercentage);

        return ops;
    }

    /**
     * 打数
     * @param results
     * @return
     */
    public static int calculateStrokesNumber(List<String> results) {
        int count = 0;
        for (String result : results) {
            if (result.contains("四") || StringUtils.equals(result, "死　球") || result.contains("犠")) {
                count++;
            }
        }
        return results.size() - count;
    }

    /**
     * ヒット数
     * @param results
     * @return
     */
    public static int calculateHitNumber(List<String> results) {
        int count = 0;
        for (String result : results) {
            if (result.contains("安") || result.contains("２") || result.contains("３") || result.contains("本")) {
                count++;
            }
        }
        return count;
    }

    /**
     * 単打数
     * @param results
     * @return
     */
    public static int calculateSinglesNumber(List<String> results) {
        int count = 0;
        for (String result : results) {
            if (result.contains("安")) {
                count++;
            }
        }
        return count;
    }

    /**
     * 二塁打数
     * @param results
     * @return
     */
    public static int calculateDoublesNumber(List<String> results) {
        int count = 0;
        for (String result : results) {
            if (result.contains("２")) {
                count++;
            }
        }
        return count;
    }

    /**
     * 三塁打数
     * @param results
     * @return
     */
    public static int calculateTriplesNumber(List<String> results) {
        int count = 0;
        for (String result : results) {
            if (result.contains("３")) {
                count++;
            }
        }
        return count;
    }

    /**
     * 本塁打数
     * @param results
     * @return
     */
    public static int calculateHomeRun(List<String> results) {
        int count = 0;
        for (String result : results) {
            if (result.contains("本")) {
                count++;
            }
        }
        return count;
    }

    /**
     * 四球数
     * @param results
     * @return
     */
    public static int calculateFourBallNumber(List<String> results) {
        int count = 0;
        for (String result : results) {
            if (result.contains("四")) {
                count++;
            }
        }
        return count;
    }

    /**
     * 死球数
     * @param results
     * @return
     */
    public static int calculateHitBallNumber(List<String> results) {
        int count = 0;
        for (String result : results) {
            if (StringUtils.equals(result, "死　球")) {
                count++;
            }
        }
        return count;
    }

    /**
     * 四死球数
     * @param results
     * @return
     */
    public static int calculateFourHitBallNumber(List<String> results) {
        int count = 0;
        for (String result : results) {
            if (StringUtils.equals(result, "死　球") || result.contains("四")) {
                count++;
            }
        }
        return count;
    }

    /**
     * 三振数
     * @param results
     * @return
     */
    public static int calculateStrikeoutsNumber(List<String> results) {
        int count = 0;
        for (String result : results) {
            if (StringUtils.equals(result, "三　振")) {
                count++;
            }
        }
        return count;
    }

    /**
     * 塁打数
     * @param results
     * @return
     */
    public static int calculateBaseHitsNumber(List<String> results) {
        int singleCount = calculateSinglesNumber(results);
        int doubleCount = calculateDoublesNumber(results) * 2;
        int tripleCount = calculateTriplesNumber(results) * 3;
        int homeRunCount = calculateHomeRun(results) * 4;
        return singleCount + doubleCount + tripleCount + homeRunCount;
    }
}
