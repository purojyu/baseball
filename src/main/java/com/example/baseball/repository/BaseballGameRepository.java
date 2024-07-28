package com.example.baseball.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.baseball.entity.BaseballGame;

@Repository
public interface BaseballGameRepository extends JpaRepository<BaseballGame, Long> {
}
