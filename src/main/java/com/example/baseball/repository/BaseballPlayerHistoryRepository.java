package com.example.baseball.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.baseball.entity.BaseballPlayerHistory;

@Repository
public interface BaseballPlayerHistoryRepository extends JpaRepository<BaseballPlayerHistory, Long> {
}
