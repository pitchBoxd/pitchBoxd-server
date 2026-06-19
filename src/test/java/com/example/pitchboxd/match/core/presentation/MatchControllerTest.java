package com.example.pitchboxd.match.core.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.auth.application.TokenManager;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import com.example.pitchboxd.match.core.dto.response.MatchResponses;
import com.example.pitchboxd.match.core.infrastructure.MatchRepository;
import com.example.pitchboxd.support.DatabaseCleaner;
import com.example.pitchboxd.team.domain.Team;
import com.example.pitchboxd.team.infrastructure.TeamRepository;
import com.example.pitchboxd.user.domain.User;
import com.example.pitchboxd.user.infrastructure.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDateTime;
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

    @LocalServerPort
    private int port;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenManager tokenManager;

    private String accessToken;
    private Team teamA;
    private Team teamB;
    private Team teamC;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.clean();

        User user = userRepository.save(new User("nickname", "email@gmail.com", "1234!"));
        accessToken = tokenManager.createAccessToken(user.getId(), user.getEmail());

        teamA = teamRepository.save(new Team("Team A", "logoA"));
        teamB = teamRepository.save(new Team("Team B", "logoB"));
        teamC = teamRepository.save(new Team("Team C", "logoC"));
    }

    @AfterEach
    void tearDown() {
        databaseCleaner.clean();
    }

    @Test
    void 모든_경기를_최신순으로_조회한다() {
        // given
        LocalDateTime now = LocalDateTime.now();
        // Match 1 (Older)
        matchRepository.save(new Match(1L, "1", teamA.getId(), teamB.getId(), now.minusDays(2),
                MatchStatus.FINISHED, "Stadium 1", "naver1"));
        // Match 2 (Newer)
        matchRepository.save(new Match(1L, "1", teamB.getId(), teamC.getId(), now.minusDays(1),
                MatchStatus.FINISHED, "Stadium 2", "naver2"));

        // when
        MatchResponses response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/matches")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MatchResponses.class);

        // then
        assertAll(
                () -> assertThat(response.matchResponses()).hasSize(2),
                // Descending order verification (Newer first)
                () -> assertThat(response.matchResponses().get(0).homeTeam()).isEqualTo("Team B"),
                () -> assertThat(response.matchResponses().get(1).homeTeam()).isEqualTo("Team A")
        );
    }

    @Test
    void 특정_팀으로_필터링하여_경기를_조회한다() {
        // given
        LocalDateTime now = LocalDateTime.now();
        // Match with Team A and Team B
        matchRepository.save(new Match(1L, "1", teamA.getId(), teamB.getId(), now.minusDays(2),
                MatchStatus.FINISHED, "Stadium 1", "naver1"));
        // Match with Team B and Team C
        matchRepository.save(new Match(1L, "1", teamB.getId(), teamC.getId(), now.minusDays(1),
                MatchStatus.FINISHED, "Stadium 2", "naver2"));

        // when (Querying for Team A)
        MatchResponses response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("teamId", teamA.getId())
                .when()
                .get("/api/v1/matches")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MatchResponses.class);

        // then (Should only have the match with Team A)
        assertAll(
                () -> assertThat(response.matchResponses()).hasSize(1),
                () -> assertThat(response.matchResponses().get(0).homeTeam()).isEqualTo("Team A")
        );
    }

    @Test
    void 미래의_경기는_조회에서_제외된다() {
        // given
        LocalDateTime now = LocalDateTime.now();
        // 과거 경기
        matchRepository.save(new Match(1L, "1", teamA.getId(), teamB.getId(), now.minusDays(1),
                MatchStatus.FINISHED, "Stadium 1", "naver1"));
        // 미래 경기 (1일 후)
        matchRepository.save(new Match(1L, "1", teamB.getId(), teamC.getId(), now.plusDays(1),
                MatchStatus.SCHEDULED, "Stadium 2", "naver2"));

        // when
        MatchResponses response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/matches")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MatchResponses.class);

        // then
        assertAll(
                () -> assertThat(response.matchResponses()).hasSize(1),
                () -> assertThat(response.matchResponses().get(0).homeTeam()).isEqualTo("Team A")
        );
    }

    @Test
    void 경기의_리뷰_종료시간을_함께_조회한다() {
        // given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime finishedAt = now.minusHours(5);

        // Match 1: finished, finishedAt is set
        Match match1 = new Match(1L, "1", teamA.getId(), teamB.getId(), now.minusDays(2),
                MatchStatus.FINISHED, "Stadium 1", "naver1");
        match1.finish(finishedAt);
        matchRepository.save(match1);

        // Match 2: finishedAt is null
        Match match2 = new Match(1L, "1", teamB.getId(), teamC.getId(), now.minusDays(1),
                MatchStatus.FINISHED, "Stadium 2", "naver2");
        matchRepository.save(match2);

        // when
        MatchResponses response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/matches")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MatchResponses.class);

        // then
        assertAll(
                () -> assertThat(response.matchResponses()).hasSize(2),
                () -> assertThat(response.matchResponses().get(0).homeTeam()).isEqualTo("Team B"),
                () -> assertThat(response.matchResponses().get(0).reviewEndTime()).isNull(),
                () -> assertThat(response.matchResponses().get(1).homeTeam()).isEqualTo("Team A"),
                () -> assertThat(response.matchResponses().get(1).reviewEndTime()).isEqualTo(finishedAt.plusDays(2))
        );
    }
}

