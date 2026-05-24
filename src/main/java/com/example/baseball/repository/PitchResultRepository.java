package com.example.baseball.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.baseball.entity.PitchResult;

@Repository
public interface PitchResultRepository extends JpaRepository<PitchResult, Long> {

    /**
     * 指定打席IDリストに紐づく投球データを取得
     */
    @Query("SELECT pr FROM PitchResult pr WHERE pr.atBatId IN :atBatIds ORDER BY pr.atBatId, pr.pitchId")
    List<PitchResult> findByAtBatIdIn(@Param("atBatIds") List<Long> atBatIds);

    /**
     * 指定打席IDリストに紐づく投球データの件数
     */
    @Query("SELECT COUNT(pr) FROM PitchResult pr WHERE pr.atBatId IN :atBatIds")
    long countByAtBatIdIn(@Param("atBatIds") List<Long> atBatIds);

    /**
     * 指定打席IDリストのうち、既に pitch_result が登録済みの at_bat_id を返す。
     * Lambda timeout 後の retry で部分処理済みを除外するために使う。
     */
    @Query("SELECT DISTINCT pr.atBatId FROM PitchResult pr WHERE pr.atBatId IN :atBatIds")
    List<Long> findProcessedAtBatIds(@Param("atBatIds") List<Long> atBatIds);
}
