package com.example.baseball.service;

import java.text.Collator;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
		return sortPlayerListByKana(vBaseballPlayerHistoryRepository.findBatterByTeamIdAndYear(TeamId, year));
	}

	/**
	 * ピッチャーのリストをチームIDと年度で検索
	 * @param TeamId
	 * @param position
	 * @param year
	 * @return
	 */
	public List<PlayerProjection> findPitcherByTeamIdAndYear(Long TeamId, String position, String year) {
		return sortPlayerListByKana(vBaseballPlayerHistoryRepository.findPitcherByTeamIdAndYear(TeamId, position, year));
	}

	/**
	 * 選手名とチームIDと日付で検索
	 * @param teamId
	 * @param playerNm
	 * @param date
	 * @return
	 */
	public VBaseballPlayerHistory findByPlayerNmAndTeamId(Long teamId, String playerNm, Date date) {
		return vBaseballPlayerHistoryRepository.findByPlayerNmAndTeamIdAndDate(playerNm, teamId, date);
	}
	
	/**
	 * 選手名のカナでソートする
	 * @param playerList
	 * @return 
	 */
	public List<PlayerProjection> sortPlayerListByKana(List<PlayerProjection> playerList) {
	    Collator collator = Collator.getInstance(Locale.JAPANESE);
	    collator.setStrength(Collator.PRIMARY);
	    Collections.sort(playerList, (p1, p2) -> collator.compare(p1.getPlayerNmKana(), p2.getPlayerNmKana()));
	    return playerList;
	}
}
