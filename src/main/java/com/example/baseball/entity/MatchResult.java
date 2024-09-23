package com.example.baseball.entity;


import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Getter,Setterが不要になる
@NoArgsConstructor  // デフォルトコンストラクターの自動生成
@AllArgsConstructor // 全フィールドに対する初期化値を引数に取るコンストラクタを自動生成
public class MatchResult {
	// 打者
	private String batterNm;
	// 打者チームID
	private Long batterTeamId;
	// 打者チーム
	private String batterTeamNm;
	// 投手
	private String pitcherNm;
	// 投手チームId
	private Long pitcherTeamId;
	// 投手チーム
	private String pitcherTeamNm;
	// 打率
	private BigDecimal battingAverage;
	// 打席数
	private int atBatNumber;
	// 打数
	private int strokesNumber;
	// ヒット数
	private int  hitNumber;
	// 単打数
	private int  singlesNumber;
	// 二塁打数
	private int doublesNumber;
	// 三塁打数
	private int triplesNumber;
	// 本塁打数
	private int homeRun;
	// 塁打数
	private int baseHitsNumber;
	// 四球数
	private int fourBallNumber;
	// 死球数
	private int hitBallNumber;
	// 犠飛数
	private int sacrificeFly;
	// 三振数
	private int strikeoutsNumber;
	// OPS
	private BigDecimal ops;
	// 出塁率
	private BigDecimal onBasePercentage;
	// 長打率
	private BigDecimal sluggingPercentage;
}
