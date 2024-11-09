package com.example.baseball.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.baseball.entity.BaseballPlayer;

@Repository
public interface BaseballPlayerRepository extends JpaRepository<BaseballPlayer, Long> {

	@Override
	List<BaseballPlayer> findAll();

	// チーム名で検索
	@Query("SELECT bp FROM BaseballPlayer bp WHERE bp.playerNm = :playerNm")
	BaseballPlayer findByplayerNm(@Param("playerNm") String playerNm);
	
	// 選手名と生年月日で検索
	@Query("SELECT bp FROM BaseballPlayer bp WHERE bp.playerNm = :playerNm AND bp.birthDate = :birthDate")
	BaseballPlayer findByPlayerNmAndBirthDate(@Param("playerNm") String playerNm, @Param("birthDate") LocalDate birthDate);

}
