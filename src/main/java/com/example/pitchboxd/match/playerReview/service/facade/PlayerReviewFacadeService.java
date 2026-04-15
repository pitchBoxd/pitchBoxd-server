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

    private final ClockHolder clockHolder;

    @Transactional
    public PlayerReviewCreateResponse submitReview(PlayerReviewCreateRequest request, Long matchId, Long userId) {
        Long playerId = request.playerId();
        Match match = matchService.findById(matchId);
        User user = userService.findById(userId);
        Player player = playerService.findPlayer(request.playerId());
        MatchLineup matchLineup = matchLineupService.findMatchLineup(matchId, playerId); // 라인업에 없는 경우는 여기서 걸러짐
        boolean isAlreadyReviewed = playerReviewService.hasAlreadyReviewed(matchId, playerId, userId);
        LocalDateTime now = clockHolder.now();

        playerReviewSubmitPolicy.validate(match, matchLineup, user, player, isAlreadyReviewed, now);

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

        // TODO: 추후에 리뷰 수정 가능 시간 검증 도입
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
}
