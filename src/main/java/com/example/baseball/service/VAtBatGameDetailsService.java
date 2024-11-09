package com.example.baseball.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.baseball.entity.VAtBatGameDetails;
import com.example.baseball.repository.VAtBatGameDetailsRepository;

@Service
public class VAtBatGameDetailsService {
	@Autowired
	private VAtBatGameDetailsRepository vAtBatGameDetailsRepository;

	/**
	 * チームID、選手IDのパラメータによって打席結果を取得する
	 * @param batterId
	 * @param pitcherId
	 * @param batterTeamId
	 * @param pitcherTeamId
	 * @return
	 */
	public List<VAtBatGameDetails> findByBatterAndPitcher(Long pitcherTeamId, Long batterTeamId, Long pitcherId,
			Long batterId, String selectedYear) {
		return vAtBatGameDetailsRepository.findByBatterAndPitcher(pitcherTeamId, batterTeamId, pitcherId, batterId, selectedYear);
	}
}
