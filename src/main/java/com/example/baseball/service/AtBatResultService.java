package com.example.baseball.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.baseball.entity.AtBatResult;
import com.example.baseball.repository.AtBatResultRepository;

@Service
public class AtBatResultService {
	@Autowired
	AtBatResultRepository atBatResultRepository;
	
	public List<String> findMatchResult(Long pitcherId, Long batterId) {
		return atBatResultRepository.findByBatterIdAndPitcherId(pitcherId, batterId);
	}
    public List<AtBatResult> saveAtBatResult(List<AtBatResult> atBatResult) {
        return atBatResultRepository.saveAll(atBatResult);
    }
}
