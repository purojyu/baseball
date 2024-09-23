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
@Table(name = "BASEBALL_PLAYER") // テーブル名を大文字で指定
public class BaseballPlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PLAYER_ID") // カラム名を大文字で指定
    private Long playerId;

    @Column(name = "PLAYER_NM") // カラム名を大文字で指定
    private String playerNm;

    @Column(name = "POSITION") // カラム名を大文字で指定
    private String position;

    @Column(name = "BIRTH_DATE") // カラム名を大文字で指定
    private LocalDate birthDate;

    @Column(name = "HEIGHT") // カラム名を大文字で指定
    private Long height;

    @Column(name = "WEIGHT") // カラム名を大文字で指定
    private Long weight;

    @Column(name = "THROWER") // カラム名を大文字で指定
    private String thrower;

    @Column(name = "HANDED") // カラム名を大文字で指定
    private String handed;
}
