package com.example.pitchboxd.admin.service.facade;

import com.example.pitchboxd.admin.dto.request.CreateMatchRequest;
import com.example.pitchboxd.admin.dto.request.CreatePlayerRequest;
import com.example.pitchboxd.admin.dto.request.UpdateMatchRequest;
import com.example.pitchboxd.admin.dto.request.UpdatePlayerRequest;
import com.example.pitchboxd.admin.dto.response.AdminUserResponse;
import com.example.pitchboxd.admin.service.sync.MatchLineupSyncService;
import com.example.pitchboxd.admin.service.sync.MatchSyncService;
import com.example.pitchboxd.admin.service.sync.PlayerSyncService;
import com.example.pitchboxd.global.infrastructure.naver.NaverSportsClient;
import com.example.pitchboxd.global.infrastructure.naver.dto.NaverMatchDetailResponse;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchResult;
import com.example.pitchboxd.match.core.service.domain.MatchService;
import com.example.pitchboxd.match.matchStatistics.service.domain.MatchStatisticsService;
import com.example.pitchboxd.player.domain.Player;
import com.example.pitchboxd.player.service.PlayerService;
import com.example.pitchboxd.user.application.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminFacadeService {

    private final MatchSyncService matchSyncService;
    private final MatchLineupSyncService matchLineupSyncService;
    private final MatchStatisticsService matchStatisticsService;
    private final PlayerSyncService playerSyncService;
    private final PlayerService playerService;
    private final MatchService matchService;
    private final NaverSportsClient naverSportsClient;
    private final ObjectProvider<AdminFacadeService> selfProvider;
    private final UserService userService;
    
    public void autoFinishMatchesAndUpdateLineup(CreateMatchRequest request) {
        List<Match> matches = matchSyncService.findMatchesInPeriod(request.from(), request.to());
        log.info("Found {} matches in period to check and sync finished details.", matches.size());

        AdminFacadeService self = selfProvider.getObject();
        for (Match match : matches) {
            try {
                NaverMatchDetailResponse detail = naverSportsClient.getMatchDetail(match.getNaverId());
                if (detail.isFinished()) {
                    self.finishMatchAndUpdateLineup(match.getNaverId());
                    log.info("Successfully finished/updated and synced lineup for match: {}", match.getNaverId());
                } else {
                    log.info("Match is not finished yet: {}", match.getNaverId());
                }
            } catch (Exception e) {
                log.error("Failed to auto-finish/update match (naverId: {}): {}", match.getNaverId(), e.getMessage(),
                        e);
            }
        }
    }

    @Transactional
    public void finishMatchAndUpdateLineup(String naverGameId) {
        matchSyncService.finishMatch(naverGameId);
        matchLineupSyncService.syncLineup(naverGameId);
    }

    @Transactional
    public void syncMatchesAndStatistics(CreateMatchRequest request) {
        List<Long> matchIds = matchSyncService.syncLeagueMatches(request);
        matchStatisticsService.createAllStatistics(matchIds);
    }

    @Transactional
    public void syncPlayers(CreatePlayerRequest request) {
        playerSyncService.syncAllPlayers(request.season());
    }

    @Transactional
    public void updateMatch(Long matchId, UpdateMatchRequest request) {
        Match match = matchService.findById(matchId);
        MatchResult matchResult = null;

        if (request.matchResult() != null) {
            matchResult = request.matchResult().toEmbeddable();
        }

        match.update(request.homeTeamId(), request.awayTeamId(), request.startTime(), request.finishedAt(),
                request.status(), request.location(), matchResult, request.naverId());
    }

    @Transactional
    public void updatePlayer(Long playerId, UpdatePlayerRequest request) {
        Player player = playerService.findPlayer(playerId);

        player.update(request.teamId(), request.name(), request.naverId());
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> getAllUsers() {
        return userService.findAllUsers().stream()
                .map(AdminUserResponse::from)
                .toList();
    }
}
