package com.example.pitchboxd.match.review.service.facade;

import com.example.pitchboxd.global.domain.ClockHolder;
import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.dto.request.MatchReviewCreateRequest;
import com.example.pitchboxd.match.core.dto.response.MatchReviewCreateResponse;
import com.example.pitchboxd.match.core.service.domain.MatchService;
import com.example.pitchboxd.match.review.domain.MatchReview;
import com.example.pitchboxd.match.review.service.domain.MatchReviewService;
import com.example.pitchboxd.match.statistics.domain.FanType;
import com.example.pitchboxd.match.statistics.service.domain.MatchStatisticsService;
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
    private final ClockHolder clockHolder;

    /***
     * 경기 리뷰 가능 여부는 다음과 조건을 따릅니다.
     * 1. 경기가 종료된 후, 경기가 종료된지 24시간 이내여야 합니다.
     * 2. 유저가 해당 경기에 리뷰를 달지 않았어야 합니다.
     * ***/
    @Transactional
    public MatchReviewCreateResponse submitReview(MatchReviewCreateRequest request, Long matchId, Long userId) {
        Match match = matchService.findMatch(matchId);
        LocalDateTime now = clockHolder.now();
        if (!match.isEnd(now) || match.isPassed(now, REVIEW_LIMIT)) {
            throw new BusinessException(ErrorCode.MATCH_REVIEW_TIME_LIMIT_PASSED);
        }

        User user = userService.findUser(userId);

        if (matchReviewService.isExist(matchId, userId)) {
            throw new BusinessException(ErrorCode.MATCH_REVIEW_ALREADY_REVIEWED);
        }

        FanType fanType = match.determineFanType(user.getFavoriteTeamId());

        MatchReview savedMatchReview = matchReviewService.save(request, matchId, userId);

        // 일단 동기적으로 만들어두고, 나중에 이벤트 리스너로 분리 ㄱㄱ
        // 나중엔 트랜잭셔널 아웃박스 패턴으로 정합성 보장해보는것도 좋을듯. 이벤트 리스너의 유실 문제 해결을 위해서
        matchStatisticsService.updateReview(matchId, request.point(), fanType);

        return new MatchReviewCreateResponse(savedMatchReview.getId());
    }
}
