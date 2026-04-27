package com.example.pitchboxd.admin.service.sync;

import com.example.pitchboxd.admin.dto.request.CreateMatchRequest;
import com.example.pitchboxd.global.domain.ClockHolder;
import com.example.pitchboxd.global.infrastructure.naver.NaverSportsClient;
import com.example.pitchboxd.global.infrastructure.naver.dto.NaverMatchDetailResponse;
import com.example.pitchboxd.global.infrastructure.naver.dto.NaverMatchResponse;
import com.example.pitchboxd.global.infrastructure.naver.dto.NaverScheduleWrapper;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchResult;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import com.example.pitchboxd.match.core.service.domain.MatchService;
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

    private final NaverSportsClient naverSportsClient;
    private final TeamQueryService teamQueryService;
    private final MatchService matchService;
    private final ClockHolder clockHolder;

    @Transactional
    public List<Long> syncLeagueMatches(CreateMatchRequest request) {
        NaverScheduleWrapper externalMatches = naverSportsClient.fetchMatches(request.from(), request.to());

        // TODO: 현재 메서드를 사용하면 기간 내 경기를 다시 저장하게 됨. 이미 있는 경기가 두 번 저장될 수도 있음.
        //  근데 Match의 naverID는 유니크 제약이 걸려 있어서 멱등성으로 막아야 한다.
        //  따라서 이미 존재하는 경기의 경우 건들면 안되는 로직 작성해야 함

        List<Match> matches = new ArrayList<>();
        for (NaverMatchResponse dto : externalMatches.getMatches()) {
            Match match = mapToMatch(dto);
            matches.add(match);
        }

        List<Match> savedMatches = matchService.createAllMatches(matches);
        return savedMatches.stream()
                .map(Match::getId)
                .toList();
    }

    @Transactional
    public void finishMatch(String matchCode) {
        LocalDateTime now = clockHolder.now();
        Match match = matchService.findByNaverId(matchCode);

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
