package com.example.baseball.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.baseball.entity.PitchResult;
import com.example.baseball.repository.PitchResultRepository;

@Service
public class PitchResultService {
	@Autowired
	PitchResultRepository pitchResultRepository;

    public List<PitchResult> saveAll(List<PitchResult> pitchResult) {
        return pitchResultRepository.saveAll(pitchResult);
    }

    public long countByAtBatIdIn(List<Long> atBatIds) {
        if (atBatIds == null || atBatIds.isEmpty()) return 0;
        return pitchResultRepository.countByAtBatIdIn(atBatIds);
    }

    public List<Long> findProcessedAtBatIds(List<Long> atBatIds) {
        if (atBatIds == null || atBatIds.isEmpty()) return List.of();
        return pitchResultRepository.findProcessedAtBatIds(atBatIds);
    }
}
