package com.example.pitchboxd.match.core.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.auth.application.TokenManager;
import com.example.pitchboxd.global.domain.ClockHolder;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchResult;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import com.example.pitchboxd.match.core.dto.response.MatchResponses;
import com.example.pitchboxd.match.core.infrastructure.MatchRepository;
import com.example.pitchboxd.match.matchStatistics.domain.MatchStatistics;
import com.example.pitchboxd.match.matchStatistics.infrastructure.MatchStatisticsRepository;
import com.example.pitchboxd.support.DatabaseCleaner;
import com.example.pitchboxd.support.TestClockHolder;
import com.example.pitchboxd.team.domain.Team;
import com.example.pitchboxd.team.infrastructure.TeamRepository;
import com.example.pitchboxd.user.domain.User;
import com.example.pitchboxd.user.infrastructure.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class MatchControllerTest {

    private final ClockHolder testClock = new TestClockHolder(LocalDateTime.now());
    @LocalServerPort
    private int port;
    @Autowired
    private DatabaseCleaner databaseCleaner;
    @Autowired
    private TokenManager tokenManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private MatchStatisticsRepository matchStatisticsRepository;

    private String accessToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.clean();

        User user = userRepository.save(new User("테스트유저", "test@example.com", "password123!"));
        accessToken = tokenManager.createAccessToken(user.getId(), user.getEmail());
    }

    @AfterEach
    void tearDown() {
        databaseCleaner.clean();
    }

    @Test
    void 리뷰_가능한_경기_목록을_조회한다() {
        // given
        Team home = teamRepository.save(new Team("홈팀", "naver-home"));
        Team away = teamRepository.save(new Team("어웨이팀", "naver-away"));

        LocalDateTime now = LocalDateTime.now();

        // 1시간 전 종료된 경기 (리뷰 가능)
        Match reviewableMatch = new Match(1L, "1라운드", home.getId(), away.getId(), now.minusHours(3),
                MatchStatus.FINISHED, "상암", "naver-match-1");
        reviewableMatch.finish(now.minusHours(1));
        reviewableMatch.decideMatchResult(new MatchResult(2, 1, List.of(), List.of()));
        matchRepository.save(reviewableMatch);

        matchStatisticsRepository.save(new MatchStatistics(reviewableMatch.getId()));

        // 50시간 전 종료된 경기 (리뷰 불가능 - threshold 48시간)
        Match expiredMatch = new Match(1L, "1라운드", home.getId(), away.getId(), now.minusHours(52),
                MatchStatus.FINISHED, "상암", "naver-match-2");
        expiredMatch.finish(now.minusHours(50));
        expiredMatch.decideMatchResult(new MatchResult(0, 0, List.of(), List.of()));
        matchRepository.save(expiredMatch);

        matchStatisticsRepository.save(new MatchStatistics(expiredMatch.getId()));

        // when & then
        MatchResponses response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("state", "REVIEWABLE")
                .when().get("/api/v1/matches")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MatchResponses.class);

        assertThat(response.matchResponses()).hasSize(1);
        assertThat(response.matchResponses().get(0).id()).isEqualTo(reviewableMatch.getId());
    }

    @Test
    void 특정_시즌의_경기_목록을_조회한다() {
        // given
        Team home = teamRepository.save(new Team("홈팀", "naver-home-season"));
        Team away = teamRepository.save(new Team("어웨이팀", "naver-away-season"));
        LocalDateTime now = LocalDateTime.now();

        Long season1 = 100L;
        Long season2 = 200L;

        Match matchInSeason1 = new Match(season1, "1라운드", home.getId(), away.getId(), now,
                MatchStatus.SCHEDULED, "상암", "naver-match-season-1");
        matchRepository.save(matchInSeason1);
        matchStatisticsRepository.save(new MatchStatistics(matchInSeason1.getId()));

        Match matchInSeason2 = new Match(season2, "1라운드", home.getId(), away.getId(), now,
                MatchStatus.SCHEDULED, "상암", "naver-match-season-2");
        matchRepository.save(matchInSeason2);
        matchStatisticsRepository.save(new MatchStatistics(matchInSeason2.getId()));

        // when & then
        MatchResponses response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("season", season1)
                .when().get("/api/v1/matches")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MatchResponses.class);

        assertAll(
                () -> assertThat(response.matchResponses()).hasSize(1),
                () -> assertThat(response.matchResponses().get(0).id()).isEqualTo(matchInSeason1.getId())
        );
    }
}
