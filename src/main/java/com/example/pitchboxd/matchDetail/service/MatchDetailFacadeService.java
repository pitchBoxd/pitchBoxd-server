package com.example.pitchboxd.matchDetail.service;

import com.example.pitchboxd.match.core.infrastructure.dto.MatchDetailStaticModel;
import com.example.pitchboxd.match.core.service.domain.MatchQueryService;
import com.example.pitchboxd.match.lineup.infrastructure.dto.LineupPlayerModel;
import com.example.pitchboxd.match.lineup.service.MatchLineupQueryService;
import com.example.pitchboxd.matchDetail.dto.response.MatchDetailResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MatchDetailFacadeService {

    private final MatchQueryService matchQueryService;
    private final MatchLineupQueryService matchLineupQueryService;

    public MatchDetailResponse getMatchStaticData(Long matchId) {
        MatchDetailStaticModel matchDetail = matchQueryService.findMatchStaticDetailById(matchId);
        List<LineupPlayerModel> lineups = matchLineupQueryService.findLineupAndPlayedPlayers(matchId);

        List<LineupPlayerModel> homeLineups = lineups.stream()
                .filter(l -> l.teamId().equals(matchDetail.homeTeamId()))
                .toList();

        List<LineupPlayerModel> awayLineups = lineups.stream()
                .filter(l -> l.teamId().equals(matchDetail.awayTeamId()))
                .toList();

        return MatchDetailResponse.from(matchDetail, homeLineups, awayLineups);
    }
}
