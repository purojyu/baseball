package com.example.baseball.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.baseball.dto.GetPlayerListRequest;
import com.example.baseball.dto.MatchResultSearchRequest;
import com.example.baseball.dto.ResponseDto;
import com.example.baseball.entity.BaseballTeam;
import com.example.baseball.entity.MatchResult;
import com.example.baseball.entity.PlayerProjection;
import com.example.baseball.entity.VAtBatGameDetails;
import com.example.baseball.service.AtBatStatisticsService;
import com.example.baseball.service.BaseballGameService;
import com.example.baseball.service.BaseballTeamService;
import com.example.baseball.service.VAtBatGameDetailsService;
import com.example.baseball.service.VBaseballPlayerHistoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/baseball/api")
public class BaseballController {
	
	private static final String SUCCESS_MESSAGE = "Success";
	private static final String NO_PITCHERS_FOUND = "ピッチャーの取得に失敗しました";
	private static final String NO_BATTERS_FOUND = "バッターの取得に失敗しました";
	private static final String NO_MATCH_RESULT = "対戦結果がありませんでした。";

	private final BaseballTeamService baseballTeamService;
	private final VBaseballPlayerHistoryService vBaseballPlayerHistoryService;
	private final VAtBatGameDetailsService vAtBatGameDetailsService;
	private final BaseballGameService baseballGameService;
	private final AtBatStatisticsService atBatStatisticsService;

	/**
	 * 初期表示データ（チーム一覧、年度一覧）を取得する
	 * @return チームリストおよび年度リスト
	 */
	@GetMapping("/getInitData")
	public ResponseEntity<ResponseDto> getInitData() {
		List<BaseballTeam> baseballTeamList = baseballTeamService.findAllBaseballTeam();
		List<String> years = baseballGameService.findDistinctYears();

		ResponseDto response = ResponseDto.builder()
				.data("baseballTeam", baseballTeamList)
				.data("years", years)
				.message(SUCCESS_MESSAGE)
				.build();

		return ResponseEntity.ok(response);
	}

	/**
	 * 指定チーム・年度のピッチャー一覧を取得
	 * @param teamId
	 * @param year
	 * @return ピッチャーリスト
	 */
	@GetMapping("/getPitcherList")
	public ResponseEntity<ResponseDto> getPitcherList(@Validated GetPlayerListRequest request) {
	    long teamId = Long.parseLong(request.getTeamId());
	    String year = request.getYear();

	    List<PlayerProjection> pitcherList = vBaseballPlayerHistoryService.findPitcherByTeamIdAndYear(teamId, "1", year);
	    if (pitcherList.isEmpty()) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body(ResponseDto.builder().message(NO_PITCHERS_FOUND).build());
	    }

	    ResponseDto response = ResponseDto.builder()
	            .data("pitcherList", pitcherList)
	            .message("Success")
	            .build();
	    return ResponseEntity.ok(response);
	}

	/**
	 * 指定チーム・年度のバッター一覧を取得
	 * @param teamId
	 * @param year
	 * @return バッターリスト
	 */
	@GetMapping("/getBatterList")
	public ResponseEntity<ResponseDto> getBatterList(@Validated GetPlayerListRequest request) {
	    long teamId = Long.parseLong(request.getTeamId());
	    String year = request.getYear();

	    List<PlayerProjection> batterList = vBaseballPlayerHistoryService.findBatterByTeamIdAndYear(teamId, year);
	    if (batterList.isEmpty()) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body(ResponseDto.builder().message(NO_BATTERS_FOUND).build());
	    }

	    ResponseDto response = ResponseDto.builder()
	            .data("batterList", batterList)
	            .message("Success")
	            .build();
	    return ResponseEntity.ok(response);
	}

	/**
	 * 投手VS野手の対戦成績を取得
	 * @param pitcherTeamId
	 * @param batterTeamId
	 * @param pitcherId
	 * @param batterId
	 * @param selectedYear
	 * @return 対戦成績リスト
	 */
	 @GetMapping("/matchResultSearch")
	    public ResponseEntity<ResponseDto> matchResultSearch(@Validated MatchResultSearchRequest request) {
	        Long pitcherTeamId = parseLongOrNull(request.getPitcherTeamId());
	        Long batterTeamId = parseLongOrNull(request.getBatterTeamId());
	        Long pitcherId = parseLongOrNull(request.getPitcherId());
	        Long batterId = parseLongOrNull(request.getBatterId());
	        String selectedYear = request.getSelectedYear();

	        List<VAtBatGameDetails> atBatResult = vAtBatGameDetailsService.findByBatterAndPitcher(
	                pitcherTeamId, batterTeamId, pitcherId, batterId, selectedYear);

	        if (atBatResult.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body(ResponseDto.builder().message(NO_MATCH_RESULT).build());
	        }

	        List<MatchResult> atBatResultList = atBatStatisticsService.retrieveAtBatResults(atBatResult, pitcherId, batterId);

	        ResponseDto response = ResponseDto.builder()
	                .data("matchResult", atBatResultList)
	                .message("Success")
	                .build();

	        return ResponseEntity.ok(response);
	    }

	    private Long parseLongOrNull(String value) {
	        if (value == null || value.trim().isEmpty()) {
	            return null;
	        }
	        return Long.parseLong(value);
	    }
	}