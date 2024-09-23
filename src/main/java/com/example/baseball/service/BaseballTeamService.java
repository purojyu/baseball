package com.example.baseball.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.baseball.entity.BaseballTeam;
import com.example.baseball.repository.BaseballTeamRepository;

@Service
public class BaseballTeamService {
	@Autowired
	BaseballTeamRepository baseballTeamRepository;
	
	public List<BaseballTeam> findAllBaseballTeam() {
		return baseballTeamRepository.findAll();
	}
	
	public BaseballTeam findByTeamNm(String teamNm) {
		return baseballTeamRepository.findByTeamNm(teamNm);
	}
}
