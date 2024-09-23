package com.example.baseball.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.baseball.entity.VAtBatGameDetails;

@Repository
public interface VAtBatGameDetailsRepository extends JpaRepository<VAtBatGameDetails, Long> {

	/**
	 * チームID、選手IDのパラメータによって打席結果を取得する
	 * パラメータによって投手VS野手、選手VSそのチームのすべての選手の情報を取得する
	 * @param batterId
	 * @param pitcherId
	 * @param batterTeamId
	 * @param pitcherTeamId
	 * @return
	 */
	@Query("SELECT vag FROM VAtBatGameDetails vag " +
		       "WHERE (:batterTeamId IS NULL OR vag.batterTeamId = :batterTeamId) " +
		       "AND (:pitcherTeamId IS NULL OR vag.pitcherTeamId = :pitcherTeamId) " +
		       "AND (:batterId IS NULL OR vag.batterId = :batterId) " +
		       "AND (:pitcherId IS NULL OR vag.pitcherId = :pitcherId)")
	List<VAtBatGameDetails> findByBatterAndPitcher(@Param("pitcherTeamId") Long pitcherTeamId,
			@Param("batterTeamId") Long batterTeamId, @Param("pitcherId") Long pitcherId,
			@Param("batterId") Long batterId);
}
