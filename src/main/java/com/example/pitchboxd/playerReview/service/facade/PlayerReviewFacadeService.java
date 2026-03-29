package com.example.pitchboxd.playerReview.service.facade;

import com.example.pitchboxd.global.domain.ClockHolder;
import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.match.domain.Match;
import com.example.pitchboxd.match.service.domain.MatchService;
import com.example.pitchboxd.matchLineup.domain.MatchLineup;
import com.example.pitchboxd.matchLineup.service.MatchLineupService;
import com.example.pitchboxd.player.domain.Player;
import com.example.pitchboxd.player.service.PlayerService;
import com.example.pitchboxd.playerMatchStatistics.service.PlayerMatchStatisticsService;
import com.example.pitchboxd.playerReview.domain.PlayerReview;
import com.example.pitchboxd.playerReview.dto.request.PlayerReviewCreateRequest;
import com.example.pitchboxd.playerReview.dto.response.PlayerReviewCreateResponse;
import com.example.pitchboxd.playerReview.service.domain.PlayerReviewService;
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
public class PlayerReviewFacadeService {

    private static final Duration REVIEW_LIMIT = Duration.ofHours(24);

    private final MatchService matchService;
    private final UserService userService;
    private final PlayerService playerService;
    private final PlayerReviewService playerReviewService;
    private final MatchLineupService matchLineupService;
    private final PlayerMatchStatisticsService playerMatchStatisticsService;
    private final ClockHolder clockHolder;

    /***
     * 선수 리뷰 가능 여부는 다음과 컨디션을 따릅니다.
     * 1. 경기가 종료된 후, 경기가 종료된지 24시간 이내여야 합니다.
     * 2. 해당 경기에서 해당 선수에게 리뷰을 달지 않았어야 합니다.
     * 3. 선수가 경기에 출전(선발 or 교체출전) 해야합니다.
     * 4. 선수의 팀과 유저의 응원 팀이 같아야 합니다.
     * ***/
    @Transactional
    public PlayerReviewCreateResponse submitReview(PlayerReviewCreateRequest request, Long matchId, Long userId) {
        Long playerId = request.playerId();
        Match match = matchService.findMatch(matchId);

        LocalDateTime now = clockHolder.now();
        if (!match.isEnd(now) || match.isPassed(now, REVIEW_LIMIT)) {
            throw new BusinessException(ErrorCode.MATCH_REVIEW_TIME_LIMIT_PASSED);
        }

        if (playerReviewService.hasAlreadyReviewed(matchId, playerId, userId)) {
            throw new BusinessException(ErrorCode.PLAYER_REVIEW_ALREADY_REVIEWED);
        }

        MatchLineup matchLineup = matchLineupService.findMatchLineup(matchId, playerId); // 라인업에 없는 경우는 여기서 걸러짐
        if (!matchLineup.isParticipated()) {
            throw new BusinessException(ErrorCode.MATCH_LINEUP_DID_NOT_PARTICIPATE); // 선수가 겸기에 참여하지 않음(벤치)
        }

        User user = userService.findUser(userId);
        Player player = playerService.findPlayer(request.playerId());
        Long playerTeamId = player.getTeamId();

        if (!user.isFanOf(playerTeamId)) {
            throw new BusinessException(ErrorCode.PLAYER_REVIEW_NOT_FAN);
        }

        PlayerReview savedPlayerReview = playerReviewService.save(request, matchId, userId);

        //TODO: 일단 동기적으로 만들어두고, 나중에 이벤트 리스너로 분리 ㄱㄱ
        // 나중엔 트랜잭셔널 아웃박스 패턴으로 정합성 보장해보는것도 좋을듯. 이벤트 리스너의 유실 문제 해결을 위해서
        playerMatchStatisticsService.updateReview(matchId, playerId, request.point());

        return new PlayerReviewCreateResponse(savedPlayerReview.getId());
    }
}
