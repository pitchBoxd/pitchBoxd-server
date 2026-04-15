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

    /***
     * 경기 리뷰 가능 정책은 다음과 같이 검증합니다.
     * 1. 경기가 종료되어야 한다.
     * 2. 제한 시간 (REVIEW_SUBMIT_LIMIT) 내로 리뷰하여야 한다.
     * 3. 리뷰는 한 사람당 한 번만 가능하다.
     * ***/
    public void validate(Match match, boolean isAlreadyReviewed, LocalDateTime now) {
        if (!match.isEnd(now)) {
            throw new BusinessException(ErrorCode.MATCH_NOT_ENDED);
        }

        if (match.isPassed(now, REVIEW_SUBMIT_LIMIT)) {
            throw new BusinessException(ErrorCode.MATCH_REVIEW_TIME_LIMIT_PASSED);
        }

        if (isAlreadyReviewed) {
            throw new BusinessException(ErrorCode.MATCH_REVIEW_ALREADY_REVIEWED);
        }
    }
}
