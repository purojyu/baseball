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
}
