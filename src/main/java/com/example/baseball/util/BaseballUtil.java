package com.example.baseball.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.thymeleaf.util.StringUtils;

import com.example.baseball.entity.VAtBatGameDetails;

public class BaseballUtil {

    /**
     * 打率を計算するメソッド。
     * @param vAtBatGameDetails 打席結果のリスト
     * @return 打率
     */
    public static BigDecimal calculateBattingAverage(List<VAtBatGameDetails> vAtBatGameDetails) {
        int hits = 0;
        int atBats = 0;
        for (VAtBatGameDetails vAtBatGameDetail : vAtBatGameDetails) {
        	String  result =  vAtBatGameDetail.getResult();
            if (result.contains("安") || result.contains("２") || result.contains("３") || result.contains("本")) {
                hits++;
                atBats++;
            } // フォアボール、デッドボール、犠打、犠牲フライは無視する
            else if (!result.contains("四") && !StringUtils.equals(result, "死　球") && !result.contains("犠打") && !result.contains("犠飛")) {
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
     * @param vAtBatGameDetails
     * @return
     */
    public static BigDecimal calculateOnBasePercentage(List<VAtBatGameDetails> vAtBatGameDetails) {
        int hitCount = calculateHitNumber(vAtBatGameDetails);
        int fourHitBallCount = calculateFourHitBallNumber(vAtBatGameDetails);

        if (vAtBatGameDetails.size() == 0) {
            return BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
        }

        // 出塁率の計算
        BigDecimal onBasePercentage = BigDecimal.valueOf(hitCount + fourHitBallCount)
                .divide(BigDecimal.valueOf(vAtBatGameDetails.size()), 3, RoundingMode.HALF_UP);

        return onBasePercentage;
    }

    /**
     * 長打率
     * @param vAtBatGameDetails
     * @return
     */
    public static BigDecimal calculateSluggingPercentage(List<VAtBatGameDetails> vAtBatGameDetails) {
        int baseHitsNumber = calculateBaseHitsNumber(vAtBatGameDetails);
        int strokesNumber = calculateStrokesNumber(vAtBatGameDetails);

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
     * @param vAtBatGameDetails
     * @return
     */
    public static BigDecimal calculateOps(List<VAtBatGameDetails> vAtBatGameDetails) {
        BigDecimal sluggingPercentage = calculateSluggingPercentage(vAtBatGameDetails);
        BigDecimal onBasePercentage = calculateOnBasePercentage(vAtBatGameDetails);

        // OPSの計算
        BigDecimal ops = sluggingPercentage.add(onBasePercentage);

        return ops;
    }

    /**
     * 打数
     * @param vAtBatGameDetails
     * @return
     */
    public static int calculateStrokesNumber(List<VAtBatGameDetails> vAtBatGameDetails) {
        int count = 0;
        for (VAtBatGameDetails vAtBatGameDetail : vAtBatGameDetails) {
        	String result = vAtBatGameDetail.getResult();
            if (result.contains("四") || StringUtils.equals(result, "死　球") || result.contains("犠打") || result.contains("犠飛")) {
                count++;
            }
        }
        return vAtBatGameDetails.size() - count;
    }

    /**
     * ヒット数
     * @param vAtBatGameDetails
     * @return
     */
    public static int calculateHitNumber(List<VAtBatGameDetails> vAtBatGameDetails) {
        int count = 0;
        for (VAtBatGameDetails vAtBatGameDetail :  vAtBatGameDetails) {
        	String result = vAtBatGameDetail.getResult();
            if (result.contains("安") || result.contains("２") || result.contains("３") || result.contains("本")) {
                count++;
            }
        }
        return count;
    }

    /**
     * 単打数
     * @param vAtBatGameDetails
     * @return
     */
    public static int calculateSinglesNumber(List<VAtBatGameDetails> vAtBatGameDetails) {
        int count = 0;
        for (VAtBatGameDetails vAtBatGameDetail :  vAtBatGameDetails) {
        	String result = vAtBatGameDetail.getResult();
            if (result.contains("安")) {
                count++;
            }
        }
        return count;
    }

    /**
     * 二塁打数
     * @param vAtBatGameDetails
     * @return
     */
    public static int calculateDoublesNumber(List<VAtBatGameDetails> vAtBatGameDetails) {
        int count = 0;
        for (VAtBatGameDetails vAtBatGameDetail :  vAtBatGameDetails) {
        	String result = vAtBatGameDetail.getResult();
            if (result.contains("２")) {
                count++;
            }
        }
        return count;
    }

    /**
     * 三塁打数
     * @param vAtBatGameDetails
     * @return
     */
    public static int calculateTriplesNumber(List<VAtBatGameDetails> vAtBatGameDetails) {
        int count = 0;
        for (VAtBatGameDetails vAtBatGameDetail :  vAtBatGameDetails) {
        	String result = vAtBatGameDetail.getResult();
            if (result.contains("３")) {
                count++;
            }
        }
        return count;
    }

    /**
     * 本塁打数
     * @param vAtBatGameDetails
     * @return
     */
    public static int calculateHomeRun(List<VAtBatGameDetails> vAtBatGameDetails) {
        int count = 0;
        for (VAtBatGameDetails vAtBatGameDetail :  vAtBatGameDetails) {
        	String result = vAtBatGameDetail.getResult();
            if (result.contains("本")) {
                count++;
            }
        }
        return count;
    }

    /**
     * 四球数
     * @param vAtBatGameDetails
     * @return
     */
    public static int calculateFourBallNumber(List<VAtBatGameDetails> vAtBatGameDetails) {
        int count = 0;
        for (VAtBatGameDetails vAtBatGameDetail :  vAtBatGameDetails) {
        	String result = vAtBatGameDetail.getResult();
            if (result.contains("四")) {
                count++;
            }
        }
        return count;
    }

    /**
     * 死球数
     * @param vAtBatGameDetails
     * @return
     */
    public static int calculateHitBallNumber(List<VAtBatGameDetails> vAtBatGameDetails) {
        int count = 0;
        for (VAtBatGameDetails vAtBatGameDetail :  vAtBatGameDetails) {
        	String result = vAtBatGameDetail.getResult();
            if (StringUtils.equals(result, "死　球")) {
                count++;
            }
        }
        return count;
    }

    /**
     * 四死球数
     * @param vAtBatGameDetails
     * @return
     */
    public static int calculateFourHitBallNumber(List<VAtBatGameDetails> vAtBatGameDetails) {
        int count = 0;
        for (VAtBatGameDetails vAtBatGameDetail :  vAtBatGameDetails) {
        	String result = vAtBatGameDetail.getResult();
            if (StringUtils.equals(result, "死　球") || result.contains("四")) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * 犠打数
     * @param vAtBatGameDetails
     * @return
     */
    public static int calculateSacrificeHit(List<VAtBatGameDetails> vAtBatGameDetails) {
        int count = 0;
        for (VAtBatGameDetails vAtBatGameDetail :  vAtBatGameDetails) {
        	String result = vAtBatGameDetail.getResult();
            if (StringUtils.equals(result, "犠打")) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * 犠飛数
     * @param vAtBatGameDetails
     * @return
     */
    public static int calculateSacrificeFly(List<VAtBatGameDetails> vAtBatGameDetails) {
        int count = 0;
        for (VAtBatGameDetails vAtBatGameDetail :  vAtBatGameDetails) {
        	String result = vAtBatGameDetail.getResult();
            if (StringUtils.equals(result, "犠飛")) {
                count++;
            }
        }
        return count;
    }

    /**
     * 三振数
     * @param vAtBatGameDetails
     * @return
     */
    public static int calculateStrikeoutsNumber(List<VAtBatGameDetails> vAtBatGameDetails) {
        int count = 0;
        for (VAtBatGameDetails vAtBatGameDetail :  vAtBatGameDetails) {
        	String result = vAtBatGameDetail.getResult();
            if (StringUtils.equals(result, "三　振")) {
                count++;
            }
        }
        return count;
    }

    /**
     * 塁打数
     * @param vAtBatGameDetails
     * @return
     */
    public static int calculateBaseHitsNumber(List<VAtBatGameDetails> vAtBatGameDetails) {
        int singleCount = calculateSinglesNumber(vAtBatGameDetails);
        int doubleCount = calculateDoublesNumber(vAtBatGameDetails) * 2;
        int tripleCount = calculateTriplesNumber(vAtBatGameDetails) * 3;
        int homeRunCount = calculateHomeRun(vAtBatGameDetails) * 4;
        return singleCount + doubleCount + tripleCount + homeRunCount;
    }
}
