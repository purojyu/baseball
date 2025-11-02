package com.example.baseball.entity;

import java.time.LocalDate;

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
@Table(name = "BASEBALL_PLAYER")
public class BaseballPlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PLAYER_ID")
    private Long playerId;

    @Column(name = "PLAYER_NM")
    private String playerNm;

    @Column(name = "POSITION")
    private String position;

    @Column(name = "BIRTH_DATE")
    private LocalDate birthDate;

    @Column(name = "HEIGHT")
    private Long height;

    @Column(name = "WEIGHT")
    private Long weight;

    @Column(name = "THROWER")
    private String thrower;

    @Column(name = "HANDED")
    private String handed;

    @Column(name = "NPB_URL")
    private String npbUrl;

    @Column(name = "PLAYER_NM_KANA")
    private String playerNmKana;
    
    /** Yahoo 側の選手 ID（https://baseball.yahoo.co.jp/npb/player/{YAHOO_ID}/top） */
    @Column(name = "YAHOO_ID", unique = true)
    private Long yahooId;
    
    /** NPB 側の選手 ID（https://npb.jp/bis/players/{NPB_ID}.html） */
    @Column(name = "NPB_ID", unique = true)
    private Long npbId;
}