package com.example.baseball.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "BASEBALL_TEAM")
public class BaseballTeam {
	@Id
	@Column(name = "TEAM_ID")
	private Long teamId;

	@Column(name = "TEAM_NM")
	private String teamNm;

	@Column(name = "TEAM_SHOT_NM")
	private String teamShotNm;

	@Column(name = "LEAGUE")
	private String league;
}
