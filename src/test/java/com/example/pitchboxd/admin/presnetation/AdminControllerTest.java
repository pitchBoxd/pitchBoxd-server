package com.example.pitchboxd.admin.presnetation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.admin.dto.request.UpdateMatchRequest;
import com.example.pitchboxd.admin.dto.request.UpdatePlayerRequest;
import com.example.pitchboxd.auth.application.TokenManager;
import com.example.pitchboxd.global.domain.ClockHolder;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import com.example.pitchboxd.match.core.infrastructure.MatchRepository;
import com.example.pitchboxd.match.matchStatistics.infrastructure.MatchStatisticsRepository;
import com.example.pitchboxd.player.domain.Player;
import com.example.pitchboxd.player.infrastructure.PlayerRepository;
import com.example.pitchboxd.support.DatabaseCleaner;
import com.example.pitchboxd.support.TestClockHolder;
import com.example.pitchboxd.team.domain.Team;
import com.example.pitchboxd.team.infrastructure.TeamRepository;
import com.example.pitchboxd.user.domain.User;
import com.example.pitchboxd.user.domain.UserRole;
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
    @Autowired
    private PlayerRepository playerRepository;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.clean();

        User admin = new User("관리자", "admin@example.com", "password123!");
        admin.assignRole(UserRole.ADMIN);
        admin = userRepository.save(admin);
        adminToken = tokenManager.createAccessToken(admin.getId(), admin.getEmail());

        User user = userRepository.save(new User("일반유저", "user@example.com", "password123!"));
        userToken = tokenManager.createAccessToken(user.getId(), user.getEmail());
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
                .header("Authorization", "Bearer " + adminToken)
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

    @Test
    void 선수_정보를_수정한다() {
        // given
        Player player = playerRepository.save(new Player(1L, "기존 이름", "oldNaverId"));

        Long newTeamId = 2L;
        String newName = "새 이름";
        String newNaverId = "newNaverId";
        UpdatePlayerRequest request = new UpdatePlayerRequest(newTeamId, newName, newNaverId);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + adminToken)
                .body(request)
                .when()
                .patch("/api/v1/admin/players/{playerId}", player.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value());

        Player updatedPlayer = playerRepository.findById(player.getId()).orElseThrow();
        assertAll(
                () -> assertThat(updatedPlayer.getTeamId()).isEqualTo(newTeamId),
                () -> assertThat(updatedPlayer.getName()).isEqualTo(newName),
                () -> assertThat(updatedPlayer.getNaverId()).isEqualTo(newNaverId)
        );
    }

    @Test
    void 일반_유저는_관리자_API에_접근할_수_없다() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body("{}")
                .when()
                .post("/api/v1/admin/sync-tasks/matches")
                .then().log().all()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void 모든_유저를_조회한다() {
        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/v1/admin/users")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("data", org.hamcrest.Matchers.hasSize(2));
    }

    @Test
    void 어드민_쿠키_인증을_통해_모든_유저를_조회한다() {
        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("accessToken", adminToken)
                .when()
                .get("/api/v1/admin/users")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("data", org.hamcrest.Matchers.hasSize(2));
    }
}