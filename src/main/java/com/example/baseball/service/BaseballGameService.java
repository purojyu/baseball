package com.example.baseball.service;

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
	
    public BaseballGame saveBaseballGame(BaseballGame baseballGame) {
        return baseballGameRepository.save(baseballGame);
    }
}
