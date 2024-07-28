package com.example.baseball.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.baseball.entity.AtBatResult;

@Repository
public interface AtBatResultRepository extends JpaRepository<AtBatResult, Long> {
	
	@Override
    List<AtBatResult> findAll();

    // JPQLクエリを使用してバッターIDとピッチャーIDで検索
    @Query("SELECT abr.result FROM AtBatResult abr WHERE abr.pitcherId = :pitcherId AND abr.batterId = :batterId")
    List<String> findByBatterIdAndPitcherId(@Param("pitcherId") Long pitcherId, @Param("batterId") Long batterId);


}
