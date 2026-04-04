package com.example.pitchboxd.match.playerReview.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.global.security.JwtProvider;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import com.example.pitchboxd.match.core.infrastructure.MatchRepository;
import com.example.pitchboxd.match.lineup.domain.MatchLineup;
import com.example.pitchboxd.match.lineup.domain.ParticipationStatus;
import com.example.pitchboxd.match.lineup.infrastructure.MatchLineupRepository;
import com.example.pitchboxd.match.matchReview.dto.response.LikeToggleResponse;
import com.example.pitchboxd.match.playerReview.dto.request.PlayerReviewCreateRequest;
import com.example.pitchboxd.match.playerReview.dto.request.PlayerReviewUpdateRequest;
import com.example.pitchboxd.match.playerStatistics.domain.PlayerMatchStatistics;
import com.example.pitchboxd.match.playerStatistics.infrastructure.PlayerMatchStatisticsRepository;
import com.example.pitchboxd.player.domain.Player;
import com.example.pitchboxd.player.infrastructure.PlayerRepository;
import com.example.pitchboxd.support.DatabaseCleaner;
import com.example.pitchboxd.support.TestClockHolder;
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
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PlayerReviewCommandControllerTest {

    private final Long homeTeamId = 1L;
    private final Long awayTeamId = 2L;

    @Autowired
    private PlayerRepository playerRepository;

    @LocalServerPort
    private int port;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private MatchLineupRepository matchLineupRepository;

    @Autowired
    private PlayerMatchStatisticsRepository playerMatchStatisticsRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private TestClockHolder clockHolder;

    private Long matchId;
    private Long playerId;
    private LocalDateTime now;
    private String accessToken;
    private User user;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.clean();
        now = LocalDateTime.now();

        Match match = new Match(1L, "1", homeTeamId, awayTeamId, now.minusHours(3), MatchStatus.FINISHED, "지구");
        match.finish(now);
        Match savedMatch = matchRepository.save(match);
        matchId = savedMatch.getId();

        Player savedPlayer = playerRepository.save(new Player(1L, "기성용"));
        playerId = savedPlayer.getId();

        MatchLineup matchLineup = new MatchLineup(matchId, playerId, 6, ParticipationStatus.STARTER);
        matchLineupRepository.save(matchLineup);

        PlayerMatchStatistics playerMatchStatistics = new PlayerMatchStatistics(playerId, matchId);
        playerMatchStatisticsRepository.save(playerMatchStatistics);

        user = userRepository.save(new User("nickname", "email @gmail.com", "abcd1234!", homeTeamId));
        accessToken = jwtProvider.createToken(user.getId(), user.getEmail());
    }

    @AfterEach
    void tearDown() {
        databaseCleaner.clean();
        clockHolder.setTime(now);
    }

    @Test
    void 경기_선수_리뷰를_성공적으로_생성한다() {
        // given
        clockHolder.plusHours(1);
        PlayerReviewCreateRequest request = new PlayerReviewCreateRequest(playerId, "기성용의 대지를 가르는 패스! 경기를 바꿨다.", 10);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(request)
                .when()
                .post("/api/v1/matches/{matchId}/player-reviews", matchId)
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }

    @Test
    void 선수_리뷰_좋아요를_토글한다() {
        // given
        clockHolder.plusHours(1);
        PlayerReviewCreateRequest request = new PlayerReviewCreateRequest(playerId, "정말 멋진 활약이었습니다.", 10);

        Long playerReviewId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(request)
                .when()
                .post("/api/v1/matches/{matchId}/player-reviews", matchId)
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .jsonPath()
                .getLong("data.id");

        // when & then
        LikeToggleResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .post("/api/v1/player-reviews/{playerReviewId}/likes", playerReviewId)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", LikeToggleResponse.class);

        assertAll(
                () -> assertThat(response.isLiked()).isTrue(),
                () -> assertThat(response.totalLikeCount()).isEqualTo(1)
        );
    }

    @Test
    void 선수_리뷰를_성공적으로_수정한다() {
        // given
        clockHolder.plusHours(1);
        PlayerReviewCreateRequest createRequest = new PlayerReviewCreateRequest(playerId, "기존 리뷰 내용입니다.", 10);

        Long playerReviewId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(createRequest)
                .when()
                .post("/api/v1/matches/{matchId}/player-reviews", matchId)
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .jsonPath()
                .getLong("data.id");

        PlayerReviewUpdateRequest updateRequest = new PlayerReviewUpdateRequest("수정된 리뷰 내용입니다.", 9);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(updateRequest)
                .when()
                .patch("/api/v1/player-reviews/{playerReviewId}", playerReviewId)
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    void 선수_리뷰를_성공적으로_삭제한다() {
        // given
        clockHolder.plusHours(1);
        PlayerReviewCreateRequest createRequest = new PlayerReviewCreateRequest(playerId, "삭제될 리뷰 내용입니다.", 10);

        Long playerReviewId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(createRequest)
                .when()
                .post("/api/v1/matches/{matchId}/player-reviews", matchId)
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .jsonPath()
                .getLong("data.id");

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .delete("/api/v1/player-reviews/{playerReviewId}", playerReviewId)
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }
}
