package com.example.baseball.entity;


import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity // ここで@Entityアノテーションを追加
@Data               // Getter,Setterが不要になる
@NoArgsConstructor  // デフォルトコンストラクターの自動生成
@AllArgsConstructor // 全フィールドに対する初期化値を引数に取るコンストラクタを自動生成
public class BaseballPlayerHistory {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;
	private Long playerId;
	private Long teamId;
	private String uniformNo;
	private Date startDate;
	private Date endDate;
}
