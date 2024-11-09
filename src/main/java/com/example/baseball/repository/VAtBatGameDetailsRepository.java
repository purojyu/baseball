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
     * チームID、選手ID、年度のパラメータによって打席結果を取得する
     * パラメータによって投手VS野手、選手VSそのチームのすべての選手の情報を取得する
     * @param pitcherTeamId
     * @param batterTeamId
     * @param pitcherId
     * @param batterId
     * @param selectedYear
     * @return
     */
    @Query(value = "SELECT * FROM V_AT_BAT_GAME_DETAILS vag " +
           "WHERE (:batterTeamId = 0 OR vag.BATTER_TEAM_ID = :batterTeamId) " +
           "AND (:pitcherTeamId = 0 OR vag.PITCHER_TEAM_ID = :pitcherTeamId) " +
           "AND (:batterId IS NULL OR vag.BATTER_ID = :batterId) " +
           "AND (:pitcherId IS NULL OR vag.PITCHER_ID = :pitcherId) " +
           "AND (:selectedYear = '通算' OR YEAR(vag.GAME_DATE) = :selectedYear)",
           nativeQuery = true)
    List<VAtBatGameDetails> findByBatterAndPitcher(
            @Param("pitcherTeamId") Long pitcherTeamId,
            @Param("batterTeamId") Long batterTeamId,
            @Param("pitcherId") Long pitcherId,
            @Param("batterId") Long batterId,
            @Param("selectedYear") String selectedYear);
}
