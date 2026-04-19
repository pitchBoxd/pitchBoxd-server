package com.example.pitchboxd.match.core.service.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import com.example.pitchboxd.match.core.dto.response.MatchResponses;
import com.example.pitchboxd.match.core.infrastructure.MatchRepository;
import com.example.pitchboxd.support.DatabaseCleaner;
import com.example.pitchboxd.team.domain.Team;
import com.example.pitchboxd.team.infrastructure.TeamRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
@DisplayNameGeneration(ReplaceUnderscores.class)
class MatchFacadeServiceTest {

    @Autowired
    private MatchFacadeService matchFacadeService;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    private Team homeTeam;
    private Team awayTeam;
    private Team otherTeam1;
    private Team otherTeam2;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();
        homeTeam = teamRepository.save(new Team("홈팀", "1"));
        awayTeam = teamRepository.save(new Team("어웨이팀", "2"));

        otherTeam1 = teamRepository.save(new Team("다른팀1", "3"));
        otherTeam2 = teamRepository.save(new Team("다른팀2", "4"));
    }

    @AfterEach
    void tearDown() {
        databaseCleaner.clean();
    }

    @Test
    void 리뷰_가능한_최근_종료된_경기_목록을_조회한다() {
        // given
        LocalDateTime now = LocalDateTime.now();

        // 리뷰 가능한 범위 내의 경기 (최근 종료)
        Match reviewableMatch = new Match(
                1L,
                "1",
                homeTeam.getId(),
                awayTeam.getId(),
                now.minusHours(3),
                MatchStatus.FINISHED,
                "경기장",
                "1"
        );
        reviewableMatch.finish(now.minusHours(1));
        matchRepository.save(reviewableMatch);

        // 리뷰 가능한 범위를 벗어난 경기 (오래전 종료)
        Match oldMatch = new Match(
                1L,
                "0",
                otherTeam1.getId(),
                otherTeam2.getId(),
                now.minusDays(7),
                MatchStatus.FINISHED,
                "경기장",
                "1"
        );
        oldMatch.finish(now.minusDays(6));
        matchRepository.save(oldMatch);

        // 아직 종료되지 않은 경기
        Match upcomingMatch = new Match(
                1L,
                "0",
                otherTeam1.getId(),
                otherTeam2.getId(),
                now.minusDays(7),
                MatchStatus.SCHEDULED,
                "경기장",
                "1"
        );
        matchRepository.save(upcomingMatch);

        // when
        MatchResponses result = matchFacadeService.findReviewableMatches();

        // then
        assertAll(
                () -> assertThat(result.matchResponses()).hasSize(1),
                () -> assertThat(result.matchResponses().get(0).homeTeam()).isEqualTo("홈팀"),
                () -> assertThat(result.matchResponses().get(0).awayTeam()).isEqualTo("어웨이팀")
        );
    }
}
