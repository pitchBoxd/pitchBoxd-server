package com.example.pitchboxd.match.playerReview.domain;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.lineup.domain.MatchLineup;
import com.example.pitchboxd.player.domain.Player;
import com.example.pitchboxd.user.domain.User;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PlayerReviewSubmitPolicy {

    private final Duration REVIEW_SUBMIT_LIMIT;

    public PlayerReviewSubmitPolicy(@Value("${app.policy.player-review-limit}") Duration reviewSubmitLimit) {
        this.REVIEW_SUBMIT_LIMIT = reviewSubmitLimit;
    }

    public void validateMatchStatus(Match match, LocalDateTime now) {
        if (!match.isEnd(now) || match.isPassed(now, REVIEW_SUBMIT_LIMIT)) {
            throw new BusinessException(ErrorCode.PLAYER_REVIEW_INVALID_REVIEW_TIME);
        }
    }

    public void validatePlayerParticipation(MatchLineup matchLineup) {
        if (!matchLineup.isParticipated()) {
            throw new BusinessException(ErrorCode.MATCH_LINEUP_DID_NOT_PARTICIPATE);
        }
    }

    public void validateUserCondition(User user, Player player, boolean isAlreadyReviewed) {
        if (isAlreadyReviewed) {
            throw new BusinessException(ErrorCode.PLAYER_REVIEW_ALREADY_REVIEWED);
        }
        if (!user.isFanOf(player.getTeamId())) {
            throw new BusinessException(ErrorCode.PLAYER_REVIEW_NOT_FAN);
        }
    }
}
