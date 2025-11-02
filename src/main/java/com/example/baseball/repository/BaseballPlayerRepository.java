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
	
	// yahooIdで検索
	@Query("SELECT bp FROM BaseballPlayer bp WHERE bp.yahooId = :yahooId")
	BaseballPlayer findByYahooId(@Param("yahooId") Long yahooId);
	
	// npbIdで検索
	@Query("SELECT bp FROM BaseballPlayer bp WHERE bp.npbId = :npbId")
	BaseballPlayer findByNpbId(@Param("npbId") Long npbId);
	
	// yahooの選手名を用いて、選手名の部分一致、かつ生年月日で検索
	@Query(
			  value = "SELECT * FROM BASEBALL_PLAYER " +
			          "WHERE PLAYER_NM LIKE CONCAT('%', :cleanedName, '%') " +
			          "AND BIRTH_DATE = :birthDate",
			  nativeQuery = true
			)
			BaseballPlayer findByPlayerNmAndBirthDateByYahooNm(
			    @Param("cleanedName") String cleanedName,
			    @Param("birthDate") LocalDate birthDate
			);
	
	// 選手名、生年月日、身長、体重で精密検索
	@Query(
			  value = "SELECT * FROM BASEBALL_PLAYER " +
			          "WHERE PLAYER_NM LIKE CONCAT('%', :cleanedName, '%') " +
			          "AND BIRTH_DATE = :birthDate " +
			          "AND (:height IS NULL OR HEIGHT = :height) " +
			          "AND (:weight IS NULL OR WEIGHT = :weight) " +
			          "ORDER BY PLAYER_ID LIMIT 1",
			  nativeQuery = true
			)
			BaseballPlayer findByPlayerProfileWithPhysical(
			    @Param("cleanedName") String cleanedName,
			    @Param("birthDate") LocalDate birthDate,
			    @Param("height") Integer height,
			    @Param("weight") Integer weight
			);
}
