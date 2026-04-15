package com.example.pitchboxd.match.playerReview.domain;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.lineup.domain.MatchLineup;
import com.example.pitchboxd.player.domain.Player;
import com.example.pitchboxd.user.domain.User;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class PlayerReviewSubmitPolicy {

    private static final Duration REVIEW_SUBMIT_LIMIT = Duration.ofHours(24);

    public void validateMatchStatus(Match match, LocalDateTime now) {
        if (!match.isEnd(now) || match.isPassed(now, REVIEW_SUBMIT_LIMIT)) {
            throw new BusinessException(ErrorCode.MATCH_REVIEW_TIME_LIMIT_PASSED);
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
