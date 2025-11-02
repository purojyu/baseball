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
@Table(name = "AT_BAT_RESULT")
public class AtBatResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AT_BAT_ID")
    private Long atBatId;
    
    @Column(name = "GAME_ID") 
    private Long gameId;
    
    @Column(name = "BATTER_ID") 
    private Long batterId;
    
    @Column(name = "PITCHER_ID") 
    private Long pitcherId;
    
    @Column(name = "RESULT") 
    private String result;
}
