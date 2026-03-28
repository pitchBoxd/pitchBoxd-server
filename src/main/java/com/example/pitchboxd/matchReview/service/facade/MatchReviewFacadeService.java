package com.example.pitchboxd.matchReview.service.facade;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.match.domain.Match;
import com.example.pitchboxd.match.dto.request.MatchReviewCreateRequest;
import com.example.pitchboxd.match.dto.response.MatchReviewCreateResponse;
import com.example.pitchboxd.match.service.domain.MatchService;
import com.example.pitchboxd.matchReview.domain.MatchReview;
import com.example.pitchboxd.matchReview.service.domain.MatchReviewService;
import com.example.pitchboxd.matchStatistics.domain.FanType;
import com.example.pitchboxd.matchStatistics.service.domain.MatchStatisticsService;
import com.example.pitchboxd.user.application.UserService;
import com.example.pitchboxd.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchReviewFacadeService {

    private final MatchReviewService matchReviewService;
    private final MatchService matchService;
    private final UserService userService;
    private final MatchStatisticsService matchStatisticsService;

    @Transactional
    public MatchReviewCreateResponse submitReview(MatchReviewCreateRequest request, Long matchId, Long userId) {
        Match match = matchService.findMatch(matchId);
        // TODO: 나중에 매치에 리뷰를 달 수 있는 시간인지 확인한다(리뷰는 24시간 이내로 작성 가능하게 한다.)

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
