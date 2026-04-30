package com.example.pitchboxd.admin.service.facade;

import com.example.pitchboxd.admin.dto.request.CreateMatchRequest;
import com.example.pitchboxd.admin.dto.request.CreatePlayerRequest;
import com.example.pitchboxd.admin.dto.request.UpdateMatchRequest;
import com.example.pitchboxd.admin.dto.request.UpdatePlayerRequest;
import com.example.pitchboxd.admin.service.sync.MatchLineupSyncService;
import com.example.pitchboxd.admin.service.sync.MatchSyncService;
import com.example.pitchboxd.admin.service.sync.PlayerSyncService;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchResult;
import com.example.pitchboxd.match.core.service.domain.MatchService;
import com.example.pitchboxd.match.matchStatistics.service.domain.MatchStatisticsService;
import com.example.pitchboxd.player.domain.Player;
import com.example.pitchboxd.player.service.PlayerService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminFacadeService {

    private final MatchSyncService matchSyncService;
    private final MatchLineupSyncService matchLineupSyncService;
    private final MatchStatisticsService matchStatisticsService;
    private final PlayerSyncService playerSyncService;
    private final PlayerService playerService;
    private final MatchService matchService;

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
}
