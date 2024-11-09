package com.example.baseball.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.baseball.entity.BaseballGame;

@Repository
public interface BaseballGameRepository extends JpaRepository<BaseballGame, Long> {
	@Override
    List<BaseballGame> findAll();
	
	// 試合日時で検索
	@Query("SELECT bg FROM BaseballGame bg WHERE bg.gameDate = :gameDate")
	List<BaseballGame> findByGameDate(@Param("gameDate") Date gameDate);
	
	// 存在する年度のリストを取得
	@Query(value = "SELECT DISTINCT YEAR(GAME_DATE) AS YEAR FROM BASEBALL_GAME ORDER BY YEAR DESC", nativeQuery = true)
	List<String> findDistinctYears();
}
