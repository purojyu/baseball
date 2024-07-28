package com.example.baseball.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.baseball.entity.VBaseballPlayerHistory;

@Repository
public interface VBaseballPlayerHistoryRepository extends JpaRepository<VBaseballPlayerHistory, Long> {

    @Override
    List<VBaseballPlayerHistory> findAll();

    // チームコードで選手を検索
//    List<VBaseballPlayerHistory> findByTeamId(Long teamId);
    
    // JPQLクエリを使用してポジションとチームコードで選手を検索
    @Query("SELECT vbph FROM VBaseballPlayerHistory vbph " +
           "WHERE vbph.teamId = :teamId ORDER BY vbph.uniformNo")
    List<VBaseballPlayerHistory> findByTeamId(@Param("teamId") Long teamId);

    // JPQLクエリを使用してポジションとチームコードで選手を検索
    @Query("SELECT vbph FROM VBaseballPlayerHistory vbph " +
           "WHERE vbph.teamId = :teamId AND vbph.position = :position ORDER BY vbph.uniformNo")
    List<VBaseballPlayerHistory> findByTeamIdAndPosition(@Param("teamId") Long teamId, @Param("position") String position);

    // チームIDとDBの登録値のスペースを除いた選手名の前方一致で検索し、指定した日付が start_date 以降で end_date 未満、または end_date が NULL のものを取得
    @Query("SELECT vbph FROM VBaseballPlayerHistory vbph " +
           "WHERE REPLACE(TRIM(vbph.playerNm), '　', '') LIKE CONCAT(:playerNm, '%') " +
           "AND vbph.teamId = :teamId " +
           "AND :date >= vbph.startDate " +
           "AND (:date < vbph.endDate OR vbph.endDate IS NULL)")
    VBaseballPlayerHistory findByPartialPlayerNmAndTeamId(
            @Param("playerNm") String playerNm,
            @Param("teamId") Long teamId,
            @Param("date") Date date);

    // チームIDとDBの選手名にスペースがある場合は名字の完全一致、ない場合は名前の完全一致で検索
    @Query("SELECT vbph FROM VBaseballPlayerHistory vbph " +
           "WHERE (LOCATE('　', vbph.playerNm) > 0 AND TRIM(SUBSTRING(vbph.playerNm, 1, LOCATE('　', vbph.playerNm) - 1)) = :playerNm " +
           "AND vbph.teamId = :teamId AND :date >= vbph.startDate " +
           "AND (:date < vbph.endDate OR vbph.endDate IS NULL)) " +
           "OR (LOCATE('　', vbph.playerNm) = 0 AND TRIM(vbph.playerNm) = :playerNm " +
           "AND vbph.teamId = :teamId AND :date >= vbph.startDate " +
           "AND (:date < vbph.endDate OR vbph.endDate IS NULL))")
    VBaseballPlayerHistory findByExactPlayerNmAndTeamId(@Param("playerNm") String playerNm,
                                                        @Param("teamId") Long teamId,
                                                        @Param("date") Date date);

    // チームIDと選手名の名字と後方部分一致で検索
    @Query("SELECT vbph FROM VBaseballPlayerHistory vbph " +
           "WHERE TRIM(SUBSTRING(vbph.playerNm, 1, LOCATE('　', vbph.playerNm) - 1)) LIKE CONCAT(:playerNm, '%') " +
           "AND vbph.teamId = :teamId " +
           "AND :date >= vbph.startDate " +
           "AND (:date < vbph.endDate OR vbph.endDate IS NULL)")
    VBaseballPlayerHistory findByPartialEndPlayerNmAndTeamId(@Param("playerNm") String playerNm,
                                                             @Param("teamId") Long teamId,
                                                             @Param("date") Date date);
}
