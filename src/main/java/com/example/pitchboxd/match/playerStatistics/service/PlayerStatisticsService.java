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
    public void createAllPlayerStatistics(Long matchId, java.util.List<Long> playerIds) {
        java.util.List<PlayerStatistics> existingStats = playerStatisticsRepository.findAllByMatchId(matchId);
        java.util.Set<Long> existingPlayerIds = existingStats.stream()
                .map(PlayerStatistics::getPlayerId)
                .collect(java.util.stream.Collectors.toSet());

        java.util.List<PlayerStatistics> newStats = playerIds.stream()
                .filter(playerId -> !existingPlayerIds.contains(playerId))
                .map(playerId -> new PlayerStatistics(playerId, matchId))
                .toList();

        playerStatisticsRepository.saveAll(newStats);
    }


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
