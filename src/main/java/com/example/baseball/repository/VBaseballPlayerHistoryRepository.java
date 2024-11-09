package com.example.baseball.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.baseball.entity.PlayerProjection;
import com.example.baseball.entity.VBaseballPlayerHistory;

@Repository
public interface VBaseballPlayerHistoryRepository extends JpaRepository<VBaseballPlayerHistory, Long> {

	/**
	 * バッターのリストをチームIDと年度で検索
	 * @param teamId
	 * @param year
	 * @return
	 */
	@Query(value = "SELECT DISTINCT vbph.PLAYER_ID AS playerId, " +
            "SUBSTRING_INDEX(vbph.PLAYER_NM , '（', 1) AS playerNm " +
            "FROM V_BASEBALL_PLAYER_HISTORY vbph " +
            "WHERE (:teamId = 0 OR vbph.TEAM_ID = :teamId) " +
            "AND (:year = '通算' OR :year BETWEEN YEAR(vbph.START_DATE) AND YEAR(IFNULL(vbph.END_DATE, '9999-12-31'))) " +
            "ORDER BY playerNm ASC", nativeQuery = true)
List<PlayerProjection> findBatterByTeamIdAndYear(@Param("teamId") Long teamId,
                                              @Param("year") String year);



	/**
	 * ピッチャーのリストをチームIDと年度で検索
	 * @param teamId
	 * @param position
	 * @param year
	 * @return
	 */
	@Query(value = "SELECT DISTINCT vbph.PLAYER_ID AS playerId, " +
		    "SUBSTRING_INDEX(vbph.PLAYER_NM , '（', 1) AS playerNm " +
            "FROM V_BASEBALL_PLAYER_HISTORY vbph " +
            "WHERE (:teamId = 0 OR vbph.TEAM_ID = :teamId) " +
            "AND vbph.POSITION = :position " +
            "AND (:year = '通算' OR :year BETWEEN YEAR(vbph.START_DATE) AND YEAR(IFNULL(vbph.END_DATE, '9999-12-31'))) " +
            "ORDER BY playerNm ASC", nativeQuery = true)
List<PlayerProjection> findPitcherByTeamIdAndYear(@Param("teamId") Long teamId,
     @Param("position") String position, @Param("year") String year);




	/**
	 * チームIDと選手名と日付で検索
	 * @param playerNm
	 * @param teamId
	 * @param date
	 * @return
	 */
	@Query("SELECT vbph FROM VBaseballPlayerHistory vbph " +
			"WHERE vbph.playerNm = :playerNm " +
			"AND vbph.teamId = :teamId " +
			"AND :date >= vbph.startDate " +
			"AND (:date < vbph.endDate OR vbph.endDate IS NULL)")
	VBaseballPlayerHistory findByPlayerNmAndTeamIdAndDate(@Param("playerNm") String playerNm,
			@Param("teamId") Long teamId,
			@Param("date") Date date);
}
