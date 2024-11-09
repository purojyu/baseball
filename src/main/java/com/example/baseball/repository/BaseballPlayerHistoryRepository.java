package com.example.baseball.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.baseball.entity.BaseballPlayerHistory;

@Repository
public interface BaseballPlayerHistoryRepository extends JpaRepository<BaseballPlayerHistory, Long> {
	
	// 選手IDとチームIDで検索
	@Query("SELECT bph FROM BaseballPlayerHistory bph WHERE bph.playerId = :playerId AND bph.teamId = :teamId AND bph.endDate IS NULL")
	BaseballPlayerHistory findByplayerIdAndteamId(@Param("playerId") Long playerId,@Param("teamId") Long teamId);
	
	// 選手IDで検索
	@Query("SELECT bph FROM BaseballPlayerHistory bph WHERE bph.playerId = :playerId AND bph.endDate IS NULL")
	BaseballPlayerHistory findByplayerId(@Param("playerId") Long playerId);
	
    /**
     * PLAYER_ID と TEAM_ID で検索し、START_DATE の年が指定された year と一致するレコードを取得します。
     * トレードの日付を正しい値にするためだけに作成
     *
     * @param playerId 検索対象の選手ID
     * @param teamId   検索対象のチームID
     * @param year     START_DATE の年（例: "2016"）
     * @return 条件に一致する BaseballPlayerHistory エンティティ
     */
    @Query(
        value = "SELECT * FROM BASEBALL_PLAYER_HISTORY bph " +
                "WHERE bph.PLAYER_ID = :playerId " +
                "AND bph.TEAM_ID = :teamId " +
                "AND YEAR(bph.START_DATE) = :year",
        nativeQuery = true
    )
    BaseballPlayerHistory findByPlayerIdAndTeamIdAndStartDateYear(
        @Param("playerId") Long playerId,
        @Param("teamId") Long teamId,
        @Param("year") String year
    );
	
    /**
     * PLAYER_ID と TEAM_ID で検索し、END_DATE の年が指定された year と一致するレコードを取得します。
     * トレードの日付を正しい値にするためだけに作成
     *
     * @param playerId 検索対象の選手ID
     * @param teamId   検索対象のチームID
     * @param year     END_DATE の年（例: "2016"）
     * @return 条件に一致する BaseballPlayerHistory エンティティ
     */
    @Query(
        value = "SELECT * FROM BASEBALL_PLAYER_HISTORY bph " +
                "WHERE bph.PLAYER_ID = :playerId " +
                "AND bph.TEAM_ID = :teamId " +
                "AND YEAR(bph.END_DATE) = :year",
        nativeQuery = true
    )
    BaseballPlayerHistory findByPlayerIdAndTeamIdAndEndDateYear(
        @Param("playerId") Long playerId,
        @Param("teamId") Long teamId,
        @Param("year") String year
    );
}
