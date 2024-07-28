package com.example.baseball.entity;

import java.util.Date;

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
@Table(name = "v_baseball_player_history")
public class VBaseballPlayerHistory {
    @Id
    private Long historyId;
    private Long playerId;
    private String playerNm;
    private String position;
    private Date birthDate;
    private Long heigth;
    private Long weigth;
    private String thrower;
    private String handed;
    private Long teamId;
    private String uniformNo;
    private Date startDate;
    private Date endDate;
}
