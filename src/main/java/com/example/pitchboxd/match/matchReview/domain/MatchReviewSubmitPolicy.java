package com.example.pitchboxd.match.matchReview.domain;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.match.core.domain.Match;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MatchReviewSubmitPolicy {

    private final Duration reviewSubmitLimit;

    public MatchReviewSubmitPolicy(
            @Value("${app.policy.match-review-limit:48h}") Duration reviewSubmitLimit
    ) {
        this.reviewSubmitLimit = reviewSubmitLimit;
    }

    public void validateMatchStatus(Match match, LocalDateTime now) {
        if (!match.isEnd(now) || match.isPassed(now, reviewSubmitLimit)) {
            throw new BusinessException(ErrorCode.MATCH_REVIEW_INVALID_REVIEW_TIME);
        }
    }

    public void validateUserCondition(boolean isAlreadyReviewed) {
        if (isAlreadyReviewed) {
            throw new BusinessException(ErrorCode.MATCH_REVIEW_ALREADY_REVIEWED);
        }
    }

    public LocalDateTime getReviewableThreshold(LocalDateTime now) {
        return now.minus(reviewSubmitLimit);
    }
}
