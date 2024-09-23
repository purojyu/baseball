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
@Table(name = "V_BASEBALL_PLAYER_HISTORY_REGULAR")
public class VBaseballPlayerHistoryRegular {
    @Id
    @Column(name = "HISTORY_ID")
    private Long historyId;

    @Column(name = "PLAYER_ID")
    private Long playerId;

    @Column(name = "PLAYER_NM")
    private String playerNm;

    @Column(name = "POSITION")
    private String position;

    @Column(name = "BIRTH_DATE")
    private Date birthDate;

    @Column(name = "HEIGHT")
    private Long height;

    @Column(name = "WEIGHT")
    private Long weight;

    @Column(name = "THROWER")
    private String thrower;

    @Column(name = "HANDED")
    private String handed;

    @Column(name = "TEAM_ID")
    private Long teamId;

    @Column(name = "UNIFORM_NO")
    private String uniformNo;

    @Column(name = "START_DATE")
    private Date startDate;

    @Column(name = "END_DATE")
    private Date endDate;
}
