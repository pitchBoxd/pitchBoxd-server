package com.example.pitchboxd.match.playerStatistics.service;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.match.playerStatistics.domain.PlayerStatistics;
import com.example.pitchboxd.match.playerStatistics.infrastructure.PlayerStatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlayerStatisticsService {

    private final PlayerStatisticsRepository playerStatisticsRepository;

    @Transactional
    public void updateReview(Long matchId, Long playerId, int point) {
        PlayerStatistics playerStatistics = playerStatisticsRepository
                .findByMatchIdAndPlayerIdForUpdate(matchId, playerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAYER_STATISTICS_NOT_FOUND));

        playerStatistics.addNewReview(point);
    }

    @Transactional
    public void adjustReviewStatistics(Long matchId, Long playerId, int differenceOfPoint) {
        PlayerStatistics playerStatistics = playerStatisticsRepository
                .findByMatchIdAndPlayerIdForUpdate(matchId, playerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAYER_STATISTICS_NOT_FOUND));

        playerStatistics.adjustRating(differenceOfPoint);
    }

    @Transactional
    public void removeReview(Long matchId, Long playerId, int point) {
        PlayerStatistics playerStatistics = playerStatisticsRepository
                .findByMatchIdAndPlayerIdForUpdate(matchId, playerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAYER_STATISTICS_NOT_FOUND));

        playerStatistics.removeReview(point);
    }
}
