package com.example.baseball.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.baseball.entity.BaseballTeam;

@Repository
public interface BaseballTeamRepository extends JpaRepository<BaseballTeam, Long> {
	
	@Override
    List<BaseballTeam> findAll();
	
    // チーム名で検索
    @Query("SELECT bt FROM BaseballTeam bt WHERE bt.teamNm LIKE %:teamNm%")
    BaseballTeam findByTeamNm(@Param("teamNm") String teamNm);
}
