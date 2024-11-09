package com.example.baseball.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.baseball.entity.BaseballTeam;
import com.example.baseball.entity.MatchResult;
import com.example.baseball.entity.PlayerProjection;
import com.example.baseball.entity.VAtBatGameDetails;
import com.example.baseball.service.AtBatResultService;
import com.example.baseball.service.AtBatStatisticsService;
import com.example.baseball.service.BaseballGameService;
import com.example.baseball.service.BaseballPlayerService;
import com.example.baseball.service.BaseballTeamService;
import com.example.baseball.service.VAtBatGameDetailsService;
import com.example.baseball.service.VBaseballPlayerHistoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/baseball/api")
public class BaseballController {

	final BaseballTeamService baseballTeamService;
	final BaseballPlayerService baseballPlayerService;
	final AtBatResultService atBatResultService;
	final VBaseballPlayerHistoryService vBaseballPlayerHistoryService;
	final VAtBatGameDetailsService vAtBatGameDetailsService;
	final BaseballGameService baseballGameService;
	final AtBatStatisticsService atBatStatisticsService;

	// 初期表示データの取得
	@GetMapping("/getInitData")
	public ResponseEntity<Map<String, Object>> getInitData() {
		Map<String, Object> response = new HashMap<>();	
		List<BaseballTeam> baseballTeamList = baseballTeamService.findAllBaseballTeam();
		List<String> years = baseballGameService.findDistinctYears();
		response.put("baseballTeam", baseballTeamList);
		response.put("years", years);
		response.put("message", "Success");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	// ピッチャーの取得
	@GetMapping("/getPitcherList")
	public ResponseEntity<Map<String, Object>> getPitcherList(@RequestParam("teamId") long teamId,@RequestParam("year") String year) {
		Map<String, Object> response = new HashMap<>();
		List<PlayerProjection> pitcherListt = vBaseballPlayerHistoryService.findPitcherByTeamIdAndYear(teamId,"1",year);
		if (pitcherListt.isEmpty()) {
			response.put("message", "ピッチャーの取得に失敗しました");
			return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
		}
		response.put("pitcherListt", pitcherListt);
		response.put("message", "Success");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	// バッターの取得
	@GetMapping("/getBatterList")
	public ResponseEntity<Map<String, Object>> getBatterList(@RequestParam("teamId") long teamId,@RequestParam("year") String year) {
		Map<String, Object> response = new HashMap<>();
		List<PlayerProjection> batterList = vBaseballPlayerHistoryService.findBatterByTeamIdAndYear(teamId,year);
		if (batterList.isEmpty()) {
			response.put("message", "バッターの取得に失敗しました");
			return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
		}
		response.put("batterList", batterList);
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
			@RequestParam(value = "batterId", required = false) Long batterId,
			@RequestParam(value = "selectedYear", required = false) String selectedYear) {
		Map<String, Object> response = new HashMap<>();
		//        List<String> AtBatResult = atBatResultService.findMatchResult(pitcherId, batterId);
		List<VAtBatGameDetails> AtBatResult = vAtBatGameDetailsService.findByBatterAndPitcher(pitcherTeamId,
				batterTeamId, pitcherId, batterId, selectedYear);

		if (AtBatResult.isEmpty()) {
			response.put("message", "対戦結果がありませんでした。");
			return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
		}
		
		List<MatchResult> AtBatResultList = atBatStatisticsService.retrieveAtBatResults(AtBatResult, pitcherId, batterId);
		// チームID -> 打席数の順に並べ替える
		response.put("matchResult", AtBatResultList);
		response.put("message", "Success");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
