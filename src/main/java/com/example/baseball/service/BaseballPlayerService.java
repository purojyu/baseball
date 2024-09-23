package com.example.baseball.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.baseball.entity.BaseballPlayer;
import com.example.baseball.repository.BaseballPlayerRepository;

@Service
public class BaseballPlayerService {
	@Autowired
	BaseballPlayerRepository baseballPlayerRepository;
	
	public List<BaseballPlayer> findAll(){
		return baseballPlayerRepository.findAll();
	}
	
	public BaseballPlayer savePlayer(BaseballPlayer baseballPlayer){
		return baseballPlayerRepository.save(baseballPlayer);
	}
}
