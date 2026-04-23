package com.example.pitchboxd.match.matchReview.presentation;

import static org.hamcrest.Matchers.hasSize;

import com.example.pitchboxd.auth.application.TokenManager;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import com.example.pitchboxd.match.core.infrastructure.MatchRepository;
import com.example.pitchboxd.match.matchReview.domain.MatchReview;
import com.example.pitchboxd.match.matchReview.infrastructure.MatchReviewRepository;
import com.example.pitchboxd.match.matchStatistics.domain.FanType;
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
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(ReplaceUnderscores.class)
class MatchReviewQueryControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchReviewRepository matchReviewRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private TestClockHolder clockHolder;

    @Autowired
    private TokenManager tokenManager;

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
    void 인기_리뷰_목록을_조회한다() {
        // given
        LocalDateTime now = LocalDateTime.now();
        clockHolder.setTime(now);

        Team homeTeam = teamRepository.save(new Team("홈팀1", "1234"));
        Team awayTeam = teamRepository.save(new Team("원정팀1", "12354"));

        // 1. 리뷰 가능한 경기 3개 생성
        for (int i = 0; i < 3; i++) {
            Match recentMatch = new Match(1L, "2", homeTeam.getId(), awayTeam.getId(), now.minusHours(5 + i),
                    MatchStatus.FINISHED, "상암", "kk" + i);
            recentMatch.finish(now.minusHours(3 + i));
            Match savedMatch = matchRepository.save(recentMatch);

            // 각 경기마다 리뷰 2개씩 생성
            for (int j = 0; j < 2; j++) {
                User author = userRepository.save(
                        new User("작성자" + i + j, "author" + i + j + "@example.com", "pw", homeTeam.getId()));
                MatchReview review = matchReviewRepository.save(
                        new MatchReview(savedMatch.getId(), author.getId(), 5, "리뷰" + i + j, FanType.HOME));
                review.addOneLikeCount();
                matchReviewRepository.save(review);
            }
        }

        // 2. 48시간보다 이전에 종료된 경기 1개 생성 (리뷰 불가능)
        Match oldMatch = new Match(1L, "2", homeTeam.getId(), awayTeam.getId(), now.minusDays(5),
                MatchStatus.FINISHED, "상암", "old-match");
        oldMatch.finish(now.minusDays(4));
        Match savedOldMatch = matchRepository.save(oldMatch);

        // 이전 경기에 리뷰 3개 생성
        for (int k = 0; k < 3; k++) {
            User author = userRepository.save(
                    new User("과거작성자" + k, "old" + k + "@example.com", "pw", homeTeam.getId()));
            MatchReview oldReview = matchReviewRepository.save(
                    new MatchReview(savedOldMatch.getId(), author.getId(), 5, "과거리뷰" + k, FanType.HOME));
            oldReview.addOneLikeCount();
            matchReviewRepository.save(oldReview);
        }

        int size = 10;

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("size", size)
                .when()
                .get("/api/v1/match-reviews/hot")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("data.responses", hasSize(6));
    }
}
