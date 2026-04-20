package com.example.pitchboxd.match.core.service.domain;

import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchResult;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import com.example.pitchboxd.match.core.dto.request.CreateMatchRequest;
import com.example.pitchboxd.match.core.infrastructure.MatchRepository;
import com.example.pitchboxd.match.core.infrastructure.external.NaverSportsMatchClient;
import com.example.pitchboxd.match.core.infrastructure.external.dto.NaverMatchResponse;
import com.example.pitchboxd.match.core.infrastructure.external.dto.NaverScheduleWrapper;
import com.example.pitchboxd.team.domain.Team;
import com.example.pitchboxd.team.service.TeamQueryService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchSyncService {

    private final NaverSportsMatchClient naverSportsClient;
    private final MatchRepository matchRepository;
    private final TeamQueryService teamQueryService;

    public void syncKLeagueMatches(CreateMatchRequest request) {
        NaverScheduleWrapper externalMatches = naverSportsClient.fetchMatches(request.from(), request.to());
        List<Match> matches = new ArrayList<>();

        for (NaverMatchResponse dto : externalMatches.getMatches()) {
            Match match = mapToMatch(dto);
            matches.add(match);
        }

        matchRepository.saveAll(matches);
    }

    private Match mapToMatch(NaverMatchResponse response) {
        Team homeTeam = teamQueryService.findByNaverCode(response.homeTeamCode());
        Team awayTeam = teamQueryService.findByNaverCode(response.awayTeamCode());

        Match match = new Match(1L, response.matchRound(), homeTeam.getId(), awayTeam.getId(), response.gameDateTime(),
                MatchStatus.SCHEDULED, response.stadium(), response.gameId());

        if (response.statusCode().equals("RESULT")) {
            MatchResult matchResult = new MatchResult(response.homeTeamScore(), response.awayTeamScore(), List.of(),
                    List.of());
            match.finish(response.gameDateTime().plusHours(2));
            match.decideMatchResult(matchResult);
        }

        return match;
    }
}
