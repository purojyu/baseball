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
@Table(name = "BASEBALL_PLAYER_HISTORY") // テーブル名を大文字で指定
public class BaseballPlayerHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "HISTORY_ID") // カラム名を大文字で指定
    private Long historyId;

    @Column(name = "PLAYER_ID") // カラム名を大文字で指定
    private Long playerId;

    @Column(name = "TEAM_ID") // カラム名を大文字で指定
    private Long teamId;

    @Column(name = "UNIFORM_NO") // カラム名を大文字で指定
    private String uniformNo;

    @Column(name = "START_DATE") // カラム名を大文字で指定
    private LocalDate startDate;

    @Column(name = "END_DATE") // カラム名を大文字で指定
    private LocalDate endDate;
}
