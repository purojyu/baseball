package com.example.baseball.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.baseball.entity.BaseballTeam;
import com.example.baseball.entity.MatchResult;
import com.example.baseball.entity.VAtBatGameDetails;
import com.example.baseball.entity.VBaseballPlayerHistoryRegular;
import com.example.baseball.service.AtBatResultService;
import com.example.baseball.service.BaseballPlayerService;
import com.example.baseball.service.BaseballTeamService;
import com.example.baseball.service.VAtBatGameDetailsService;
import com.example.baseball.service.VBaseballPlayerHistoryRegularService;
import com.example.baseball.util.BaseballUtil;

import lombok.RequiredArgsConstructor;

@RestController
//@CrossOrigin(origins = "http://localhost:8081")
@RequiredArgsConstructor
@RequestMapping("/baseball/api")
public class BaseballController {

	final BaseballTeamService baseballTeamService;
	final BaseballPlayerService baseballPlayerService;
	final AtBatResultService atBatResultService;
	final VBaseballPlayerHistoryRegularService vBaseballPlayerHistoryRegularService;
	final VAtBatGameDetailsService vAtBatGameDetailsService;

	// 初期表示データの取得
	@GetMapping("/getInitData")
	public ResponseEntity<Map<String, Object>> getInitData() {
		Map<String, Object> response = new HashMap<>();
		List<BaseballTeam> baseballTeamList = baseballTeamService.findAllBaseballTeam();
		response.put("baseballTeam", baseballTeamList);
		response.put("message", "Success");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	// ピッチャーの取得
	@GetMapping("/getPitcherList")
	public ResponseEntity<Map<String, Object>> getPitcherList(@RequestParam("teamId") long teamId) {
		Map<String, Object> response = new HashMap<>();
		List<VBaseballPlayerHistoryRegular> baseballPlayerList = vBaseballPlayerHistoryRegularService
				.findByTeamIdAndPosition(teamId, "1");
		if (baseballPlayerList.isEmpty()) {
			response.put("message", "ピッチャーの取得に失敗しました");
			return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
		}

		response.put("baseballPlayer", baseballPlayerList);
		response.put("message", "Success");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	// バッターの取得
	@GetMapping("/getBatterList")
	public ResponseEntity<Map<String, Object>> getBatterList(@RequestParam("teamId") long teamId) {
		Map<String, Object> response = new HashMap<>();
		List<VBaseballPlayerHistoryRegular> baseballPlayerList = vBaseballPlayerHistoryRegularService
				.findByTeamId(teamId);
		if (baseballPlayerList.isEmpty()) {
			response.put("message", "バッターの取得に失敗しました");
			return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
		}
		response.put("baseballPlayer", baseballPlayerList);
		response.put("message", "Success");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * 投手VS野手の対戦成績を取得する
	 * @param pitcherId
	 * @param batterId
	 * @param pitcherId
	 * @param batterId
	 * @return
	 */
	@GetMapping("/matchResultSearch")
	public ResponseEntity<Map<String, Object>> matchResultSearch(
			@RequestParam(value = "pitcherTeamId", required = false) Long pitcherTeamId,
			@RequestParam(value = "batterTeamId", required = false) Long batterTeamId,
			@RequestParam(value = "pitcherId", required = false) Long pitcherId,
			@RequestParam(value = "batterId", required = false) Long batterId) {
		Map<String, Object> response = new HashMap<>();
		//        List<String> AtBatResult = atBatResultService.findMatchResult(pitcherId, batterId);
		List<VAtBatGameDetails> AtBatResult = vAtBatGameDetailsService.findByBatterAndPitcher(pitcherTeamId,
				batterTeamId, pitcherId, batterId);

		if (AtBatResult.isEmpty()) {
			response.put("message", "対戦結果がありませんでした。");
			return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
		}
		List<MatchResult> AtBatResultList = new ArrayList<>();
		// 打者VSすべてのチームの投手
		if (pitcherTeamId == null) {
			List<List<VAtBatGameDetails>> groupedByPitcherList = AtBatResult.stream()
					.collect(Collectors.groupingBy(v -> Map.entry(v.getPitcherTeamId(), v.getPitcherId())))
					.values()
					.stream()
					.collect(Collectors.toList());
			for (List<VAtBatGameDetails> groupedByPitcher : groupedByPitcherList) {
				AtBatResultList.add(calcAtBatResult(groupedByPitcher));
			}
			AtBatResultList.sort(Comparator.comparing(MatchResult::getPitcherTeamId)
					.thenComparing(Comparator.comparing(MatchResult::getAtBatNumber).reversed()));
			// 投手VSすべてのチームの打者
		} else if (batterTeamId == null) {
			List<List<VAtBatGameDetails>> groupedByBatterList = AtBatResult.stream()
					.collect(Collectors.groupingBy(v -> Map.entry(v.getBatterTeamId(), v.getBatterId())))
					.values()
					.stream()
					.collect(Collectors.toList());
			for (List<VAtBatGameDetails> groupedByBatter : groupedByBatterList) {
				AtBatResultList.add(calcAtBatResult(groupedByBatter));
			}
			AtBatResultList.sort(Comparator.comparing(MatchResult::getBatterTeamId)
					.thenComparing(Comparator.comparing(MatchResult::getAtBatNumber).reversed()));
		}
		// 投手チームVS打者
		else if (pitcherId == null) {
			List<List<VAtBatGameDetails>> groupedByPitcherList = AtBatResult.stream()
					.collect(Collectors.groupingBy(VAtBatGameDetails::getPitcherId))
					.values() // Map<Long, List<VAtBatGameDetails>> の値部分を取り出す
					.stream() // Stream<List<VAtBatGameDetails>> を生成
					.collect(Collectors.toList()); // List<List<VAtBatGameDetails>> に変換
			for (List<VAtBatGameDetails> groupedByPitcher : groupedByPitcherList) {
				AtBatResultList.add(calcAtBatResult(groupedByPitcher));
			}
			AtBatResultList.sort((Comparator.comparing(MatchResult::getAtBatNumber).reversed()));
			// 打者チームVS投手(打者のチームに属するすべての打者VS投手)
		} else if (batterId == null) {
			List<List<VAtBatGameDetails>> groupedByBatterList = AtBatResult.stream()
					.collect(Collectors.groupingBy(VAtBatGameDetails::getBatterId))
					.values() // Map<Long, List<VAtBatGameDetails>> の値部分を取り出す
					.stream() // Stream<List<VAtBatGameDetails>> を生成
					.collect(Collectors.toList()); // List<List<VAtBatGameDetails>> に変換
			for (List<VAtBatGameDetails> groupedByBatter : groupedByBatterList) {
				AtBatResultList.add(calcAtBatResult(groupedByBatter));
			}
			AtBatResultList.sort((Comparator.comparing(MatchResult::getAtBatNumber).reversed()));
			//投手VS打者
		} else {
			AtBatResultList.add(calcAtBatResult(AtBatResult));
		}
		// チームID -> 打席数の順に並べ替える
		response.put("matchResult", AtBatResultList);
		response.put("message", "Success");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * 打率等を計算する
	 * @param atBatResult
	 * @return
	 */
	public MatchResult calcAtBatResult(List<VAtBatGameDetails> atBatResult) {
		MatchResult matchResult = new MatchResult();
		matchResult.setBatterNm(atBatResult.get(0).getBatterName().replace("　", ""));
		matchResult.setBatterTeamId(atBatResult.get(0).getBatterTeamId());
		matchResult.setBatterTeamNm(atBatResult.get(0).getBatterTeamShortName());
		matchResult.setPitcherNm(atBatResult.get(0).getPitcherName().replace("　", ""));
		matchResult.setPitcherTeamId(atBatResult.get(0).getPitcherTeamId());
		matchResult.setPitcherTeamNm(atBatResult.get(0).getPitcherTeamShortName());
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
