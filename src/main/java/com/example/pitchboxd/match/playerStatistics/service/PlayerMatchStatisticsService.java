package com.example.pitchboxd.match.playerStatistics.service;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.match.playerStatistics.domain.PlayerMatchStatistics;
import com.example.pitchboxd.match.playerStatistics.infrastructure.PlayerMatchStatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlayerMatchStatisticsService {

    private final PlayerMatchStatisticsRepository playerMatchStatisticsRepository;

    public void updateReview(Long matchId, Long playerId, int point) {
        PlayerMatchStatistics playerMatchStatistics = playerMatchStatisticsRepository
                .findByMatchIdAndPlayerId(matchId, playerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAYER_REVIEW_NOT_FOUND));

        playerMatchStatistics.addReview(point);
    }
}
