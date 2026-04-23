package com.example.pitchboxd.admin.presnetation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.admin.dto.request.UpdateMatchRequest;
import com.example.pitchboxd.auth.application.TokenManager;
import com.example.pitchboxd.global.domain.ClockHolder;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import com.example.pitchboxd.match.core.infrastructure.MatchRepository;
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
class AdminControllerTest {

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
    void 매치_정보를_수정한다() {
        // given
        Team homeTeam = teamRepository.save(new Team("홈팀", "1"));
        Team awayTeam = teamRepository.save(new Team("어웨이팀", "2"));
        Match match = matchRepository.save(new Match(
                1L,
                "3R",
                homeTeam.getId(),
                awayTeam.getId(),
                LocalDateTime.now(),
                MatchStatus.FINISHED,
                "STADIUM",
                "matchNaverId"
        ));

        String newStadium = "NEW_STADIUM";
        String newNaverId = "newID";

        UpdateMatchRequest request = new UpdateMatchRequest(null, null, null, null, null, null, newStadium, null,
                newNaverId);
        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(request)
                .when()
                .patch("/api/v1/admin/matches/{matchId}", match.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value());

        Match updatedMatch = matchRepository.findById(match.getId()).orElseThrow();
        assertAll(
                () -> assertThat(updatedMatch.getLocation()).isEqualTo(newStadium),
                () -> assertThat(updatedMatch.getNaverId()).isEqualTo(newNaverId),
                () -> assertThat(updatedMatch.getHomeTeamId()).isEqualTo(homeTeam.getId()),
                () -> assertThat(updatedMatch.getAwayTeamId()).isEqualTo(awayTeam.getId())
        );
    }
}