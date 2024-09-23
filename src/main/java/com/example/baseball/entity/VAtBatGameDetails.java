package com.example.baseball.entity;

import java.util.Date;

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
@Table(name = "V_AT_BAT_GAME_DETAILS")
public class VAtBatGameDetails {

    @Id
    @Column(name = "AT_BAT_ID")
    private Long atBatId;

    @Column(name = "GAME_ID")
    private Long gameId;

    @Column(name = "HOME_TEAM_ID")
    private Long homeTeamId;

    @Column(name = "AWAY_TEAM_ID")
    private Long awayTeamId;

    @Column(name = "HOME_TEAM_SCORE")
    private Long homeTeamScore;

    @Column(name = "AWAY_TEAM_SCORE")
    private Long awayTeamScore;

    @Column(name = "GAME_DATE")
    private Date gameDate; 

    @Column(name = "STADIUM")
    private String stadium;

    @Column(name = "BATTER_ID")
    private Long batterId;

    @Column(name = "BATTER_NAME")
    private String batterName;
    
    @Column(name = "BATTER_TEAM_ID")
    private Long batterTeamId;

    @Column(name = "BATTER_TEAM_NAME")
    private String batterTeamName;

    @Column(name = "BATTER_TEAM_SHORT_NAME")
    private String batterTeamShortName;

    @Column(name = "PITCHER_ID")
    private Long pitcherId;

    @Column(name = "PITCHER_NAME")
    private String pitcherName;
    
    @Column(name = "PITCHER_TEAM_ID")
    private Long pitcherTeamId;

    @Column(name = "PITCHER_TEAM_NAME")
    private String pitcherTeamName;

    @Column(name = "PITCHER_TEAM_SHORT_NAME")
    private String pitcherTeamShortName;

    @Column(name = "RESULT")
    private String result;
}
