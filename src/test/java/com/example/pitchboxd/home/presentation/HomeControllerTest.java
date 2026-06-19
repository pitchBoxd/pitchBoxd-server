package com.example.pitchboxd.home.presentation;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.pitchboxd.auth.application.TokenManager;
import com.example.pitchboxd.home.dto.response.HomeResponses;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchResult;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import com.example.pitchboxd.match.core.infrastructure.MatchRepository;
import com.example.pitchboxd.match.matchReview.domain.MatchReview;
import com.example.pitchboxd.match.matchReview.infrastructure.MatchReviewRepository;
import com.example.pitchboxd.match.matchStatistics.domain.FanType;
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
class HomeControllerTest {

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
    private MatchReviewRepository matchReviewRepository;

    @Autowired
    private MatchStatisticsRepository matchStatisticsRepository;

    @Autowired
    private TestClockHolder clockHolder;

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
    void 홈_화면_데이터를_조회한다() {
        // given
        LocalDateTime now = LocalDateTime.now();
        clockHolder.setTime(now);

        Team homeTeam = teamRepository.save(new Team("홈팀1", "naver1"));
        Team awayTeam = teamRepository.save(new Team("원정팀1", "naver2"));

        // 리뷰 가능한 경기 2개 생성
        for (int i = 0; i < 2; i++) {
            Match recentMatch = new Match(1L, "2", homeTeam.getId(), awayTeam.getId(), now.minusHours(5 + i),
                    MatchStatus.FINISHED, "상암", "match" + i);
            recentMatch.finish(now.minusHours(3 + i));
            recentMatch.decideMatchResult(new MatchResult(2, 1, List.of(), List.of()));
            Match savedMatch = matchRepository.save(recentMatch);
            matchStatisticsRepository.save(new MatchStatistics(savedMatch.getId()));

            // 각 경기마다 리뷰 4개 생성 (응답에는 경기당 최대 3개씩 포함되어야 함)
            for (int j = 0; j < 4; j++) {
                User author = userRepository.save(
                        new User("작성자" + i + j, "author" + i + j + "@example.com", "pw", homeTeam.getId()));
                MatchReview review = new MatchReview(savedMatch.getId(), author.getId(), 5, "리뷰" + i + j,
                        FanType.HOME);
                for (int l = 0; l <= j; l++) {
                    review.addOneLikeCount();
                }
                matchReviewRepository.save(review);
            }
        }

        // 리뷰 불가능한 경기 생성 (48시간 이전 종료)
        Match oldMatch = new Match(1L, "1", homeTeam.getId(), awayTeam.getId(), now.minusDays(5),
                MatchStatus.FINISHED, "상암", "oldMatch");
        oldMatch.finish(now.minusDays(4));
        oldMatch.decideMatchResult(new MatchResult(1, 1, List.of(), List.of()));
        matchRepository.save(oldMatch);
        matchStatisticsRepository.save(new MatchStatistics(oldMatch.getId()));

        // when & then
        HomeResponses response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("state", "REVIEWABLE")
                .when()
                .get("/api/v1/home")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", HomeResponses.class);

        assertThat(response.responses()).hasSize(2);
        assertThat(response.responses().get(0).hotReviews()).hasSize(3);
        assertThat(response.responses().get(1).hotReviews()).hasSize(3);
        assertThat(response.responses().get(0).matchResponse().reviewEndTime()).isEqualTo(now.minusHours(4).plusDays(2));
        assertThat(response.responses().get(1).matchResponse().reviewEndTime()).isEqualTo(now.minusHours(3).plusDays(2));
    }
}


