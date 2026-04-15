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

    public void validateMatchStatus(Match match, LocalDateTime now) {
        if (!match.isEnd(now) || match.isPassed(now, REVIEW_SUBMIT_LIMIT)) {
            throw new BusinessException(ErrorCode.MATCH_REVIEW_INVALID_REVIEW_TIME);
        }
    }

    public void validateUserCondition(boolean isAlreadyReviewed) {
        if (isAlreadyReviewed) {
            throw new BusinessException(ErrorCode.MATCH_REVIEW_ALREADY_REVIEWED);
        }
    }
}
