package com.example.pitchboxd.matchDetail.service;

import com.example.pitchboxd.match.core.infrastructure.dto.MatchDetailStaticModel;
import com.example.pitchboxd.match.core.service.domain.MatchQueryService;
import com.example.pitchboxd.match.lineup.infrastructure.dto.LineupPlayerModel;
import com.example.pitchboxd.match.lineup.service.MatchLineupQueryService;
import com.example.pitchboxd.match.matchReview.infrastructure.dto.HotReviewSummary;
import com.example.pitchboxd.match.matchReview.service.domain.MatchReviewLikeService;
import com.example.pitchboxd.match.matchReview.service.domain.MatchReviewQueryService;
import com.example.pitchboxd.matchDetail.dto.response.MatchDetailMatchReviewResponses;
import com.example.pitchboxd.matchDetail.dto.response.MatchDetailResultResponse;
import com.example.pitchboxd.matchDetail.dto.response.MatchDetailStatsResponse;
import com.example.pitchboxd.matchDetail.dto.response.LineupResponse;
import com.example.pitchboxd.matchDetail.dto.response.LineupResponses;
import com.example.pitchboxd.match.playerStatistics.domain.PlayerStatistics;
import com.example.pitchboxd.match.playerStatistics.infrastructure.PlayerStatisticsRepository;
import com.example.pitchboxd.match.matchStatistics.domain.MatchStatistics;
import com.example.pitchboxd.match.matchStatistics.infrastructure.MatchStatisticsRepository;
import com.example.pitchboxd.match.matchReview.infrastructure.MatchReviewRepository;
import com.example.pitchboxd.match.playerReview.infrastructure.PlayerReviewRepository;
import com.example.pitchboxd.matchDetail.dto.response.MatchDetailPersonalResponse;
import com.example.pitchboxd.matchDetail.dto.response.MyMatchReviewResponse;
import com.example.pitchboxd.matchDetail.dto.response.MyPlayerReviewResponse;
import com.example.pitchboxd.matchDetail.dto.response.MatchReviewDetailResponse;
import com.example.pitchboxd.matchDetail.dto.response.MatchReviewSliceResponse;
import com.example.pitchboxd.match.matchReview.domain.MatchReview;
import com.example.pitchboxd.match.matchReview.domain.ReviewSortType;
import com.example.pitchboxd.match.matchReview.infrastructure.MatchReviewQueryRepository;
import com.example.pitchboxd.user.domain.User;
import com.example.pitchboxd.user.infrastructure.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import java.util.function.Function;
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
    private final PlayerReviewRepository playerReviewRepository;
    private final MatchReviewQueryRepository matchReviewQueryRepository;
    private final UserRepository userRepository;

    public MatchDetailResultResponse getMatchResultData(Long matchId) {
        MatchDetailStaticModel matchDetail = matchQueryService.findMatchStaticDetailById(matchId);
        List<LineupPlayerModel> lineups = matchLineupQueryService.findLineupAndPlayedPlayers(matchId);

        List<PlayerStatistics> playerStats = playerStatisticsRepository.findAllByMatchId(matchId);
        Map<Long, Double> playerRatingsMap = playerStats.stream()
                .collect(Collectors.toMap(
                        PlayerStatistics::getPlayerId,
                        PlayerStatistics::getAverageRating,
                        (existing, replacement) -> existing
                ));

        List<LineupResponse> homeLineupResponses = lineups.stream()
                .filter(l -> l.teamId().equals(matchDetail.homeTeamId()))
                .map(l -> LineupResponse.of(l, playerRatingsMap.getOrDefault(l.playerId(), 0.0)))
                .toList();

        List<LineupResponse> awayLineupResponses = lineups.stream()
                .filter(l -> l.teamId().equals(matchDetail.awayTeamId()))
                .map(l -> LineupResponse.of(l, playerRatingsMap.getOrDefault(l.playerId(), 0.0)))
                .toList();

        return new MatchDetailResultResponse(
                matchDetail.seasonName(),
                matchDetail.round(),
                matchDetail.startTime(),
                matchDetail.location(),
                matchDetail.homeTeamName(),
                matchDetail.awayTeamName(),
                matchDetail.homeScore(),
                matchDetail.awayScore(),
                new LineupResponses(homeLineupResponses),
                new LineupResponses(awayLineupResponses)
        );
    }

    public MatchDetailStatsResponse getMatchStatsData(Long matchId) {
        List<LineupPlayerModel> lineups = matchLineupQueryService.findLineupAndPlayedPlayers(matchId);
        List<PlayerStatistics> playerStats = playerStatisticsRepository.findAllByMatchId(matchId);

        MatchStatistics matchStats = matchStatisticsRepository.findByMatchId(matchId)
                .orElse(new MatchStatistics(matchId));

        List<Object[]> rawDistribution = matchReviewRepository.countPointDistributionByMatchId(matchId);
        Map<Integer, Long> distributionMap = new HashMap<>();
        for (int i = 0; i <= 10; i++) {
            distributionMap.put(i, 0L);
        }
        for (Object[] row : rawDistribution) {
            Integer point = (Integer) row[0];
            Long count = (Long) row[1];
            if (point != null && count != null && point >= 0 && point <= 10) {
                distributionMap.put(point, count);
            }
        }

        List<PlayerStatistics> sortedStats = playerStats.stream()
                .filter(ps -> ps.getReviewCount() > 0)
                .sorted(Comparator.comparingDouble(PlayerStatistics::getAverageRating).reversed()
                        .thenComparing(PlayerStatistics::getReviewCount, Comparator.reverseOrder())
                        .thenComparing(PlayerStatistics::getPlayerId, Comparator.reverseOrder()))
                .toList();

        Map<Long, String> playerNamesMap = lineups.stream()
                .collect(Collectors.toMap(
                        LineupPlayerModel::playerId,
                        LineupPlayerModel::playerName,
                        (existing, replacement) -> existing
                ));

        MatchDetailStatsResponse.HighlightPlayerResponse mom = null;
        if (!sortedStats.isEmpty()) {
            PlayerStatistics momStat = sortedStats.get(0);
            String momName = playerNamesMap.getOrDefault(momStat.getPlayerId(), "Unknown Player");
            mom = new MatchDetailStatsResponse.HighlightPlayerResponse(momStat.getPlayerId(), momName, momStat.getAverageRating());
        }

        List<MatchDetailStatsResponse.HighlightPlayerResponse> top3 = sortedStats.stream()
                .limit(3)
                .map(ps -> new MatchDetailStatsResponse.HighlightPlayerResponse(
                        ps.getPlayerId(),
                        playerNamesMap.getOrDefault(ps.getPlayerId(), "Unknown Player"),
                        ps.getAverageRating()
                ))
                .toList();

        return new MatchDetailStatsResponse(
                matchStats.getTotalAverage(),
                matchStats.getHomeAverage(),
                matchStats.getAwayAverage(),
                distributionMap,
                new MatchDetailStatsResponse.MatchHighlightsResponse(mom, top3)
        );
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

    public MatchDetailPersonalResponse getMatchPersonalData(Long matchId, Long userId) {
        if (userId == null) {
            return new MatchDetailPersonalResponse(false, null, List.of());
        }

        MyMatchReviewResponse myMatchReview = matchReviewRepository.findByMatchIdAndUserId(matchId, userId)
                .map(matchReview -> new MyMatchReviewResponse(
                        matchReview.getId(),
                        matchReview.getPoint(),
                        matchReview.getContent()
                ))
                .orElse(null);

        List<MyPlayerReviewResponse> myPlayerReviews = playerReviewRepository.findAllByMatchIdAndUserId(matchId, userId)
                .stream()
                .map(playerReview -> new MyPlayerReviewResponse(
                        playerReview.getId(),
                        playerReview.getPlayerId(),
                        playerReview.getPoint(),
                        playerReview.getContent()
                ))
                .toList();

        boolean isEvaluated = (myMatchReview != null || !myPlayerReviews.isEmpty());
        return new MatchDetailPersonalResponse(isEvaluated, myMatchReview, myPlayerReviews);
    }

    public MatchReviewSliceResponse getMatchReviews(Long matchId, Long cursorId, Long cursorLikeCount, ReviewSortType sort, int size, Long userId) {
        if (size <= 0) {
            throw new IllegalArgumentException("페이지 크기는 1 이상이어야 합니다.");
        }
        if (ReviewSortType.LIKE == sort) {
            if ((cursorId == null && cursorLikeCount != null) || (cursorId != null && cursorLikeCount == null)) {
                throw new IllegalArgumentException("추천순 정렬 페이징 시 cursorId와 cursorLikeCount는 모두 null이거나 모두 null이 아니어야 합니다.");
            }
        }

        List<MatchReview> reviews = matchReviewQueryRepository.findReviewsByCursor(matchId, cursorId, cursorLikeCount, sort, size);

        boolean hasNext = reviews.size() > size;
        List<MatchReview> content = hasNext ? reviews.subList(0, size) : reviews;

        List<Long> authorIds = content.stream().map(MatchReview::getUserId).distinct().toList();
        List<User> authors = userRepository.findAllById(authorIds);
        Map<Long, User> authorMap = authors.stream().collect(Collectors.toMap(User::getId, Function.identity(), (existing, replacement) -> existing));

        List<Long> reviewIds = content.stream().map(MatchReview::getId).toList();
        Map<Long, Boolean> likedStatus = matchReviewLikeService.checkLikedStatusForReviews(reviewIds, userId);

        List<MatchReviewDetailResponse> reviewResponses = content.stream()
                .map(r -> {
                    User author = authorMap.get(r.getUserId());
                    String nickname = author != null ? author.getNickname() : "Unknown";
                    String profile = "";
                    boolean isLiked = likedStatus.getOrDefault(r.getId(), false);
                    boolean isOwner = userId != null && r.isOwner(userId);
                    return new MatchReviewDetailResponse(
                            r.getId(),
                            r.getUserId(),
                            nickname,
                            profile,
                            r.getFanType(),
                            r.getPoint(),
                            r.getContent(),
                            r.getLikeCount(),
                            isLiked,
                            isOwner,
                            r.getCreatedAt()
                    );
                })
                .toList();

        Long nextCursorId = null;
        Long nextCursorLikeCount = null;
        if (hasNext && !reviewResponses.isEmpty()) {
            MatchReviewDetailResponse last = reviewResponses.get(reviewResponses.size() - 1);
            nextCursorId = last.reviewId();
            nextCursorLikeCount = last.likeCount();
        }

        return new MatchReviewSliceResponse(reviewResponses, nextCursorId, nextCursorLikeCount, hasNext);
    }
}
