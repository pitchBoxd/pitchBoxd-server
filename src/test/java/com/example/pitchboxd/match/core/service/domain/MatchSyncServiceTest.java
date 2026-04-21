package com.example.pitchboxd.match.core.service.domain;

import com.example.pitchboxd.admin.dto.request.CreateMatchRequest;
import com.example.pitchboxd.admin.service.sync.MatchSyncService;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.infrastructure.MatchRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@DisplayNameGeneration(ReplaceUnderscores.class)
class MatchSyncServiceTest {

    @Autowired
    private MatchSyncService matchSyncService;

    @Autowired
    private MatchRepository matchRepository;

    @Test
    void 외부_경기_로딩_테스트() {
        // given
        CreateMatchRequest request = new CreateMatchRequest(LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 6));
        matchSyncService.syncLeagueMatches(request);

        // when
        List<Match> matches = matchRepository.findAll();

        // then
        for (Match match : matches) {
            System.out.println(match.toString());
        }
    }
}
