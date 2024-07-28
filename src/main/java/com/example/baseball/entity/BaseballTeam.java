package com.example.baseball.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity // ここで@Entityアノテーションを追加
@Data               // Getter,Setterが不要になる
@NoArgsConstructor  // デフォルトコンストラクターの自動生成
@AllArgsConstructor // 全フィールドに対する初期化値を引数に取るコンストラクタを自動生成
public class BaseballTeam {
	@Id             // 主キーに当たるフィールドに付与する(今回はid)に付与
	private Long teamId;
	private String teamNm;
	private String teamShotNm;
	private String league;
}
