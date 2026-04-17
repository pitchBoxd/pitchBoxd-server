package com.example.pitchboxd.match.core.service.domain;

import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.dto.request.CreateMatchRequest;
import com.example.pitchboxd.match.core.infrastructure.MatchRepository;
import com.example.pitchboxd.match.core.infrastructure.external.NaverSportsMatchClient;
import com.example.pitchboxd.match.core.infrastructure.external.dto.NaverMatchResponse;
import com.example.pitchboxd.match.core.infrastructure.external.dto.NaverScheduleWrapper;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchSyncService {

    private final NaverSportsMatchClient naverSportsClient;
    private final MatchRepository matchRepository;

    public void syncKLeagueMatches(CreateMatchRequest request) {
        NaverScheduleWrapper externalMatches = naverSportsClient.fetchMatches(request.from(), request.to());
        List<Match> matches = new ArrayList<>();

        for (NaverMatchResponse dto : externalMatches.getMatches()) {
            Match match = dto.toMatch();
            matches.add(match);
        }
        
        matchRepository.saveAll(matches);
    }
}
