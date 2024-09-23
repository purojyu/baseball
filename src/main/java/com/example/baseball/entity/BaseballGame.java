package com.example.baseball.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "BASEBALL_GAME") // テーブル名を大文字で指定
public class BaseballGame {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GAME_ID") // カラム名を大文字で指定
    private Long gameId;
    
    @Column(name = "HOME_TEAM_ID") // カラム名を大文字で指定
    private Long homeTeamId;
    
    @Column(name = "AWAY_TEAM_ID") // カラム名を大文字で指定
    private Long awayTeamId;
    
    @Column(name = "HOME_TEAM_SCORE") // カラム名を大文字で指定
    private Long homeTeamScore;
    
    @Column(name = "AWAY_TEAM_SCORE") // カラム名を大文字で指定
    private Long awayTeamScore;
    
    @Column(name = "GAME_DATE") // カラム名を大文字で指定
    private Date gameDate;
    
    @Column(name = "STADIUM") // カラム名を大文字で指定
    private String stadium;
}
