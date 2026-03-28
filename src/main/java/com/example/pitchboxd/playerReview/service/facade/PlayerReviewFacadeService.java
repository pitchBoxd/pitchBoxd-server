package com.example.pitchboxd.playerReview.service.facade;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlayerReviewFacadeService {

    private final MatchService matchService;
    private final UserService userService;
    private final PlayerService playerService;
    private final PlayerReviewService playerReviewService;
    private final MatchLineupService matchLineupService;
    private final PlayerMatchStatisticsService playerMatchStatisticsService;

    @Transactional
    public PlayerReviewCreateResponse submitReview(PlayerReviewCreateRequest request, Long matchId, Long userId) {
        Long playerId = request.playerId();
        Match match = matchService.findMatch(matchId);
        // TODO: 나중에 매치에 리뷰를 달 수 있는 시간인지 확인한다(리뷰는 24시간 이내로 작성 가능하게 한다.)

        // 리뷰 중복 검증 (멱등성)
        if (playerReviewService.hasAlreadyReviewed(matchId, playerId, userId)) {
            throw new BusinessException(ErrorCode.PLAYER_REVIEW_ALREADY_REVIEWED);
        }

        // 선수가 경기에 출전했는지?
        MatchLineup matchLineup = matchLineupService.findMatchLineup(matchId, playerId);
        if (!matchLineup.isParticipated()) {
            throw new BusinessException(ErrorCode.MATCH_LINEUP_DID_NOT_PARTICIPATE); // 선수가 겸기에 참여하지 않음(벤치)
        }

        // 선수의 팀과 유저의 응원 팀이 같은지?
        User user = userService.findUser(userId);
        Player player = playerService.findPlayer(request.playerId());
        Long playerTeamId = player.getTeamId();
        if (!user.isFanOf(playerTeamId)) {
            throw new BusinessException(ErrorCode.PLAYER_REVIEW_NOT_FAN); // 유저의 응원팀이 선수의 팀과 다름
        }

        PlayerReview savedPlayerReview = playerReviewService.save(request, matchId, userId);

        // 일단 동기적으로 만들어두고, 나중에 이벤트 리스너로 분리 ㄱㄱ
        // 나중엔 트랜잭셔널 아웃박스 패턴으로 정합성 보장해보는것도 좋을듯. 이벤트 리스너의 유실 문제 해결을 위해서
        playerMatchStatisticsService.updateReview(matchId, playerId, request.point());

        return new PlayerReviewCreateResponse(savedPlayerReview.getId());
    }
}
