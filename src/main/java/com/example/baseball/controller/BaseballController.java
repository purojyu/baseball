package com.example.baseball.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.baseball.entity.BaseballTeam;
import com.example.baseball.entity.MatchResult;
import com.example.baseball.entity.VBaseballPlayerHistory;
import com.example.baseball.service.AtBatResultService;
import com.example.baseball.service.BaseballPlayerService;
import com.example.baseball.service.BaseballTeamService;
import com.example.baseball.service.VBaseballPlayerHistoryService;
import com.example.baseball.util.BaseballUtil;

import lombok.RequiredArgsConstructor;

@RestController
@CrossOrigin(origins = "http://localhost:8081")
@RequiredArgsConstructor
@RequestMapping("/baseball/api")
public class BaseballController {

    final BaseballTeamService baseballTeamService;
    final BaseballPlayerService baseballPlayerService;
    final AtBatResultService atBatResultService;
    final VBaseballPlayerHistoryService vBaseballPlayerHistoryService;

    // 初期表示データの取得
    @GetMapping("/getInitData")
    public ResponseEntity<Map<String, Object>> getInitData() {
        Map<String, Object> response = new HashMap<>();
        List<BaseballTeam> baseballTeamList = baseballTeamService.findAllBaseballTeam();
        response.put("baseballTeam", baseballTeamList);
        response.put("message", "Success");
;        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ピッチャーの取得
    @GetMapping("/getPitcherList")
    public ResponseEntity<Map<String, Object>> getPitcherList(@RequestParam("teamId") long teamId) {
        Map<String, Object> response = new HashMap<>();
        List<VBaseballPlayerHistory> baseballPlayerList = vBaseballPlayerHistoryService.findByTeamIdAndPosition(teamId, "0");
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
        List<VBaseballPlayerHistory> baseballPlayerList = vBaseballPlayerHistoryService.findByTeamId(teamId);
        if (baseballPlayerList.isEmpty()) {
            response.put("message", "バッターの取得に失敗しました");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        response.put("baseballPlayer", baseballPlayerList);
        response.put("message", "Success");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ピッチャーVSバッターの対戦結果の取得
    @GetMapping("/matchResultSearch")
    public ResponseEntity<Map<String, Object>> matchResultSearch(@RequestParam("pitcherId") long pitcherId, @RequestParam("batterId") long batterId) {
        Map<String, Object> response = new HashMap<>();
        MatchResult matchResult = new MatchResult();
        List<MatchResult> matchResultList = new ArrayList<>();
        List<String> AtBatResult = atBatResultService.findMatchResult(pitcherId, batterId);

        if (AtBatResult.isEmpty()) {
            response.put("message", "対戦結果がありませんでした。");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        matchResult.setBattingAverage(BaseballUtil.calculateBattingAverage(AtBatResult));
        matchResult.setAtBatNumber(AtBatResult.size());
        matchResult.setStrokesNumber(BaseballUtil.calculateStrokesNumber(AtBatResult));
        matchResult.setHitNumber(BaseballUtil.calculateHitNumber(AtBatResult));
        matchResult.setSinglesNumber(BaseballUtil.calculateSinglesNumber(AtBatResult));
        matchResult.setDoublesNumber(BaseballUtil.calculateDoublesNumber(AtBatResult));
        matchResult.setTriplesNumber(BaseballUtil.calculateTriplesNumber(AtBatResult));
        matchResult.setHomeRun(BaseballUtil.calculateHomeRun(AtBatResult));
        matchResult.setBaseHitsNumber(BaseballUtil.calculateBaseHitsNumber(AtBatResult));
        matchResult.setFourBallNumber(BaseballUtil.calculateFourBallNumber(AtBatResult));
        matchResult.setHitBallNumber(BaseballUtil.calculateHitBallNumber(AtBatResult));
        matchResult.setStrikeoutsNumber(BaseballUtil.calculateStrikeoutsNumber(AtBatResult));
        matchResult.setOps(BaseballUtil.calculateOps(AtBatResult));
        matchResult.setOnBasePercentage(BaseballUtil.calculateOnBasePercentage(AtBatResult));
        matchResult.setSluggingPercentage(BaseballUtil.calculateSluggingPercentage(AtBatResult));

        matchResultList.add(matchResult);
        response.put("matchResult", matchResultList);
        response.put("message", "Success");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
