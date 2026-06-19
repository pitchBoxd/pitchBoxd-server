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
import com.example.pitchboxd.match.playerReview.domain.PlayerReview;
import com.example.pitchboxd.match.playerReview.infrastructure.PlayerReviewRepository;
import com.example.pitchboxd.match.playerStatistics.domain.PlayerStatistics;
import com.example.pitchboxd.match.playerStatistics.infrastructure.PlayerStatisticsRepository;
import com.example.pitchboxd.matchDetail.dto.response.MatchDetailMatchReviewResponses;
import com.example.pitchboxd.matchDetail.dto.response.MatchDetailPersonalResponse;
import com.example.pitchboxd.matchDetail.dto.response.MatchDetailResultResponse;
import com.example.pitchboxd.matchDetail.dto.response.MatchDetailStatsResponse;
import com.example.pitchboxd.matchDetail.dto.response.MatchReviewSliceResponse;
import com.example.pitchboxd.matchDetail.dto.response.MatchReviewDetailResponse;
import com.example.pitchboxd.matchDetail.dto.response.PlayerReviewSliceResponse;
import com.example.pitchboxd.matchDetail.dto.response.PlayerReviewDetailResponse;
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

    @Autowired
    private PlayerReviewRepository playerReviewRepository;

    @Autowired
    private PlayerStatisticsRepository playerStatisticsRepository;

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
    void 경기_상세_결과와_통계_데이터를_조회한다() {
        // when - 결과 데이터 조회
        MatchDetailResultResponse resultResponse = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/matches/{matchId}/detail/result", match.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MatchDetailResultResponse.class);

        // when - 통계 데이터 조회
        MatchDetailStatsResponse statsResponse = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/matches/{matchId}/detail/stats", match.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MatchDetailStatsResponse.class);

        // then
        assertAll(
                () -> assertThat(resultResponse.seasonName()).isEqualTo("2026 K리그1"),
                () -> assertThat(resultResponse.homeTeamName()).isEqualTo("홈팀"),
                () -> assertThat(resultResponse.awayTeamName()).isEqualTo("원정팀"),
                () -> assertThat(resultResponse.homeScore()).isEqualTo(2),
                () -> assertThat(resultResponse.awayScore()).isEqualTo(1),
                () -> assertThat(resultResponse.homeLineups().responses()).hasSize(1),
                () -> assertThat(resultResponse.awayLineups().responses()).hasSize(1),
                
                () -> assertThat(statsResponse.totalAverage()).isEqualTo(0.0),
                () -> assertThat(statsResponse.homeAverage()).isEqualTo(0.0),
                () -> assertThat(statsResponse.awayAverage()).isEqualTo(0.0),
                () -> assertThat(statsResponse.highlights().mom()).isNull(),
                () -> assertThat(statsResponse.highlights().top3()).isEmpty()
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

    @Test
    void 로그인_유저가_경기와_선수를_모두_평가한_경우_개인_평가_데이터를_조회한다() {
        // given
        MatchReview myMatchReview = matchReviewRepository.save(new MatchReview(match.getId(), loginUser.getId(), 8, "좋은 경기였습니다.", FanType.HOME));
        
        Player homePlayer = playerRepository.findAll().get(0);
        PlayerReview myPlayerReview = playerReviewRepository.save(new PlayerReview(match.getId(), homePlayer.getId(), loginUser.getId(), 9, "오늘 활약이 대단했습니다."));

        // when
        MatchDetailPersonalResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/matches/{matchId}/detail/personal", match.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MatchDetailPersonalResponse.class);

        // then
        assertAll(
                () -> assertThat(response.isEvaluated()).isTrue(),
                () -> assertThat(response.myMatchReview().reviewId()).isEqualTo(myMatchReview.getId()),
                () -> assertThat(response.myMatchReview().rating()).isEqualTo(8),
                () -> assertThat(response.myMatchReview().comment()).isEqualTo("좋은 경기였습니다."),
                () -> assertThat(response.myMatchReview().likeCount()).isEqualTo(0L),
                () -> assertThat(response.myPlayerReviews()).hasSize(1),
                () -> assertThat(response.myPlayerReviews().get(0).playerReviewId()).isEqualTo(myPlayerReview.getId()),
                () -> assertThat(response.myPlayerReviews().get(0).playerId()).isEqualTo(homePlayer.getId()),
                () -> assertThat(response.myPlayerReviews().get(0).rating()).isEqualTo(9),
                () -> assertThat(response.myPlayerReviews().get(0).comment()).isEqualTo("오늘 활약이 대단했습니다."),
                () -> assertThat(response.myPlayerReviews().get(0).likeCount()).isEqualTo(0L)
        );
    }

    @Test
    void 로그인_유저가_평가하지_않은_경우_평가하지_않음_데이터를_반환한다() {
        // when
        MatchDetailPersonalResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/matches/{matchId}/detail/personal", match.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MatchDetailPersonalResponse.class);

        // then
        assertAll(
                () -> assertThat(response.isEvaluated()).isFalse(),
                () -> assertThat(response.myMatchReview()).isNull(),
                () -> assertThat(response.myPlayerReviews()).isEmpty()
        );
    }

    @Test
    void 비로그인_유저인_경우_평가하지_않음_데이터를_반환한다() {
        // when
        MatchDetailPersonalResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/matches/{matchId}/detail/personal", match.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MatchDetailPersonalResponse.class);

        // then
        assertAll(
                () -> assertThat(response.isEvaluated()).isFalse(),
                () -> assertThat(response.myMatchReview()).isNull(),
                () -> assertThat(response.myPlayerReviews()).isEmpty()
        );
    }

    @Test
    void 로그인_유저가_선수만_평가하고_경기는_평가하지_않은_경우_선수_평가_데이터만_반환한다() {
        // given
        Player homePlayer = playerRepository.findAll().get(0);
        PlayerReview myPlayerReview = playerReviewRepository.save(new PlayerReview(match.getId(), homePlayer.getId(), loginUser.getId(), 9, "오늘 활약이 대단했습니다."));

        // when
        MatchDetailPersonalResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/matches/{matchId}/detail/personal", match.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MatchDetailPersonalResponse.class);

        // then
        assertAll(
                () -> assertThat(response.isEvaluated()).isTrue(),
                // Match review is null
                () -> assertThat(response.myMatchReview()).isNull(),
                // Player reviews lists our player review
                () -> assertThat(response.myPlayerReviews()).hasSize(1),
                () -> assertThat(response.myPlayerReviews().get(0).playerReviewId()).isEqualTo(myPlayerReview.getId()),
                () -> assertThat(response.myPlayerReviews().get(0).playerId()).isEqualTo(homePlayer.getId()),
                () -> assertThat(response.myPlayerReviews().get(0).rating()).isEqualTo(9),
                () -> assertThat(response.myPlayerReviews().get(0).comment()).isEqualTo("오늘 활약이 대단했습니다.")
        );
    }

    @Test
    void 경기_리뷰를_최신순으로_커서_페이징_조회한다() {
        // given
        User author = userRepository.save(new User("작성자1", "author1@test.com", "pass"));
        MatchReview review1 = matchReviewRepository.save(new MatchReview(match.getId(), author.getId(), 5, "첫번째 리뷰", FanType.NEUTRAL));
        MatchReview review2 = matchReviewRepository.save(new MatchReview(match.getId(), author.getId(), 6, "두번째 리뷰", FanType.NEUTRAL));
        MatchReview review3 = matchReviewRepository.save(new MatchReview(match.getId(), author.getId(), 7, "세번째 리뷰", FanType.NEUTRAL));

        // when - first page
        MatchReviewSliceResponse response1 = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("sort", "LATEST")
                .queryParam("size", 2)
                .when()
                .get("/api/v1/matches/{matchId}/match-reviews", match.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MatchReviewSliceResponse.class);

        // then - first page
        assertAll(
                () -> assertThat(response1.reviews()).hasSize(2),
                () -> assertThat(response1.reviews().get(0).reviewId()).isEqualTo(review3.getId()),
                () -> assertThat(response1.reviews().get(1).reviewId()).isEqualTo(review2.getId()),
                () -> assertThat(response1.hasNext()).isTrue(),
                () -> assertThat(response1.nextCursorId()).isEqualTo(review2.getId()),
                () -> assertThat(response1.nextCursorLikeCount()).isEqualTo(review2.getLikeCount())
        );

        // when - second page using cursor
        MatchReviewSliceResponse response2 = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("sort", "LATEST")
                .queryParam("size", 2)
                .queryParam("cursorId", response1.nextCursorId())
                .when()
                .get("/api/v1/matches/{matchId}/match-reviews", match.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MatchReviewSliceResponse.class);

        // then - second page
        assertAll(
                () -> assertThat(response2.reviews()).hasSize(1),
                () -> assertThat(response2.reviews().get(0).reviewId()).isEqualTo(review1.getId()),
                () -> assertThat(response2.hasNext()).isFalse(),
                () -> assertThat(response2.nextCursorId()).isNull(),
                () -> assertThat(response2.nextCursorLikeCount()).isNull()
        );
    }

    @Test
    void 경기_리뷰를_추천순으로_커서_페이징_조회한다() {
        // given
        User author = userRepository.save(new User("작성자1", "author1@test.com", "pass"));
        MatchReview review1 = new MatchReview(match.getId(), author.getId(), 5, "리뷰1", FanType.NEUTRAL);
        review1.addOneLikeCount(); // likeCount = 1
        matchReviewRepository.save(review1);

        MatchReview review2 = new MatchReview(match.getId(), author.getId(), 6, "리뷰2", FanType.NEUTRAL);
        review2.addOneLikeCount();
        review2.addOneLikeCount(); // likeCount = 2
        matchReviewRepository.save(review2);

        MatchReview review3 = new MatchReview(match.getId(), author.getId(), 7, "리뷰3", FanType.NEUTRAL);
        review3.addOneLikeCount();
        review3.addOneLikeCount(); // likeCount = 2
        matchReviewRepository.save(review3);

        // when - first page
        MatchReviewSliceResponse response1 = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("sort", "LIKE")
                .queryParam("size", 2)
                .when()
                .get("/api/v1/matches/{matchId}/match-reviews", match.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MatchReviewSliceResponse.class);

        // then - first page
        assertAll(
                () -> assertThat(response1.reviews()).hasSize(2),
                () -> assertThat(response1.reviews().get(0).reviewId()).isEqualTo(review3.getId()),
                () -> assertThat(response1.reviews().get(1).reviewId()).isEqualTo(review2.getId()),
                () -> assertThat(response1.hasNext()).isTrue(),
                () -> assertThat(response1.nextCursorId()).isEqualTo(review2.getId()),
                () -> assertThat(response1.nextCursorLikeCount()).isEqualTo(2L)
        );

        // when - second page using cursor
        MatchReviewSliceResponse response2 = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("sort", "LIKE")
                .queryParam("size", 2)
                .queryParam("cursorId", response1.nextCursorId())
                .queryParam("cursorLikeCount", response1.nextCursorLikeCount())
                .when()
                .get("/api/v1/matches/{matchId}/match-reviews", match.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MatchReviewSliceResponse.class);

        // then - second page
        assertAll(
                () -> assertThat(response2.reviews()).hasSize(1),
                () -> assertThat(response2.reviews().get(0).reviewId()).isEqualTo(review1.getId()),
                () -> assertThat(response2.hasNext()).isFalse(),
                () -> assertThat(response2.nextCursorId()).isNull(),
                () -> assertThat(response2.nextCursorLikeCount()).isNull()
        );
    }

    @Test
    void 로그인_유저가_자신이_남긴_리뷰와_좋아요를_누른_리뷰가_포함된_목록을_페이징_조회한다() {
        // given
        User otherUser = userRepository.save(new User("다른유저", "other@test.com", "pass"));
        MatchReview myReview = matchReviewRepository.save(new MatchReview(match.getId(), loginUser.getId(), 8, "내 리뷰", FanType.HOME));
        MatchReview otherReview = matchReviewRepository.save(new MatchReview(match.getId(), otherUser.getId(), 7, "다른 유저 리뷰", FanType.AWAY));
        
        // 내 리뷰에는 좋아요를 안누르고, 다른 유저 리뷰에 내가 좋아요를 누름
        matchReviewLikeRepository.save(new MatchReviewLike(otherReview.getId(), loginUser.getId()));
        otherReview.addOneLikeCount();
        matchReviewRepository.save(otherReview);

        // when
        MatchReviewSliceResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("sort", "LATEST")
                .queryParam("size", 10)
                .when()
                .get("/api/v1/matches/{matchId}/match-reviews", match.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MatchReviewSliceResponse.class);

        // then
        assertThat(response.reviews()).hasSize(2);
        
        MatchReviewDetailResponse first = response.reviews().get(0);
        assertThat(first.reviewId()).isEqualTo(otherReview.getId());
        assertThat(first.isLiked()).isTrue();
        assertThat(first.isOwner()).isFalse();

        MatchReviewDetailResponse second = response.reviews().get(1);
        assertThat(second.reviewId()).isEqualTo(myReview.getId());
        assertThat(second.isLiked()).isFalse();
        assertThat(second.isOwner()).isTrue();
    }

    @Test
    void 선수_리뷰를_최신순으로_커서_페이징_조회한다() {
        // given
        Player homePlayer = playerRepository.findAll().get(0);
        User author1 = userRepository.save(new User("작성자1", "author1@test.com", "pass"));
        User author2 = userRepository.save(new User("작성자2", "author2@test.com", "pass"));

        PlayerReview review1 = playerReviewRepository.save(new PlayerReview(match.getId(), homePlayer.getId(), author1.getId(), 5, "선수 리뷰1"));
        PlayerReview review2 = playerReviewRepository.save(new PlayerReview(match.getId(), homePlayer.getId(), author2.getId(), 6, "선수 리뷰2"));
        PlayerReview review3 = playerReviewRepository.save(new PlayerReview(match.getId(), homePlayer.getId(), author1.getId(), 7, "선수 리뷰3"));

        // when - 첫 번째 페이지 조회
        PlayerReviewSliceResponse response1 = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("sort", "LATEST")
                .queryParam("size", 2)
                .when()
                .get("/api/v1/matches/{matchId}/players/{playerId}/player-reviews", match.getId(), homePlayer.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", PlayerReviewSliceResponse.class);

        // then - 첫 번째 페이지
        assertAll(
                () -> assertThat(response1.reviews()).hasSize(2),
                () -> assertThat(response1.reviews().get(0).id()).isEqualTo(review3.getId()),
                () -> assertThat(response1.reviews().get(1).id()).isEqualTo(review2.getId()),
                () -> assertThat(response1.hasNext()).isTrue(),
                () -> assertThat(response1.nextCursorId()).isEqualTo(review2.getId()),
                () -> assertThat(response1.nextCursorLikeCount()).isEqualTo(0L)
        );

        // when - 두 번째 페이지 조회
        PlayerReviewSliceResponse response2 = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("sort", "LATEST")
                .queryParam("size", 2)
                .queryParam("cursorId", response1.nextCursorId())
                .when()
                .get("/api/v1/matches/{matchId}/players/{playerId}/player-reviews", match.getId(), homePlayer.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", PlayerReviewSliceResponse.class);

        // then - 두 번째 페이지
        assertAll(
                () -> assertThat(response2.reviews()).hasSize(1),
                () -> assertThat(response2.reviews().get(0).id()).isEqualTo(review1.getId()),
                () -> assertThat(response2.hasNext()).isFalse(),
                () -> assertThat(response2.nextCursorId()).isNull(),
                () -> assertThat(response2.nextCursorLikeCount()).isNull()
        );
    }

    @Test
    void 경기_상세_통계_데이터를_조회할_때_선수평점과_팬분포를_포함한다() {
        // given
        Player homePlayer = playerRepository.findAll().get(0);
        PlayerStatistics homeStat = new PlayerStatistics(homePlayer.getId(), match.getId());
        homeStat.addNewReview(8); // 평점 = 4.0
        playerStatisticsRepository.save(homeStat);

        User author = userRepository.save(new User("팬작성자", "fan@test.com", "pass"));
        matchReviewRepository.save(new MatchReview(match.getId(), author.getId(), 9, "경기 후기", FanType.HOME));

        // when
        MatchDetailStatsResponse statsResponse = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/matches/{matchId}/detail/stats", match.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MatchDetailStatsResponse.class);

        // then
        assertAll(
                () -> assertThat(statsResponse.homePlayerAverage()).isEqualTo(4.0),
                () -> assertThat(statsResponse.awayPlayerAverage()).isEqualTo(0.0),
                () -> assertThat(statsResponse.homeCount()).isEqualTo(1L),
                () -> assertThat(statsResponse.awayCount()).isEqualTo(0L),
                () -> assertThat(statsResponse.neutralCount()).isEqualTo(0L)
        );
    }

    @Test
    void 경기_상세_결과_조회시_리뷰_종료시간을_함께_반환한다() {
        // given
        LocalDateTime finishedAt = LocalDateTime.of(2026, 4, 28, 21, 0);
        match.finish(finishedAt);
        matchRepository.save(match);

        // when
        MatchDetailResultResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/matches/{matchId}/detail/result", match.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MatchDetailResultResponse.class);

        // then
        assertThat(response.reviewEndTime()).isEqualTo(finishedAt.plusDays(2)); // 48h limit
    }

    @Test
    void 경기_상세_결과_조회시_종료시간이_없으면_리뷰_종료시간은_null이다() {
        // given
        // match from setUp has finishedAt = null

        // when
        MatchDetailResultResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/matches/{matchId}/detail/result", match.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MatchDetailResultResponse.class);

        // then
        assertThat(response.reviewEndTime()).isNull();
    }
}


