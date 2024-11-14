package com.example.baseball.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.baseball.entity.MatchResult;
import com.example.baseball.entity.VAtBatGameDetails;
import com.example.baseball.util.BaseballUtil;

/**
 * 取得した打席結果を集計して計算する
 * 移籍した選手の場合、画面でチームの指定がない場合は、チームの絞り込みをしない選手検索を行う。
 * チームの指定があれば、チームの絞り込みをした選手検索を行う。
 */
@Service
public class AtBatStatisticsService {

    public List<MatchResult> retrieveAtBatResults(List<VAtBatGameDetails> atBatResults, Long pitcherId, Long batterId) {
        if (pitcherId == null) {
            return processResults(atBatResults, VAtBatGameDetails::getPitcherId, MatchResult::getPitcherTeamId);
        } else if (batterId == null) {
            return processResults(atBatResults, VAtBatGameDetails::getBatterId, MatchResult::getBatterTeamId);
        } else {
            return Collections.singletonList(calcAtBatResult(atBatResults));
        }
    }

    private List<MatchResult> processResults(List<VAtBatGameDetails> atBatResults, 
                                             Function<VAtBatGameDetails, Long> groupingFunction,
                                             Function<MatchResult, Long> sortingFunction) {
        return atBatResults.stream()
                .collect(Collectors.groupingBy(groupingFunction))
                .values()
                .stream()
                .map(this::calcAtBatResult)
                .sorted(Comparator.comparing(sortingFunction)
                        .thenComparing(Comparator.comparing(MatchResult::getAtBatNumber).reversed()))
                .collect(Collectors.toList());
    }

	/**
	 * 打率等を計算する
	 * @param atBatResult
	 * @return
	 */
    public MatchResult calcAtBatResult(List<VAtBatGameDetails> atBatResults) {
        MatchResult matchResult = new MatchResult();
        VAtBatGameDetails firstRecord = atBatResults.get(0);

        setCommonMatchResultProperties(matchResult, firstRecord, atBatResults);

        matchResult.setBattingAverage(BaseballUtil.calculateBattingAverage(atBatResults));
        matchResult.setAtBatNumber(atBatResults.size());
        matchResult.setStrokesNumber(BaseballUtil.calculateStrokesNumber(atBatResults));
        matchResult.setHitNumber(BaseballUtil.calculateHitNumber(atBatResults));
        matchResult.setSinglesNumber(BaseballUtil.calculateSinglesNumber(atBatResults));
        matchResult.setDoublesNumber(BaseballUtil.calculateDoublesNumber(atBatResults));
        matchResult.setTriplesNumber(BaseballUtil.calculateTriplesNumber(atBatResults));
        matchResult.setHomeRun(BaseballUtil.calculateHomeRun(atBatResults));
        matchResult.setBaseHitsNumber(BaseballUtil.calculateBaseHitsNumber(atBatResults));
        matchResult.setFourBallNumber(BaseballUtil.calculateFourBallNumber(atBatResults));
        matchResult.setHitBallNumber(BaseballUtil.calculateHitBallNumber(atBatResults));
        matchResult.setSacrificeFly(BaseballUtil.calculateSacrificeFly(atBatResults));
        matchResult.setStrikeoutsNumber(BaseballUtil.calculateStrikeoutsNumber(atBatResults));
        matchResult.setOps(BaseballUtil.calculateOps(atBatResults));
        matchResult.setOnBasePercentage(BaseballUtil.calculateOnBasePercentage(atBatResults));
        matchResult.setSluggingPercentage(BaseballUtil.calculateSluggingPercentage(atBatResults));
        return matchResult;
    }

    private void setCommonMatchResultProperties(MatchResult matchResult, VAtBatGameDetails firstRecord, List<VAtBatGameDetails> atBatResults) {
        matchResult.setBatterNm(firstRecord.getBatterNm());
        matchResult.setBatterNpbUrl(firstRecord.getBatterNpbUrl());
        
        Set<Long> uniqueBatterTeamIds = atBatResults.stream()
                .map(VAtBatGameDetails::getBatterTeamId)
                .collect(Collectors.toSet());
        if (uniqueBatterTeamIds.size() > 1) {
            matchResult.setBatterTeamId(13L);
            matchResult.setBatterTeamNm("複数");
        } else {
            matchResult.setBatterTeamId(firstRecord.getBatterTeamId());
            matchResult.setBatterTeamNm(firstRecord.getBatterTeamShortNm());
        }
        
        matchResult.setPitcherNm(firstRecord.getPitcherNm());
        matchResult.setPitcherNpbUrl(firstRecord.getPitcherNpbUrl());
        Set<Long> uniquePitcherTeamIds = atBatResults.stream()
                .map(VAtBatGameDetails::getPitcherTeamId)
                .collect(Collectors.toSet());
        if (uniquePitcherTeamIds.size() > 1) {
            matchResult.setPitcherTeamId(13L);
            matchResult.setPitcherTeamNm("複数");
        } else {
            matchResult.setPitcherTeamId(firstRecord.getPitcherTeamId());
            matchResult.setPitcherTeamNm(firstRecord.getPitcherTeamShortNm());
        }
    }
}
