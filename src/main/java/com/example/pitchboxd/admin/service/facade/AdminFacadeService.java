package com.example.pitchboxd.admin.service.facade;

import com.example.pitchboxd.admin.service.MatchLineupSyncService;
import com.example.pitchboxd.admin.service.MatchSyncService;
import com.example.pitchboxd.match.core.dto.request.CreateMatchRequest;
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

    @Transactional
    public void finishMatchAndUpdateLineup(String naverGameId) {
        matchSyncService.finishMatch(naverGameId);
        matchLineupSyncService.syncLineup(naverGameId);
    }

    @Transactional
    public void syncMatchesAndStatistics(CreateMatchRequest request) {
        List<Long> matchIds = matchSyncService.syncKLeagueMatches(request);
        matchStatisticsService.createAllStatistics(matchIds);
    }
}
