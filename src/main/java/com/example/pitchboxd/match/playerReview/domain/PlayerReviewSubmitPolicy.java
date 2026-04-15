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

    /***
     * 선수 리뷰 가능 여부는 다음과 조건을 따릅니다.
     * 1. 경기가 종료된 후, 경기가 종료된지 24시간 이내여야 합니다.
     * 2. 유저가 해당 경기에서 해당 선수에게 리뷰을 달지 않았어야 합니다.
     * 3. 선수가 경기에 출전(선발 or 교체출전) 해야합니다.
     * 4. 선수의 팀과 유저의 응원 팀이 같아야 합니다.
     * ***/
    public void validate(Match match, MatchLineup matchLineup, User user, Player player, boolean isAlreadyReviewed,
                         LocalDateTime now) {
        Long playerTeamId = player.getTeamId();
        boolean isFan = user.isFanOf(playerTeamId);

        if (!match.isEnd(now) || match.isPassed(now, REVIEW_SUBMIT_LIMIT)) {
            throw new BusinessException(ErrorCode.MATCH_REVIEW_TIME_LIMIT_PASSED);
        }

        if (isAlreadyReviewed) {
            throw new BusinessException(ErrorCode.PLAYER_REVIEW_ALREADY_REVIEWED);
        }

        if (!matchLineup.isParticipated()) {
            throw new BusinessException(ErrorCode.MATCH_LINEUP_DID_NOT_PARTICIPATE); // 선수가 겸기에 참여하지 않음(벤치)
        }

        if (!isFan) {
            throw new BusinessException(ErrorCode.PLAYER_REVIEW_NOT_FAN);
        }
    }
}
