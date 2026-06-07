package com.example.baseball.controller;

import java.time.Year;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.baseball.dto.ResponseDto;
import com.example.baseball.service.PlayerProfileService;

/**
 * 1選手のプロフィール（全打席集計・ゾーン分析・対戦相手TOP/WORST）を返す API。
 * 投手か打者かはサーバー側で自動判定する。
 */
@RestController
@RequestMapping("/baseball/api")
public class PlayerProfileController {

    private static final Logger logger = LoggerFactory.getLogger(PlayerProfileController.class);

    @Autowired
    private PlayerProfileService playerProfileService;

    /**
     * GET /baseball/api/playerProfile/{playerId}?year=2026
     *
     * @param playerId 選手ID
     * @param year     年度フィルタ。省略時は現在年。"通算" 指定で全期間集計
     */
    @GetMapping("/playerProfile/{playerId}")
    public ResponseEntity<ResponseDto> getPlayerProfile(
            @PathVariable Long playerId,
            @RequestParam(required = false) String year) {

        String selectedYear = (year != null && !year.isEmpty())
                ? year
                : String.valueOf(Year.now().getValue());

        logger.info("playerProfile request: playerId={}, year={}", playerId, selectedYear);

        Map<String, Object> profile = playerProfileService.buildProfile(playerId, selectedYear);

        if (profile == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResponseDto.builder().message("選手が見つかりません").build());
        }

        return ResponseEntity.ok(ResponseDto.builder()
                .data("playerProfile", profile)
                .message("OK")
                .build());
    }
}
