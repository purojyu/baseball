package com.example.baseball.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.baseball.entity.VBaseballPlayerHistoryRegular;
import com.example.baseball.repository.VBaseballPlayerHistoryRegularRepository;

@Service
public class VBaseballPlayerHistoryRegularService {
    @Autowired
    private VBaseballPlayerHistoryRegularRepository vBaseballPlayerHistoryRegularRepository;
    
    public List<VBaseballPlayerHistoryRegular> findAll(){
    	return vBaseballPlayerHistoryRegularRepository.findAll();
    }
	
	public List<VBaseballPlayerHistoryRegular> findByTeamId(Long teamId) {
		return vBaseballPlayerHistoryRegularRepository.findByTeamId(teamId);
	}
	
public List<VBaseballPlayerHistoryRegular> findByTeamIdAndPosition(Long TeamId, String position){
	return vBaseballPlayerHistoryRegularRepository.findByTeamIdAndPosition(TeamId, position);
}

public VBaseballPlayerHistoryRegular findByTeamNmAndTeamId(Long teamId, String playerNm ,Date date) {
	VBaseballPlayerHistoryRegular player = vBaseballPlayerHistoryRegularRepository.findByExactPlayerNmAndTeamId(playerNm, teamId,date);
	VBaseballPlayerHistoryRegular player2 = null;
	if (player != null) {
		return player;
	} else if (player == null) {
		player2 = vBaseballPlayerHistoryRegularRepository.findByPartialPlayerNmAndTeamId(playerNm, teamId,date);
	}
	if (player2 == null) {
		VBaseballPlayerHistoryRegular player3 = vBaseballPlayerHistoryRegularRepository.findByPartialEndPlayerNmAndTeamId(playerNm, teamId,date);
		return player3;
	}
	return player2;
}
}
