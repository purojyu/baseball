package com.example.baseball.service;

import java.util.ArrayList;
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
					List<List<VAtBatGameDetails>> groupedByPitcherList = atBatResult.stream()
							.collect(Collectors.groupingBy(VAtBatGameDetails::getPitcherId))
							.values() // Map<Long, List<VAtBatGameDetails>> の値部分を取り出す
							.stream() // Stream<List<VAtBatGameDetails>> を生成
							.collect(Collectors.toList()); // List<List<VAtBatGameDetails>> に変換
					for (List<VAtBatGameDetails> groupedByPitcher : groupedByPitcherList) {
						AtBatResultList.add(calcAtBatResult(groupedByPitcher));
					}
					AtBatResultList.sort(Comparator.comparing(MatchResult::getPitcherTeamId)
                            .thenComparing(Comparator.comparing(MatchResult::getAtBatNumber).reversed()));


					// 投手選択の場合
				} else if (batterId == null) {
					List<List<VAtBatGameDetails>> groupedByBatterList = atBatResult.stream()
							.collect(Collectors.groupingBy(VAtBatGameDetails::getBatterId))
							.values() // Map<Long, List<VAtBatGameDetails>> の値部分を取り出す
							.stream() // Stream<List<VAtBatGameDetails>> を生成
							.collect(Collectors.toList()); // List<List<VAtBatGameDetails>> に変換
					for (List<VAtBatGameDetails> groupedByBatter : groupedByBatterList) {
						AtBatResultList.add(calcAtBatResult(groupedByBatter));
					}
					AtBatResultList.sort(Comparator.comparing(MatchResult::getBatterTeamId)
                            .thenComparing(Comparator.comparing(MatchResult::getAtBatNumber).reversed()));


					//投手VS打者
				} else {
					AtBatResultList.add(calcAtBatResult(atBatResult));
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
		matchResult.setBatterNm(atBatResult.get(0).getBatterNm());
		matchResult.setBatterNpbUrl(atBatResult.get(0).getBatterNpbUrl());
	    Set<Long> uniqueBatterTeamIds = atBatResult.stream()
                .map(VAtBatGameDetails::getBatterTeamId)
                .collect(Collectors.toSet());
	    // 1プレイヤーに対して複数のチームがある場合
	    if(uniqueBatterTeamIds.size() > 1) {
	    	matchResult.setBatterTeamId(13L);
			matchResult.setBatterTeamNm("複数");
	    }else {
		matchResult.setBatterTeamId(atBatResult.get(0).getBatterTeamId());
		matchResult.setBatterTeamNm(atBatResult.get(0).getBatterTeamShortNm());
	    }
	    matchResult.setPitcherNm(atBatResult.get(0).getPitcherNm());
	    matchResult.setPitcherNpbUrl(atBatResult.get(0).getPitcherNpbUrl());
	    Set<Long> uniquePitcherTeamIds = atBatResult.stream()
                .map(VAtBatGameDetails::getPitcherTeamId)
                .collect(Collectors.toSet());
	    if(uniquePitcherTeamIds.size() > 1) {
			matchResult.setPitcherTeamId(13L);
			matchResult.setPitcherTeamNm("複数");
	    }else {
		matchResult.setPitcherTeamId(atBatResult.get(0).getPitcherTeamId());
		matchResult.setPitcherTeamNm(atBatResult.get(0).getPitcherTeamShortNm());
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
