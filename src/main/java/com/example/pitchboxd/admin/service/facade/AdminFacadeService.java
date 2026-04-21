package com.example.pitchboxd.admin.service.facade;

import com.example.pitchboxd.admin.dto.request.CreateMatchRequest;
import com.example.pitchboxd.admin.dto.request.CreatePlayerRequest;
import com.example.pitchboxd.admin.service.sync.MatchLineupSyncService;
import com.example.pitchboxd.admin.service.sync.MatchSyncService;
import com.example.pitchboxd.admin.service.sync.PlayerSyncService;
import com.example.pitchboxd.match.matchStatistics.service.domain.MatchStatisticsService;
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
}
