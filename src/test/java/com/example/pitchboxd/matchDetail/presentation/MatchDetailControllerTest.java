package com.example.pitchboxd.matchDetail.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.auth.application.TokenManager;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchResult;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import com.example.pitchboxd.match.core.infrastructure.MatchRepository;
import com.example.pitchboxd.match.lineup.domain.MatchLineup;
import com.example.pitchboxd.match.lineup.domain.ParticipationStatus;
import com.example.pitchboxd.match.lineup.infrastructure.MatchLineupRepository;
import com.example.pitchboxd.match.matchReview.domain.MatchReview;
import com.example.pitchboxd.match.matchReview.domain.MatchReviewLike;
import com.example.pitchboxd.match.matchReview.infrastructure.MatchReviewLikeRepository;
import com.example.pitchboxd.match.matchReview.infrastructure.MatchReviewRepository;
import com.example.pitchboxd.match.matchStatistics.domain.FanType;
import com.example.pitchboxd.matchDetail.dto.response.MatchDetailMatchReviewResponses;
import com.example.pitchboxd.matchDetail.dto.response.MatchDetailResponse;
import com.example.pitchboxd.player.domain.Player;
import com.example.pitchboxd.player.infrastructure.PlayerRepository;
import com.example.pitchboxd.season.domain.Season;
import com.example.pitchboxd.season.infrastructure.SeasonRepository;
import com.example.pitchboxd.support.DatabaseCleaner;
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
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MatchDetailControllerTest {

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
    private SeasonRepository seasonRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MatchLineupRepository matchLineupRepository;

    @Autowired
    private MatchReviewRepository matchReviewRepository;

    @Autowired
    private MatchReviewLikeRepository matchReviewLikeRepository;

    private String accessToken;
    private User loginUser;
    private Match match;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.clean();

        loginUser = userRepository.save(new User("테스트유저", "test@example.com", "password123!"));
        accessToken = tokenManager.createAccessToken(loginUser.getId(), loginUser.getEmail());

        Season season = seasonRepository.save(new Season("2026 K리그1"));
        Team homeTeam = teamRepository.save(new Team("홈팀", "naver1"));
        Team awayTeam = teamRepository.save(new Team("원정팀", "naver2"));

        match = new Match(season.getId(), "1R", homeTeam.getId(), awayTeam.getId(),
                LocalDateTime.of(2026, 4, 28, 19, 0), MatchStatus.FINISHED, "상암", "match-1");
        match.decideMatchResult(new MatchResult(2, 1, List.of(), List.of()));
        matchRepository.save(match);

        Player homePlayer = playerRepository.save(new Player(homeTeam.getId(), "홈선수", "p1"));
        Player awayPlayer = playerRepository.save(new Player(awayTeam.getId(), "원정선수", "p2"));

        matchLineupRepository.save(new MatchLineup(match.getId(), homePlayer.getId(), 7, ParticipationStatus.STARTER));
        matchLineupRepository.save(new MatchLineup(match.getId(), awayPlayer.getId(), 9, ParticipationStatus.STARTER));
    }

    @AfterEach
    void tearDown() {
        databaseCleaner.clean();
    }

    @Test
    void 경기_상세_정적_데이터를_조회한다() {
        // when
        MatchDetailResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/matches/{matchId}/detail/static", match.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MatchDetailResponse.class);

        // then
        assertAll(
                () -> assertThat(response.season()).isEqualTo("2026 K리그1"),
                () -> assertThat(response.homeTeam()).isEqualTo("홈팀"),
                () -> assertThat(response.awayTeam()).isEqualTo("원정팀"),
                () -> assertThat(response.homeScore()).isEqualTo(2),
                () -> assertThat(response.awayScore()).isEqualTo(1),
                () -> assertThat(response.homeLineups().responses()).hasSize(1),
                () -> assertThat(response.awayLineups().responses()).hasSize(1)
        );
    }

    @Test
    void 경기_상세_핫한_리뷰_데이터를_조회한다() {
        // given
        User author = userRepository.save(new User("작성자", "author@test.com", "pass"));
        MatchReview hotReview = new MatchReview(match.getId(), author.getId(), 10, "대박 경기", FanType.HOME);
        hotReview.addOneLikeCount();
        matchReviewRepository.save(hotReview);
        matchReviewLikeRepository.save(new MatchReviewLike(hotReview.getId(), loginUser.getId()));

        // when
        MatchDetailMatchReviewResponses response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("limit", 5)
                .when()
                .get("/api/v1/matches/{matchId}/match-reviews/hot", match.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MatchDetailMatchReviewResponses.class);

        // then
        assertAll(
                () -> assertThat(response.responses()).hasSize(1),
                () -> assertThat(response.responses().get(0).reviewId()).isEqualTo(hotReview.getId()),
                () -> assertThat(response.responses().get(0).isLiked()).isTrue(),
                () -> assertThat(response.responses().get(0).likeCount()).isEqualTo(1)
        );
    }

    @Test
    void 비로그인_유저도_경기_상세_핫한_리뷰_데이터를_조회한다() {
        // given
        User author = userRepository.save(new User("작성자", "author@test.com", "pass"));
        MatchReview hotReview = new MatchReview(match.getId(), author.getId(), 10, "대박 경기", FanType.HOME);
        hotReview.addOneLikeCount();
        matchReviewRepository.save(hotReview);

        // when
        MatchDetailMatchReviewResponses response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("limit", 5)
                .when()
                .get("/api/v1/matches/{matchId}/match-reviews/hot", match.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MatchDetailMatchReviewResponses.class);

        // then
        assertAll(
                () -> assertThat(response.responses()).hasSize(1),
                () -> assertThat(response.responses().get(0).reviewId()).isEqualTo(hotReview.getId()),
                () -> assertThat(response.responses().get(0).isLiked()).isFalse()
        );
    }
}
