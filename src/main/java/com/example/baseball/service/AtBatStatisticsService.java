package com.example.baseball.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
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
	public List<MatchResult> retrieveAtBatResults(List<VAtBatGameDetails> atBatResult, Long pitcherId, Long batterId){
		List<MatchResult> AtBatResultList = new ArrayList<>();
       		// 打者選択の場合
		if (pitcherId == null) {
	        // Map を直接処理し、並列ストリームを使用
	        AtBatResultList = atBatResult.stream()
	                .collect(Collectors.groupingBy(VAtBatGameDetails::getPitcherId))
	                .values()
	                .parallelStream()
	                .map(this::calcAtBatResult)
	                .sorted(Comparator.comparing(MatchResult::getPitcherTeamId)
	                        .thenComparing(Comparator.comparing(MatchResult::getAtBatNumber).reversed()))
	                .collect(Collectors.toList());
	    } else if (batterId == null) {
	        AtBatResultList = atBatResult.stream()
	                .collect(Collectors.groupingBy(VAtBatGameDetails::getBatterId))
	                .values()
	                .parallelStream()
	                .map(this::calcAtBatResult)
	                .sorted(Comparator.comparing(MatchResult::getBatterTeamId)
	                        .thenComparing(Comparator.comparing(MatchResult::getAtBatNumber).reversed()))
	                .collect(Collectors.toList());
	    } else {
	        AtBatResultList = Collections.singletonList(calcAtBatResult(atBatResult));
	    }

	    return AtBatResultList;
	}
	
	/**
	 * 打率等を計算する
	 * @param atBatResult
	 * @return
	 */
	public MatchResult calcAtBatResult(List<VAtBatGameDetails> atBatResult) {
		MatchResult matchResult = new MatchResult();
	    VAtBatGameDetails firstRecord = atBatResult.get(0);
	    matchResult.setBatterNm(firstRecord.getBatterNm());
	    matchResult.setBatterNpbUrl(firstRecord.getBatterNpbUrl());
	    Set<Long> uniqueBatterTeamIds = atBatResult.stream()
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
	    Set<Long> uniquePitcherTeamIds = atBatResult.stream()
                .map(VAtBatGameDetails::getPitcherTeamId)
                .collect(Collectors.toSet());
	    if(uniquePitcherTeamIds.size() > 1) {
			matchResult.setPitcherTeamId(13L);
			matchResult.setPitcherTeamNm("複数");
	    }else {
		matchResult.setPitcherTeamId(firstRecord.getPitcherTeamId());
		matchResult.setPitcherTeamNm(firstRecord.getPitcherTeamShortNm());
	    }
		matchResult.setBattingAverage(BaseballUtil.calculateBattingAverage(atBatResult));
		matchResult.setAtBatNumber(atBatResult.size());
		matchResult.setStrokesNumber(BaseballUtil.calculateStrokesNumber(atBatResult));
		matchResult.setHitNumber(BaseballUtil.calculateHitNumber(atBatResult));
		matchResult.setSinglesNumber(BaseballUtil.calculateSinglesNumber(atBatResult));
		matchResult.setDoublesNumber(BaseballUtil.calculateDoublesNumber(atBatResult));
		matchResult.setTriplesNumber(BaseballUtil.calculateTriplesNumber(atBatResult));
		matchResult.setHomeRun(BaseballUtil.calculateHomeRun(atBatResult));
		matchResult.setBaseHitsNumber(BaseballUtil.calculateBaseHitsNumber(atBatResult));
		matchResult.setFourBallNumber(BaseballUtil.calculateFourBallNumber(atBatResult));
		matchResult.setHitBallNumber(BaseballUtil.calculateHitBallNumber(atBatResult));
		matchResult.setSacrificeFly(BaseballUtil.calculateSacrificeFly(atBatResult));
		matchResult.setStrikeoutsNumber(BaseballUtil.calculateStrikeoutsNumber(atBatResult));
		matchResult.setOps(BaseballUtil.calculateOps(atBatResult));
		matchResult.setOnBasePercentage(BaseballUtil.calculateOnBasePercentage(atBatResult));
		matchResult.setSluggingPercentage(BaseballUtil.calculateSluggingPercentage(atBatResult));
		return matchResult;
	}

}
