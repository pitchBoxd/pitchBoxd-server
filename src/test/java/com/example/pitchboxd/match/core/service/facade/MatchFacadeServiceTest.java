package com.example.pitchboxd.match.core.service.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import com.example.pitchboxd.match.core.dto.response.MatchResponses;
import com.example.pitchboxd.match.core.infrastructure.MatchRepository;
import com.example.pitchboxd.support.DatabaseCleaner;
import com.example.pitchboxd.team.domain.Team;
import com.example.pitchboxd.team.infrastructure.TeamRepository;
import com.example.pitchboxd.user.domain.User;
import com.example.pitchboxd.user.infrastructure.UserRepository;
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
    private UserRepository userRepository;

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
        MatchResponses result = matchFacadeService.findReviewableMatches(null, "all");

        // then
        assertAll(
                () -> assertThat(result.matchResponses()).hasSize(1),
                () -> assertThat(result.matchResponses().get(0).homeTeam()).isEqualTo("홈팀"),
                () -> assertThat(result.matchResponses().get(0).awayTeam()).isEqualTo("어웨이팀")
        );
    }

    @Test
    void 비로그인_유저가_내_팀_경기_목록을_조회하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> matchFacadeService.findReviewableMatches(null, "my"))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.UNAUTHORIZED.getMessage());
    }

    @Test
    void 로그인_유저는_내_팀_경기_목록만_조회한다() {
        // given
        User user = userRepository.save(new User("닉네임", "test@test.com", "password", homeTeam.getId()));
        LocalDateTime now = LocalDateTime.now();

        // 내 팀 경기 (리뷰 가능)
        Match myMatch = new Match(
                1L,
                "1",
                homeTeam.getId(),
                awayTeam.getId(),
                now.minusHours(3),
                MatchStatus.FINISHED,
                "경기장",
                "1"
        );
        myMatch.finish(now.minusHours(1));
        matchRepository.save(myMatch);

        // 다른 팀 경기 (리뷰 가능하지만 내 팀이 아님)
        Match otherMatch = new Match(
                1L,
                "2",
                otherTeam1.getId(),
                otherTeam2.getId(),
                now.minusHours(3),
                MatchStatus.FINISHED,
                "경기장",
                "1"
        );
        otherMatch.finish(now.minusHours(1));
        matchRepository.save(otherMatch);

        // when
        MatchResponses result = matchFacadeService.findReviewableMatches(user.getId(), "my");

        // then
        assertAll(
                () -> assertThat(result.matchResponses()).hasSize(1),
                () -> assertThat(result.matchResponses().get(0).homeTeam()).isEqualTo("홈팀")
        );
    }
}
