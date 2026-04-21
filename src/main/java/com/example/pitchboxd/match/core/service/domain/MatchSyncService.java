package com.example.pitchboxd.match.core.service.domain;

import com.example.pitchboxd.global.domain.ClockHolder;
import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchResult;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import com.example.pitchboxd.match.core.dto.request.CreateMatchRequest;
import com.example.pitchboxd.match.core.infrastructure.MatchRepository;
import com.example.pitchboxd.match.core.infrastructure.external.NaverSportsMatchClient;
import com.example.pitchboxd.match.core.infrastructure.external.dto.NaverMatchDetailResponse;
import com.example.pitchboxd.match.core.infrastructure.external.dto.NaverMatchResponse;
import com.example.pitchboxd.match.core.infrastructure.external.dto.NaverScheduleWrapper;
import com.example.pitchboxd.team.domain.Team;
import com.example.pitchboxd.team.service.TeamQueryService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MatchSyncService {

    private final NaverSportsMatchClient naverSportsClient;
    private final MatchRepository matchRepository;
    private final TeamQueryService teamQueryService;
    private ClockHolder clockHolder;

    @Transactional
    public void syncKLeagueMatches(CreateMatchRequest request) {
        NaverScheduleWrapper externalMatches = naverSportsClient.fetchMatches(request.from(), request.to());
        List<Match> matches = new ArrayList<>();

        for (NaverMatchResponse dto : externalMatches.getMatches()) {
            Match match = mapToMatch(dto);
            matches.add(match);
        }

        matchRepository.saveAll(matches);
    }

    @Transactional
    public void updateMatch(String matchCode) {
        LocalDateTime now = clockHolder.now();
        Match match = matchRepository.findByNaverId(matchCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));

        NaverMatchDetailResponse response = naverSportsClient.getMatchDetail(matchCode);
        MatchResult matchResult = new MatchResult(response.homeTeamScore(), response.awayTeamScore(),
                response.toHomeScorers(), response.toAwayScorers());

        match.finish(now);
        match.decideMatchResult(matchResult);
    }

    private Match mapToMatch(NaverMatchResponse response) {
        Team homeTeam = teamQueryService.findByNaverCode(response.homeTeamCode());
        Team awayTeam = teamQueryService.findByNaverCode(response.awayTeamCode());

        Match match = new Match(1L, response.matchRound(), homeTeam.getId(), awayTeam.getId(), response.gameDateTime(),
                MatchStatus.SCHEDULED, response.stadium(), response.gameId());

        if (response.statusCode().equals("RESULT")) {
            match.finish(response.gameDateTime().plusHours(2));
        }

        return match;
    }
}
