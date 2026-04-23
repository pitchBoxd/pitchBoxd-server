package com.example.pitchboxd.match.matchReview.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.auth.application.TokenManager;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import com.example.pitchboxd.match.core.infrastructure.MatchRepository;
import com.example.pitchboxd.match.matchReview.domain.MatchReview;
import com.example.pitchboxd.match.matchReview.dto.request.MatchReviewCreateRequest;
import com.example.pitchboxd.match.matchReview.dto.request.MatchReviewUpdateRequest;
import com.example.pitchboxd.match.matchReview.infrastructure.MatchReviewRepository;
import com.example.pitchboxd.match.matchStatistics.domain.MatchStatistics;
import com.example.pitchboxd.match.matchStatistics.infrastructure.MatchStatisticsRepository;
import com.example.pitchboxd.support.DatabaseCleaner;
import com.example.pitchboxd.user.domain.User;
import com.example.pitchboxd.user.infrastructure.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
class MatchReviewCommandControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private MatchStatisticsRepository matchStatisticsRepository;

    @Autowired
    private MatchReviewRepository matchReviewRepository;

    @Autowired
    private TokenManager tokenManager;

    private String accessToken;
    private Long matchId;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.clean();

        User user = userRepository.save(new User("nickname", "email@gmail.com", "1234!"));
        accessToken = tokenManager.createAccessToken(user.getId(), user.getEmail());

        Match match = new Match(1L, "1", 1L, 1L, LocalDateTime.now().minusHours(3), MatchStatus.FINISHED, "지구", "1");

        match.finish(LocalDateTime.now().minusHours(1));
        Match savedMatch = matchRepository.save(match);

        matchId = savedMatch.getId();

        MatchStatistics matchStatistics = new MatchStatistics(matchId);
        matchStatisticsRepository.save(matchStatistics);
    }

    @AfterEach
    void tearDown() {
        databaseCleaner.clean();
    }

    @Test
    void 경기_리뷰를_생성한다() {
        // given
        MatchReviewCreateRequest request = new MatchReviewCreateRequest("정말 재미있는 경기였습니다!", 5);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(request)
                .when().post("/api/v1/matches/{matchId}/match-reviews", matchId)
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }

    @Test
    void 경기_리뷰에_좋아요를_토글한다() {
        // given
        MatchReviewCreateRequest request = new MatchReviewCreateRequest("좋은 경기!", 5);
        Long matchReviewId = RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(request)
                .when().post("/api/v1/matches/{matchId}/match-reviews", matchId)
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract().jsonPath().getLong("data.id");

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when().post("/api/v1/match-reviews/{matchReviewId}/likes", matchReviewId)
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    void 경기_리뷰에_좋아요를_5명이_동시에_토글한다() throws InterruptedException {
        // given
        MatchReviewCreateRequest request = new MatchReviewCreateRequest("동시성 테스트 리뷰", 5);
        Long matchReviewId = RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(request)
                .when().post("/api/v1/matches/{matchId}/match-reviews", matchId)
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract().jsonPath().getLong("data.id");

        int threadCount = 5;
        String[] tokens = new String[threadCount];
        for (int i = 0; i < threadCount; i++) {
            User user = userRepository.save(new User("user" + i, "user" + i + "@gmail.com", "password!"));
            tokens[i] = tokenManager.createAccessToken(user.getId(), user.getEmail());
        }

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            final String token = tokens[i];
            executorService.submit(() -> {
                try {
                    RestAssured.given()
                            .header("Authorization", "Bearer " + token)
                            .when().post("/api/v1/match-reviews/{matchReviewId}/likes", matchReviewId)
                            .then()
                            .statusCode(HttpStatus.OK.value());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        MatchReview matchReview = matchReviewRepository.findById(matchReviewId).orElseThrow();
        assertThat(matchReview.getLikeCount()).isEqualTo(5);
    }

    @Test
    void 경기_리뷰를_수정한다() {
        // given
        MatchReviewCreateRequest createRequest = new MatchReviewCreateRequest("수정 전 리뷰", 5);
        Long matchReviewId = RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(createRequest)
                .when().post("/api/v1/matches/{matchId}/match-reviews", matchId)
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract().jsonPath().getLong("data.id");

        MatchReviewUpdateRequest updateRequest = new MatchReviewUpdateRequest("수정 후 리뷰", 4);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(updateRequest)
                .when().patch("/api/v1/match-reviews/{matchReviewId}", matchReviewId)
                .then().log().all()
                .statusCode(HttpStatus.OK.value());

        // then
        MatchReview matchReview = matchReviewRepository.findById(matchReviewId).orElseThrow();
        assertAll(
                () -> assertThat(matchReview.getContent()).isEqualTo("수정 후 리뷰"),
                () -> assertThat(matchReview.getPoint()).isEqualTo(4)
        );
    }

    @Test
    void 경기_리뷰를_삭제한다() {
        // given
        MatchReviewCreateRequest createRequest = new MatchReviewCreateRequest("삭제할 리뷰", 5);
        Long matchReviewId = RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(createRequest)
                .when().post("/api/v1/matches/{matchId}/match-reviews", matchId)
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract().jsonPath().getLong("data.id");

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when().delete("/api/v1/match-reviews/{matchReviewId}", matchReviewId)
                .then().log().all()
                .statusCode(HttpStatus.OK.value());

        // then
        assertThat(matchReviewRepository.findById(matchReviewId)).isEmpty();
    }
}
