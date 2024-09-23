package com.example.baseball.entity;

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
@Table(name = "AT_BAT_RESULT") // テーブル名を大文字で指定
public class AtBatResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AT_BAT_ID") // カラム名を大文字で指定
    private Long atBatId;
    
    @Column(name = "GAME_ID") // カラム名を大文字で指定
    private Long gameId;
    
    @Column(name = "BATTER_ID") // カラム名を大文字で指定
    private Long batterId;
    
    @Column(name = "PITCHER_ID") // カラム名を大文字で指定
    private Long pitcherId;
    
    @Column(name = "RESULT") // カラム名を大文字で指定
    private String result;
}
