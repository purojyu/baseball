package com.example.baseball.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.baseball.entity.BaseballPlayerHistory;
import com.example.baseball.repository.BaseballPlayerHistoryRepository;

@Service
public class BaseballPlayerHistoryService {
	@Autowired
	BaseballPlayerHistoryRepository baseballPlayerHistoryRepository;
	
    public BaseballPlayerHistory saveBaseballPlayerHistory(BaseballPlayerHistory baseballPlayerHistory) {
        return baseballPlayerHistoryRepository.save(baseballPlayerHistory);
    }
}
