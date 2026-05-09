package com.example.baseball.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "PITCH_RESULT")
public class PitchResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PITCH_ID")
    private Long pitchId;

    @Column(name = "AT_BAT_ID", nullable = false)
    private Long atBatId;

    @Column(name = "PITCH_TYPE", nullable = false)
    private String pitchType;

    @Column(name = "COURSE", nullable = false)
    private Integer course;

    @Column(name = "RESULT", nullable = false)
    private String result;

    @Column(name = "SPEED", nullable = false)
    private Integer speed;
    
    @CreationTimestamp                 // 登録時だけ自動セット
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp                   // 更新のたびに自動更新
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
}