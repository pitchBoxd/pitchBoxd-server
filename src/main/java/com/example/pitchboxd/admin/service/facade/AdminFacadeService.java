package com.example.pitchboxd.admin.service.facade;

import com.example.pitchboxd.admin.dto.request.CreateMatchRequest;
import com.example.pitchboxd.admin.dto.request.CreatePlayerRequest;
import com.example.pitchboxd.admin.dto.request.UpdateMatchRequest;
import com.example.pitchboxd.admin.dto.request.UpdatePlayerRequest;
import com.example.pitchboxd.admin.dto.response.AdminMatchResponse;
import com.example.pitchboxd.admin.dto.response.AdminPlayerResponse;
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
    private final com.example.pitchboxd.team.service.TeamQueryService teamQueryService;
    
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

    @Transactional(readOnly = true)
    public List<com.example.pitchboxd.admin.dto.response.AdminMatchResponse> getMatchesBySeason(Long seasonId) {
        List<Match> matches = matchService.findMatchesBySeasonId(seasonId);
        java.util.Map<Long, String> teamNameMap = teamQueryService.findAllTeam().stream()
                .collect(java.util.stream.Collectors.toMap(com.example.pitchboxd.team.domain.Team::getId, com.example.pitchboxd.team.domain.Team::getName));
        return matches.stream()
                .map(match -> com.example.pitchboxd.admin.dto.response.AdminMatchResponse.of(
                        match,
                        teamNameMap.getOrDefault(match.getHomeTeamId(), "알 수 없음"),
                        teamNameMap.getOrDefault(match.getAwayTeamId(), "알 수 없음")
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminPlayerResponse> getAllPlayers() {
        List<Player> players = playerService.findAll();
        java.util.Map<Long, String> teamNameMap = teamQueryService.findAllTeam().stream()
                .collect(java.util.stream.Collectors.toMap(com.example.pitchboxd.team.domain.Team::getId, com.example.pitchboxd.team.domain.Team::getName));
        return players.stream()
                .map(player -> AdminPlayerResponse.of(player, teamNameMap.getOrDefault(player.getTeamId(), "알 수 없음")))
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminMatchResponse getMatch(Long matchId) {
        Match match = matchService.findById(matchId);
        com.example.pitchboxd.team.domain.Team home = teamQueryService.findById(match.getHomeTeamId());
        com.example.pitchboxd.team.domain.Team away = teamQueryService.findById(match.getAwayTeamId());
        return AdminMatchResponse.of(match, home.getName(), away.getName());
    }
}
