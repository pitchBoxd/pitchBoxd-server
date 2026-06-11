package com.example.pitchboxd.matchDetail.service;

import com.example.pitchboxd.match.core.infrastructure.dto.MatchDetailStaticModel;
import com.example.pitchboxd.match.core.service.domain.MatchQueryService;
import com.example.pitchboxd.match.lineup.infrastructure.dto.LineupPlayerModel;
import com.example.pitchboxd.match.lineup.service.MatchLineupQueryService;
import com.example.pitchboxd.match.matchReview.infrastructure.dto.HotReviewSummary;
import com.example.pitchboxd.match.matchReview.service.domain.MatchReviewLikeService;
import com.example.pitchboxd.match.matchReview.service.domain.MatchReviewQueryService;
import com.example.pitchboxd.matchDetail.dto.response.MatchDetailMatchReviewResponses;
import com.example.pitchboxd.matchDetail.dto.response.MatchDetailResponse;
import com.example.pitchboxd.matchDetail.dto.response.LineupResponse;
import com.example.pitchboxd.matchDetail.dto.response.LineupResponses;
import com.example.pitchboxd.match.playerStatistics.domain.PlayerStatistics;
import com.example.pitchboxd.match.playerStatistics.infrastructure.PlayerStatisticsRepository;
import com.example.pitchboxd.match.matchStatistics.domain.MatchStatistics;
import com.example.pitchboxd.match.matchStatistics.infrastructure.MatchStatisticsRepository;
import com.example.pitchboxd.match.matchReview.infrastructure.MatchReviewRepository;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MatchDetailFacadeService {

    private final MatchQueryService matchQueryService;
    private final MatchLineupQueryService matchLineupQueryService;
    private final MatchReviewQueryService matchReviewQueryService;
    private final MatchReviewLikeService matchReviewLikeService;
    private final PlayerStatisticsRepository playerStatisticsRepository;
    private final MatchStatisticsRepository matchStatisticsRepository;
    private final MatchReviewRepository matchReviewRepository;

    public MatchDetailResponse getMatchStaticData(Long matchId) {
        MatchDetailStaticModel matchDetail = matchQueryService.findMatchStaticDetailById(matchId);
        List<LineupPlayerModel> lineups = matchLineupQueryService.findLineupAndPlayedPlayers(matchId);

        List<PlayerStatistics> playerStats = playerStatisticsRepository.findAllByMatchId(matchId);
        Map<Long, Double> playerRatingsMap = playerStats.stream()
                .collect(Collectors.toMap(PlayerStatistics::getPlayerId, PlayerStatistics::getAverageRating));

        List<LineupResponse> homeLineupResponses = lineups.stream()
                .filter(l -> l.teamId().equals(matchDetail.homeTeamId()))
                .map(l -> LineupResponse.of(l, playerRatingsMap.getOrDefault(l.playerId(), 0.0)))
                .toList();

        List<LineupResponse> awayLineupResponses = lineups.stream()
                .filter(l -> l.teamId().equals(matchDetail.awayTeamId()))
                .map(l -> LineupResponse.of(l, playerRatingsMap.getOrDefault(l.playerId(), 0.0)))
                .toList();

        MatchStatistics matchStats = matchStatisticsRepository.findByMatchId(matchId)
                .orElse(new MatchStatistics(matchId));

        long totalSum = matchStats.getTotalRatingSum();
        long homeSum = matchStats.getHomeFanRatingSum();
        long awaySum = matchStats.getAwayFanRatingSum();
        int totalCount = matchStats.getTotalReviewCount();
        int homeCount = matchStats.getHomeFanReviewCount();
        int awayCount = matchStats.getAwayFanReviewCount();

        int neutralCount = totalCount - homeCount - awayCount;
        long neutralSum = totalSum - homeSum - awaySum;
        double neutralAverage = neutralCount <= 0 ? 0.0 : (neutralSum / (double) neutralCount) / 2.0;

        List<Object[]> rawDistribution = matchReviewRepository.countPointDistributionByMatchId(matchId);
        Map<Integer, Long> distributionMap = new HashMap<>();
        for (int i = 1; i <= 10; i++) {
            distributionMap.put(i, 0L);
        }
        for (Object[] row : rawDistribution) {
            Integer point = (Integer) row[0];
            Long count = (Long) row[1];
            if (point >= 1 && point <= 10) {
                distributionMap.put(point, count);
            }
        }

        List<PlayerStatistics> sortedStats = playerStats.stream()
                .filter(ps -> ps.getReviewCount() > 0)
                .sorted(Comparator.comparingDouble(PlayerStatistics::getAverageRating).reversed()
                        .thenComparing(PlayerStatistics::getReviewCount, Comparator.reverseOrder())
                        .thenComparing(PlayerStatistics::getPlayerId, Comparator.reverseOrder()))
                .toList();

        MatchDetailResponse.HighlightPlayerResponse mom = null;
        if (!sortedStats.isEmpty()) {
            PlayerStatistics momStat = sortedStats.get(0);
            String momName = findPlayerName(lineups, momStat.getPlayerId());
            mom = new MatchDetailResponse.HighlightPlayerResponse(momStat.getPlayerId(), momName, momStat.getAverageRating());
        }

        List<MatchDetailResponse.HighlightPlayerResponse> top3 = sortedStats.stream()
                .limit(3)
                .map(ps -> new MatchDetailResponse.HighlightPlayerResponse(
                        ps.getPlayerId(),
                        findPlayerName(lineups, ps.getPlayerId()),
                        ps.getAverageRating()
                ))
                .toList();

        return new MatchDetailResponse(
                matchDetail.seasonName(),
                matchDetail.round(),
                matchDetail.startTime(),
                matchDetail.location(),
                matchDetail.homeTeamName(),
                matchDetail.awayTeamName(),
                matchDetail.homeScore(),
                matchDetail.awayScore(),
                new LineupResponses(homeLineupResponses),
                new LineupResponses(awayLineupResponses),
                matchStats.getTotalAverage(),
                matchStats.getHomeAverage(),
                matchStats.getAwayAverage(),
                neutralAverage,
                distributionMap,
                new MatchDetailResponse.MatchHighlightsResponse(mom, top3)
        );
    }

    private String findPlayerName(List<LineupPlayerModel> lineups, Long playerId) {
        return lineups.stream()
                .filter(l -> l.playerId().equals(playerId))
                .map(LineupPlayerModel::playerName)
                .findFirst()
                .orElse("Unknown Player");
    }

    public MatchDetailMatchReviewResponses getMatchHotReviews(Long matchId, Long userId, int limit) {
        List<HotReviewSummary> topHotReviewsByMatchId = matchReviewQueryService.getTopHotReviewsByMatchId(matchId,
                limit);

        List<Long> matchReviewIds = topHotReviewsByMatchId.stream()
                .map(HotReviewSummary::reviewId)
                .toList();

        Map<Long, Boolean> likedStatusForReviews = matchReviewLikeService.checkLikedStatusForReviews(matchReviewIds,
                userId);

        return MatchDetailMatchReviewResponses.of(topHotReviewsByMatchId, likedStatusForReviews);
    }
}
