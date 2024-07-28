package com.example.baseball.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.baseball.entity.VBaseballPlayerHistory;
import com.example.baseball.repository.VBaseballPlayerHistoryRepository;

@Service
public class VBaseballPlayerHistoryService {
    @Autowired
    private VBaseballPlayerHistoryRepository vBaseballPlayerHistoryRepository;
    
    public List<VBaseballPlayerHistory> findAll(){
    	return vBaseballPlayerHistoryRepository.findAll();
    }
	
	public List<VBaseballPlayerHistory> findByTeamId(Long teamId) {
		return vBaseballPlayerHistoryRepository.findByTeamId(teamId);
	}
	
public List<VBaseballPlayerHistory> findByTeamIdAndPosition(Long TeamId, String position){
	return vBaseballPlayerHistoryRepository.findByTeamIdAndPosition(TeamId, position);
}

public VBaseballPlayerHistory findByTeamNmAndTeamId(Long teamId, String playerNm ,Date date) {
	VBaseballPlayerHistory player = vBaseballPlayerHistoryRepository.findByExactPlayerNmAndTeamId(playerNm, teamId,date);
	VBaseballPlayerHistory player2 = null;
	if (player != null) {
		return player;
	} else if (player == null) {
		player2 = vBaseballPlayerHistoryRepository.findByPartialPlayerNmAndTeamId(playerNm, teamId,date);
	}
	if (player2 == null) {
		VBaseballPlayerHistory player3 = vBaseballPlayerHistoryRepository.findByPartialEndPlayerNmAndTeamId(playerNm, teamId,date);
		return player3;
	}
	return player2;
}
}
