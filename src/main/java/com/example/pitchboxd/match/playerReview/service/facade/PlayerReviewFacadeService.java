package com.example.pitchboxd.match.playerReview.service.facade;

import com.example.pitchboxd.global.domain.ClockHolder;
import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.service.domain.MatchService;
import com.example.pitchboxd.match.lineup.domain.MatchLineup;
import com.example.pitchboxd.match.lineup.service.MatchLineupService;
import com.example.pitchboxd.match.matchReview.dto.response.LikeToggleResponse;
import com.example.pitchboxd.match.playerReview.domain.PlayerReview;
import com.example.pitchboxd.match.playerReview.domain.PlayerReviewSubmitPolicy;
import com.example.pitchboxd.match.playerReview.dto.request.PlayerReviewCreateRequest;
import com.example.pitchboxd.match.playerReview.dto.request.PlayerReviewUpdateRequest;
import com.example.pitchboxd.match.playerReview.dto.response.PlayerReviewCreateResponse;
import com.example.pitchboxd.match.playerReview.dto.response.PlayerReviewUpdateResponse;
import com.example.pitchboxd.match.playerReview.service.domain.PlayerReviewLikeService;
import com.example.pitchboxd.match.playerReview.service.domain.PlayerReviewService;
import com.example.pitchboxd.match.playerStatistics.service.PlayerStatisticsService;
import com.example.pitchboxd.player.domain.Player;
import com.example.pitchboxd.player.service.PlayerService;
import com.example.pitchboxd.user.application.UserService;
import com.example.pitchboxd.user.domain.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.function.Function;
import com.example.pitchboxd.matchDetail.dto.response.PlayerReviewSliceResponse;
import com.example.pitchboxd.matchDetail.dto.response.PlayerReviewDetailResponse;
import com.example.pitchboxd.match.matchReview.domain.ReviewSortType;
import com.example.pitchboxd.match.playerReview.infrastructure.PlayerReviewQueryRepository;
import com.example.pitchboxd.match.playerReview.infrastructure.PlayerReviewLikeRepository;
import com.example.pitchboxd.user.infrastructure.UserRepository;
import com.example.pitchboxd.team.infrastructure.TeamRepository;
import com.example.pitchboxd.team.domain.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlayerReviewFacadeService {

    private final MatchService matchService;
    private final UserService userService;
    private final PlayerService playerService;
    private final PlayerReviewService playerReviewService;
    private final MatchLineupService matchLineupService;
    private final PlayerStatisticsService playerStatisticsService;
    private final PlayerReviewLikeService playerReviewLikeService;
    private final PlayerReviewSubmitPolicy playerReviewSubmitPolicy;
    private final PlayerReviewQueryRepository playerReviewQueryRepository;
    private final PlayerReviewLikeRepository playerReviewLikeRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    private final ClockHolder clockHolder;

    /***
     * 선수 리뷰 가능 여부는 다음과 조건을 따릅니다.
     * 1. 경기가 종료된 후, 경기가 종료된지 24시간 이내여야 합니다.
     * 2. 유저가 해당 경기에서 해당 선수에게 리뷰을 달지 않았어야 합니다.
     * 3. 선수가 경기에 출전(선발 or 교체출전) 해야합니다.
     * 4. 선수의 팀과 유저의 응원 팀이 같아야 합니다.
     * ***/
    @Transactional
    public PlayerReviewCreateResponse submitReview(PlayerReviewCreateRequest request, Long matchId, Long userId) {
        Long playerId = request.playerId();
        Match match = matchService.findById(matchId);
        LocalDateTime now = clockHolder.now();
        playerReviewSubmitPolicy.validateMatchStatus(match, now);

        MatchLineup matchLineup = matchLineupService.findMatchLineup(matchId, playerId);
        playerReviewSubmitPolicy.validatePlayerParticipation(matchLineup);

        User user = userService.findById(userId);
        Player player = playerService.findPlayer(request.playerId());
        boolean isAlreadyReviewed = playerReviewService.hasAlreadyReviewed(matchId, playerId, userId);
        playerReviewSubmitPolicy.validateUserCondition(user, player, isAlreadyReviewed);

        PlayerReview savedPlayerReview = playerReviewService.save(request, matchId, userId);

        //TODO: 일단 동기적으로 만들어두고, 나중에 이벤트 리스너로 분리 ㄱㄱ
        // 나중엔 트랜잭셔널 아웃박스 패턴으로 정합성 보장해보는것도 좋을듯. 이벤트 리스너의 유실 문제 해결을 위해서
        playerStatisticsService.updateReview(matchId, playerId, request.point());

        return new PlayerReviewCreateResponse(savedPlayerReview.getId());
    }

    @Transactional
    public LikeToggleResponse toggleLike(Long playerReviewId, Long userId) {
        PlayerReview playerReview = playerReviewService.findByIdForUpdate(playerReviewId);
        userService.findById(userId);

        boolean isLikedNow = playerReviewLikeService.isLiked(playerReviewId, userId);
        boolean willBeLiked = !isLikedNow;

        if (isLikedNow) {
            playerReviewLikeService.delete(playerReviewId, userId);
            playerReview.minusOneLikeCount();
        }

        if (!isLikedNow) {
            playerReviewLikeService.save(playerReviewId, userId);
            playerReview.addOneLikeCount();
        }

        return LikeToggleResponse.of(willBeLiked, playerReview);
    }

    @Transactional
    public PlayerReviewUpdateResponse updateReview(PlayerReviewUpdateRequest request, Long playerReviewId,
                                                   Long userId) {
        PlayerReview playerReview = playerReviewService.findById(playerReviewId);
        userService.findById(userId);
        
        if (!playerReview.isOwner(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        int beforePoint = playerReview.getPoint();

        playerReview.update(request.content(), request.point());

        int afterPoint = request.point();
        int differenceOfPoint = afterPoint - beforePoint;

        // TODO: 일단 동기적으로 만들어두고, 나중에 이벤트 리스너로 분리 ㄱㄱ
        // 나중엔 트랜잭셔널 아웃박스 패턴으로 정합성 보장해보는것도 좋을듯. 이벤트 리스너의 유실 문제 해결을 위해서
        if (differenceOfPoint != 0) {
            playerStatisticsService.adjustReviewStatistics(playerReview.getMatchId(), playerReview.getPlayerId(),
                    differenceOfPoint);
        }

        return new PlayerReviewUpdateResponse(playerReview.getId());
    }

    @Transactional
    public void deleteReview(Long playerReviewId, Long userId) {
        PlayerReview playerReview = playerReviewService.findById(playerReviewId);
        userService.findById(userId);

        if (!playerReview.isOwner(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        playerReviewService.deleteById(playerReviewId);

        // TODO: 다른 트랜잭션으로 분리 필요. 나중에 이벤트 리스너로 분리 ㄱㄱ
        playerStatisticsService.removeReview(playerReview.getMatchId(), playerReview.getPlayerId(),
                playerReview.getPoint());
    }

    public PlayerReviewSliceResponse getPlayerReviews(Long matchId, Long playerId, Long cursorId, Long cursorLikeCount, ReviewSortType sort, int size, Long loginUserId) {
        if (size <= 0) {
            throw new IllegalArgumentException("페이지 크기는 1 이상이어야 합니다.");
        }
        if (ReviewSortType.LIKE == sort) {
            if ((cursorId == null && cursorLikeCount != null) || (cursorId != null && cursorLikeCount == null)) {
                throw new IllegalArgumentException("추천순 정렬 페이징 시 cursorId와 cursorLikeCount는 모두 null이거나 모두 null이 아니어야 합니다.");
            }
        }

        List<PlayerReview> reviews = playerReviewQueryRepository.findReviewsByCursor(matchId, playerId, cursorId, cursorLikeCount, sort, size);

        boolean hasNext = reviews.size() > size;
        List<PlayerReview> content = hasNext ? reviews.subList(0, size) : reviews;

        List<Long> authorIds = content.stream()
                .map(PlayerReview::getUserId)
                .distinct()
                .toList();

        Map<Long, User> authorMap = userRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (existing, replacement) -> existing));

        List<Long> teamIds = authorMap.values().stream()
                .map(User::getFavoriteTeamId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, String> teamMap = teamIds.isEmpty() ? Map.of() : teamRepository.findAllById(teamIds).stream()
                .collect(Collectors.toMap(Team::getId, Team::getName, (existing, replacement) -> existing));

        List<Long> reviewIds = content.stream()
                .map(PlayerReview::getId)
                .toList();

        java.util.Set<Long> likedReviewIds = (loginUserId != null && !reviewIds.isEmpty())
                ? new java.util.HashSet<>(playerReviewLikeRepository.findLikedReviewIdsIn(reviewIds, loginUserId))
                : java.util.Collections.emptySet();

        List<PlayerReviewDetailResponse> reviewResponses = content.stream()
                .map(r -> {
                    User author = authorMap.get(r.getUserId());
                    String nickname = author != null ? author.getNickname() : "Unknown";

                    String favoriteTeamName = null;
                    if (author != null && author.getFavoriteTeamId() != null) {
                        favoriteTeamName = teamMap.get(author.getFavoriteTeamId());
                    }

                    boolean isLiked = likedReviewIds.contains(r.getId());
                    String fanTypeStr = r.getFanType() != null ? r.getFanType().name() : null;

                    return new PlayerReviewDetailResponse(
                            r.getId(),
                            nickname,
                            favoriteTeamName,
                            r.getPoint(),
                            r.getContent(),
                            fanTypeStr,
                            r.getLikeCount(),
                            isLiked,
                            r.getCreatedAt()
                    );
                })
                .toList();

        Long nextCursorId = null;
        Long nextCursorLikeCount = null;
        if (hasNext && !reviewResponses.isEmpty()) {
            PlayerReviewDetailResponse last = reviewResponses.get(reviewResponses.size() - 1);
            nextCursorId = last.id();
            nextCursorLikeCount = last.likeCount();
        }

        return new PlayerReviewSliceResponse(reviewResponses, nextCursorId, nextCursorLikeCount, hasNext);
    }
}
