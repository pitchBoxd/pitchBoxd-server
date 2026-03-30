package com.example.pitchboxd.match.service.domain;

import com.example.pitchboxd.match.domain.Match;
import com.example.pitchboxd.match.infrastructure.MatchRepository;
import com.example.pitchboxd.match.infrastructure.external.NaverSportsMatchClient;
import com.example.pitchboxd.match.infrastructure.external.dto.NaverMatchResponse;
import com.example.pitchboxd.match.infrastructure.external.dto.NaverScheduleWrapper;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchSyncService {

    private final NaverSportsMatchClient naverSportsClient;
    private final MatchRepository matchRepository;

    public void syncKLeagueMatches(LocalDate from, LocalDate to) {
        NaverScheduleWrapper externalMatches = naverSportsClient.fetchMatches(from, to);

        for (NaverMatchResponse dto : externalMatches.getMatches()) {
            System.out.println(dto);

            Match match = dto.toMatch();

            matchRepository.save(match);
        }
    }
}
