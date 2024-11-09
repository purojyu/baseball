package com.example.baseball.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.baseball.entity.BaseballPlayerHistory;
import com.example.baseball.repository.BaseballPlayerHistoryRepository;

/**
 * BaseballPlayerHistory のサービスクラス
 */
@Service
public class BaseballPlayerHistoryService {

    @Autowired
    private BaseballPlayerHistoryRepository baseballPlayerHistoryRepository;

    /**
     * プレイヤーIDとチームIDで選手経歴を検索します。
     *
     * @param playerId 選手のID
     * @param teamId   チームのID
     * @return 該当する BaseballPlayerHistory エンティティ
     */
    public BaseballPlayerHistory findByPlayerIdAndteamId(Long playerId, Long teamId) {
        return baseballPlayerHistoryRepository.findByplayerIdAndteamId(playerId, teamId);
    }

    /**
     * プレイヤーIDで選手経歴を検索します。
     *
     * @param playerId 選手のID
     * @return 該当する BaseballPlayerHistory エンティティ
     */
    public BaseballPlayerHistory findByPlayerId(Long playerId) {
        return baseballPlayerHistoryRepository.findByplayerId(playerId);
    }
    
    /**
     * プレイヤーID、チームID、およびEND_DATEの年で選手経歴を検索します。
     *
     * @param playerId 選手のID
     * @param teamId   チームのID
     * @param year     END_DATEの年（例: "2016"）
     * @return 該当する BaseballPlayerHistory エンティティ
     */
    public BaseballPlayerHistory findByPlayerIdAndTeamIdAndEndDateYear(Long playerId, Long teamId, String year) {
        return baseballPlayerHistoryRepository.findByPlayerIdAndTeamIdAndEndDateYear(playerId, teamId, year);
    }

    /**
     * プレイヤーID、チームID、およびSTART_DATEの年で選手経歴を検索します。
     *
     * @param playerId 選手のID
     * @param teamId   チームのID
     * @param year     START_DATEの年（例: "2016"）
     * @return 該当する BaseballPlayerHistory エンティティ
     */
    public BaseballPlayerHistory findByPlayerIdAndTeamIdAndStartDateYear(Long playerId, Long teamId, String year) {
        return baseballPlayerHistoryRepository.findByPlayerIdAndTeamIdAndStartDateYear(playerId, teamId, year);
    }

    /**
     * 選手経歴を保存または更新します。
     *
     * @param baseballPlayerHistory 保存または更新する BaseballPlayerHistory エンティティ
     * @return 保存または更新された BaseballPlayerHistory エンティティ
     */
    public BaseballPlayerHistory saveBaseballPlayerHistory(BaseballPlayerHistory baseballPlayerHistory) {
        return baseballPlayerHistoryRepository.save(baseballPlayerHistory);
    }
}
