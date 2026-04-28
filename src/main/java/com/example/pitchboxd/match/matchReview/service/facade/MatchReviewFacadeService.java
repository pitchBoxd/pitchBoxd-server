package com.example.pitchboxd.match.matchReview.service.facade;

import com.example.pitchboxd.global.domain.ClockHolder;
import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.infrastructure.dto.MatchSummary;
import com.example.pitchboxd.match.core.service.domain.MatchQueryService;
import com.example.pitchboxd.match.core.service.domain.MatchService;
import com.example.pitchboxd.match.matchReview.domain.MatchReview;
import com.example.pitchboxd.match.matchReview.domain.MatchReviewSubmitPolicy;
import com.example.pitchboxd.match.matchReview.dto.request.MatchReviewCreateRequest;
import com.example.pitchboxd.match.matchReview.dto.request.MatchReviewUpdateRequest;
import com.example.pitchboxd.match.matchReview.dto.response.HotReviewResponses;
import com.example.pitchboxd.match.matchReview.dto.response.LikeToggleResponse;
import com.example.pitchboxd.match.matchReview.dto.response.MatchReviewCreateResponse;
import com.example.pitchboxd.match.matchReview.dto.response.MatchReviewUpdateResponse;
import com.example.pitchboxd.match.matchReview.infrastructure.dto.HotReviewSummary;
import com.example.pitchboxd.match.matchReview.service.domain.MatchReviewLikeService;
import com.example.pitchboxd.match.matchReview.service.domain.MatchReviewQueryService;
import com.example.pitchboxd.match.matchReview.service.domain.MatchReviewService;
import com.example.pitchboxd.match.matchStatistics.domain.FanType;
import com.example.pitchboxd.match.matchStatistics.service.domain.MatchStatisticsService;
import com.example.pitchboxd.user.application.UserService;
import com.example.pitchboxd.user.domain.User;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchReviewFacadeService {

    private final MatchReviewService matchReviewService;
    private final MatchReviewQueryService matchReviewQueryService;
    private final MatchService matchService;
    private final MatchQueryService matchQueryService;
    private final UserService userService;
    private final MatchStatisticsService matchStatisticsService;
    private final MatchReviewLikeService matchReviewLikeService;
    private final MatchReviewSubmitPolicy matchReviewSubmitPolicy;
    private final ClockHolder clockHolder;

    /***
     * 경기 리뷰 가능 정책은 다음과 같이 검증합니다.
     * 1. 경기가 종료되어야 한다.
     * 2. 제한 시간 (REVIEW_SUBMIT_LIMIT) 내로 리뷰하여야 한다.
     * 3. 리뷰는 한 사람당 한 번만 가능하다.
     * ***/
    @Transactional
    public MatchReviewCreateResponse submitReview(MatchReviewCreateRequest request, Long matchId, Long userId) {
        Match match = matchService.findById(matchId);
        LocalDateTime now = clockHolder.now();
        matchReviewSubmitPolicy.validateMatchStatus(match, now);

        boolean alreadyReviewed = matchReviewService.isExist(matchId, userId);
        matchReviewSubmitPolicy.validateUserCondition(alreadyReviewed);

        User user = userService.findById(userId);
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

    //TODO: 현재는 비관락 사용으로 lost update를 방지하지만, 추후에 바꿔야함!
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

    @Transactional
    public void deleteMatchReview(Long matchReviewId, Long userId) {
        MatchReview matchReview = matchReviewService.findById(matchReviewId);
        if (!matchReview.isOwner(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        matchReviewService.delete(matchReviewId);

        // TODO: 다른 트랜잭션으로 분리 필요. 나중에 이벤트 리스너로 분리 ㄱㄱ
        matchStatisticsService.removeReview(matchReview.getMatchId(), matchReview.getPoint(), matchReview.getFanType());
    }

    public HotReviewResponses getHotReviews(int limit) {
        LocalDateTime threshold = matchReviewQueryService.getReviewableThreshold();

        List<MatchSummary> matchSummaries = matchQueryService.findRecentlyFinishedMatches(threshold);
        List<Long> reviewableMatchIds = matchSummaries.stream()
                .map(MatchSummary::id)
                .toList();

        if (reviewableMatchIds.isEmpty()) {
            return new HotReviewResponses(Collections.emptyList());
        }

        List<HotReviewSummary> hotReviews = matchReviewQueryService.getTopHotReviews(reviewableMatchIds, limit);

        return HotReviewResponses.of(hotReviews);
    }
}
