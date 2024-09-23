package com.example.baseball.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.baseball.entity.BaseballPlayer;

@Repository
public interface BaseballPlayerRepository extends JpaRepository<BaseballPlayer, Long> {

	@Override
	List<BaseballPlayer> findAll();
}
