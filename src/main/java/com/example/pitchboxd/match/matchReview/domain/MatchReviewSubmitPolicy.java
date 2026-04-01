package com.example.pitchboxd.match.matchReview.domain;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.match.core.domain.Match;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class MatchReviewSubmitPolicy {

    private static final Duration REVIEW_SUBMIT_LIMIT = Duration.ofHours(24);

    public void validate(Match match, boolean isAlreadyReviewed, LocalDateTime now) {
        // 1. 경기 종료 여부
        if (!match.isEnd(now)) {
            throw new BusinessException(ErrorCode.MATCH_NOT_ENDED);
        }

        // 2. 시간 제한 로직을 Match에서 가져와 Policy가 직접 통제
        if (match.isPassed(now, REVIEW_SUBMIT_LIMIT)) {
            throw new BusinessException(ErrorCode.MATCH_REVIEW_TIME_LIMIT_PASSED);
        }

        // 3. 중복 리뷰 검증
        if (isAlreadyReviewed) {
            throw new BusinessException(ErrorCode.MATCH_REVIEW_ALREADY_REVIEWED);
        }
    }
}
