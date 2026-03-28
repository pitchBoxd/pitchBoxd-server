package com.example.pitchboxd.matchReview.presentation;

import com.example.pitchboxd.global.security.JwtProvider;
import com.example.pitchboxd.match.domain.Match;
import com.example.pitchboxd.match.domain.MatchResult;
import com.example.pitchboxd.match.domain.MatchStatus;
import com.example.pitchboxd.match.dto.request.MatchReviewCreateRequest;
import com.example.pitchboxd.match.infrastructure.MatchRepository;
import com.example.pitchboxd.matchStatistics.domain.MatchStatistics;
import com.example.pitchboxd.matchStatistics.infrastructure.MatchStatisticsRepository;
import com.example.pitchboxd.support.DatabaseCleaner;
import com.example.pitchboxd.user.domain.User;
import com.example.pitchboxd.user.infrastructure.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private JwtProvider jwtProvider;

    private String accessToken;
    private Long matchId;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.clean();

        User user = userRepository.save(new User("nickname", "email@gmail.com", "1234!"));
        accessToken = jwtProvider.createToken(user.getId(), user.getEmail());

        Match match = new Match(1L, 1, 1L, 1L, LocalDateTime.now().minusHours(3), MatchStatus.FINISHED, "지구",
                new MatchResult(0, 0, new ArrayList<>(), new ArrayList<>()));

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
}
