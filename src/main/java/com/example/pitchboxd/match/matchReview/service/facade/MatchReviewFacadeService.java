package com.example.pitchboxd.match.matchReview.service.facade;

import com.example.pitchboxd.global.domain.ClockHolder;
import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.service.domain.MatchService;
import com.example.pitchboxd.match.matchReview.domain.MatchReview;
import com.example.pitchboxd.match.matchReview.dto.request.MatchReviewCreateRequest;
import com.example.pitchboxd.match.matchReview.dto.request.MatchReviewUpdateRequest;
import com.example.pitchboxd.match.matchReview.dto.response.LikeToggleResponse;
import com.example.pitchboxd.match.matchReview.dto.response.MatchReviewCreateResponse;
import com.example.pitchboxd.match.matchReview.dto.response.MatchReviewUpdateResponse;
import com.example.pitchboxd.match.matchReview.service.domain.MatchReviewLikeService;
import com.example.pitchboxd.match.matchReview.service.domain.MatchReviewService;
import com.example.pitchboxd.match.matchStatistics.domain.FanType;
import com.example.pitchboxd.match.matchStatistics.service.domain.MatchStatisticsService;
import com.example.pitchboxd.user.application.UserService;
import com.example.pitchboxd.user.domain.User;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchReviewFacadeService {

    private static final Duration REVIEW_LIMIT = Duration.ofHours(24);

    private final MatchReviewService matchReviewService;
    private final MatchService matchService;
    private final UserService userService;
    private final MatchStatisticsService matchStatisticsService;
    private final MatchReviewLikeService matchReviewLikeService;
    private final ClockHolder clockHolder;

    /***
     * 경기 리뷰 가능 여부는 다음과 조건을 따릅니다.
     * 1. 경기가 종료된 후, 경기가 종료된지 24시간 이내여야 합니다.
     * 2. 유저가 해당 경기에 리뷰를 달지 않았어야 합니다.
     * ***/
    @Transactional
    public MatchReviewCreateResponse submitReview(MatchReviewCreateRequest request, Long matchId, Long userId) {
        Match match = matchService.findById(matchId);
        LocalDateTime now = clockHolder.now();
        if (!match.isEnd(now) || match.isPassed(now, REVIEW_LIMIT)) {
            throw new BusinessException(ErrorCode.MATCH_REVIEW_TIME_LIMIT_PASSED);
        }

        User user = userService.findById(userId);

        if (matchReviewService.isExist(matchId, userId)) {
            throw new BusinessException(ErrorCode.MATCH_REVIEW_ALREADY_REVIEWED);
        }

        FanType fanType = match.determineFanType(user.getFavoriteTeamId());

        MatchReview savedMatchReview = matchReviewService.save(request, fanType, matchId, userId);

        // TODO: 일단 동기적으로 만들어두고, 나중에 이벤트 리스너로 분리 ㄱㄱ
        // 나중엔 트랜잭셔널 아웃박스 패턴으로 정합성 보장해보는것도 좋을듯. 이벤트 리스너의 유실 문제 해결을 위해서
        matchStatisticsService.updateReview(matchId, request.point(), fanType);

        return new MatchReviewCreateResponse(savedMatchReview.getId());
    }

    @Transactional
    public MatchReviewUpdateResponse updateMatchReview(Long matchReviewId, Long userId,
                                                       MatchReviewUpdateRequest request) {
        MatchReview matchReview = matchReviewService.findById(matchReviewId);
        if (!matchReview.isOwner(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        int beforePoint = matchReview.getPoint();

        // TODO: 수정에 대한 어떤 정책이 필요할지? (수정하면 Review 자체에 수정됨 낙인을 찍는게 나을듯)

        matchReview.update(request.content(), request.point());

        int afterPoint = request.point();
        int differenceOfPoint = afterPoint - beforePoint;

        // TODO: 일단 동기적으로 만들어두고, 나중에 이벤트 리스너로 분리 ㄱㄱ
        // 나중엔 트랜잭셔널 아웃박스 패턴으로 정합성 보장해보는것도 좋을듯. 이벤트 리스너의 유실 문제 해결을 위해서
        if (differenceOfPoint != 0) {
            matchStatisticsService.adjustReviewStatistics(matchReview.getMatchId(), differenceOfPoint,
                    matchReview.getFanType());
        }

        return new MatchReviewUpdateResponse(matchReview.getId());
    }

    @Transactional
    public LikeToggleResponse toggleLike(Long matchReviewId, Long userId) {
        MatchReview matchReview = matchReviewService.findByIdForUpdate(matchReviewId);
        userService.findById(userId);

        boolean isLikedNow = matchReviewLikeService.isLiked(matchReviewId, userId);
        boolean willBeLiked = !isLikedNow;

        if (isLikedNow) {
            matchReviewLikeService.delete(matchReviewId, userId);
            matchReview.minusOneLikeCount();
        }

        if (!isLikedNow) {
            matchReviewLikeService.save(matchReviewId, userId);
            matchReview.addOneLikeCount();
        }

        return LikeToggleResponse.of(willBeLiked, matchReview);
    }
}
