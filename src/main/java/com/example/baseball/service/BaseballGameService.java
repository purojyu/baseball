package com.example.baseball.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.baseball.entity.BaseballGame;
import com.example.baseball.repository.BaseballGameRepository;

@Service
public class BaseballGameService {
	@Autowired
	BaseballGameRepository baseballGameRepository;
	
	public List<BaseballGame> findAll() {
		return baseballGameRepository.findAll();
	}
	
	public List<BaseballGame>  findByGameDate(Date gameDate) {
		return baseballGameRepository.findByGameDate(gameDate);
	}
	
	public List<BaseballGame>  findByGameDateAndTeamId(Date gameDate, Long homeTeamId, Long awayTeamId) {
		return baseballGameRepository.findByGameDateAndTeamId(gameDate, homeTeamId, awayTeamId);
	}
	
    public BaseballGame saveBaseballGame(BaseballGame baseballGame) {
        return baseballGameRepository.save(baseballGame);
    }
    
    public List<String> findDistinctYears(){
    	return baseballGameRepository.findDistinctYears();
    }
}
