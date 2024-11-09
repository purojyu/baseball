package com.example.baseball.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.baseball.entity.PlayerProjection;
import com.example.baseball.entity.VBaseballPlayerHistory;
import com.example.baseball.repository.VBaseballPlayerHistoryRepository;

@Service
public class VBaseballPlayerHistoryService {
	@Autowired
	private VBaseballPlayerHistoryRepository vBaseballPlayerHistoryRepository;

	/**
	 * バッターのリストをチームIDと年度で検索
	 * @param TeamId
	 * @param year
	 * @return
	 */
	public List<PlayerProjection> findBatterByTeamIdAndYear(Long TeamId, String year) {
		return vBaseballPlayerHistoryRepository.findBatterByTeamIdAndYear(TeamId, year);
	}

	/**
	 * ピッチャーのリストをチームIDと年度で検索
	 * @param TeamId
	 * @param position
	 * @param year
	 * @return
	 */
	public List<PlayerProjection> findPitcherByTeamIdAndYear(Long TeamId, String position, String year) {
		return vBaseballPlayerHistoryRepository.findPitcherByTeamIdAndYear(TeamId, position, year);
	}

	/**
	 * @param teamId
	 * @param playerNm
	 * @param date
	 * @return
	 */
	public VBaseballPlayerHistory findByPlayerNmAndTeamId(Long teamId, String playerNm, Date date) {
		return vBaseballPlayerHistoryRepository.findByPlayerNmAndTeamIdAndDate(playerNm, teamId, date);
	}
}
