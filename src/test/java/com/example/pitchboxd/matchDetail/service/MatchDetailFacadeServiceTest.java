package com.example.pitchboxd.matchDetail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchResult;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import com.example.pitchboxd.match.core.infrastructure.MatchRepository;
import com.example.pitchboxd.match.lineup.domain.MatchLineup;
import com.example.pitchboxd.match.lineup.domain.ParticipationStatus;
import com.example.pitchboxd.match.lineup.infrastructure.MatchLineupRepository;
import com.example.pitchboxd.match.matchReview.domain.MatchReview;
import com.example.pitchboxd.match.matchReview.domain.MatchReviewLike;
import com.example.pitchboxd.match.matchReview.domain.ReviewSortType;
import com.example.pitchboxd.match.matchReview.infrastructure.MatchReviewLikeRepository;
import com.example.pitchboxd.match.matchReview.infrastructure.MatchReviewRepository;
import com.example.pitchboxd.match.matchStatistics.domain.FanType;
import com.example.pitchboxd.match.matchStatistics.domain.MatchStatistics;
import com.example.pitchboxd.match.matchStatistics.infrastructure.MatchStatisticsRepository;
import com.example.pitchboxd.match.playerReview.domain.PlayerReview;
import com.example.pitchboxd.match.playerReview.infrastructure.PlayerReviewRepository;
import com.example.pitchboxd.match.playerStatistics.domain.PlayerStatistics;
import com.example.pitchboxd.match.playerStatistics.infrastructure.PlayerStatisticsRepository;
import com.example.pitchboxd.matchDetail.dto.response.MatchDetailMatchReviewResponse;
import com.example.pitchboxd.matchDetail.dto.response.MatchDetailMatchReviewResponses;
import com.example.pitchboxd.matchDetail.dto.response.MatchDetailPersonalResponse;
import com.example.pitchboxd.matchDetail.dto.response.MatchDetailResultResponse;
import com.example.pitchboxd.matchDetail.dto.response.MatchDetailStatsResponse;
import com.example.pitchboxd.matchDetail.dto.response.MatchReviewSliceResponse;
import com.example.pitchboxd.player.domain.Player;
import com.example.pitchboxd.player.infrastructure.PlayerRepository;
import com.example.pitchboxd.season.domain.Season;
import com.example.pitchboxd.season.infrastructure.SeasonRepository;
import com.example.pitchboxd.support.DatabaseCleaner;
import com.example.pitchboxd.team.domain.Team;
import com.example.pitchboxd.team.infrastructure.TeamRepository;
import com.example.pitchboxd.user.domain.User;
import com.example.pitchboxd.user.infrastructure.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MatchDetailFacadeServiceTest {

    @Autowired
    private MatchDetailFacadeService matchDetailFacadeService;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private SeasonRepository seasonRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MatchLineupRepository matchLineupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchReviewRepository matchReviewRepository;

    @Autowired
    private MatchReviewLikeRepository matchReviewLikeRepository;

    @Autowired
    private PlayerStatisticsRepository playerStatisticsRepository;

    @Autowired
    private MatchStatisticsRepository matchStatisticsRepository;

    @Autowired
    private DatabaseCleaner databaseCleaner;
    @Autowired
    private PlayerReviewRepository playerReviewRepository;

    private Team homeTeam;
    private Team awayTeam;
    private Season season;
    private Match match;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();

        season = seasonRepository.save(new Season("2026 K리그1"));
        homeTeam = teamRepository.save(new Team("홈팀", "naver1"));
        awayTeam = teamRepository.save(new Team("원정팀", "naver2"));

        match = new Match(season.getId(), "1R", homeTeam.getId(), awayTeam.getId(),
                LocalDateTime.of(2026, 4, 28, 19, 0), MatchStatus.FINISHED, "상암", "match-1");
        match.decideMatchResult(new MatchResult(2, 1, List.of(), List.of()));
        matchRepository.save(match);
    }

    @AfterEach
    void tearDown() {
        databaseCleaner.clean();
    }

    @Test
    void 경기의_결과와_라인업_데이터를_정확히_조회한다() {
        // given
        Player player = playerRepository.save(new Player(homeTeam.getId(), "테스트선수", "pT"));
        matchLineupRepository.save(new MatchLineup(match.getId(), player.getId(), 1, ParticipationStatus.STARTER));

        // when
        MatchDetailResultResponse result = matchDetailFacadeService.getMatchResultData(match.getId());

        // then
        assertAll(
                () -> assertThat(result.seasonName()).isEqualTo("2026 K리그1"),
                () -> assertThat(result.round()).isEqualTo("1R"),
                () -> assertThat(result.startTime()).isEqualTo(LocalDateTime.of(2026, 4, 28, 19, 0)),
                () -> assertThat(result.location()).isEqualTo("상암"),
                () -> assertThat(result.homeTeamName()).isEqualTo("홈팀"),
                () -> assertThat(result.awayTeamName()).isEqualTo("원정팀"),
                () -> assertThat(result.homeScore()).isEqualTo(2),
                () -> assertThat(result.awayScore()).isEqualTo(1),
                () -> assertThat(result.homeLineups().responses()).hasSize(1),
                () -> assertThat(result.homeLineups().responses().get(0).playerName()).isEqualTo("테스트선수"),
                () -> assertThat(result.awayLineups().responses()).isEmpty()
        );
    }

    @Test
    void 경기의_인기_한줄평을_조회한다() {
        // given
        User user1 = userRepository.save(new User("유저1", "u1@test.com", "pass"));
        User loginUser = userRepository.save(new User("로그인유저", "login@test.com", "pass"));

        // 리뷰 6개 생성 (좋아요 수: 5, 4, 3, 2, 1, 0)
        MatchReview review1 = createReviewWithLikes(match.getId(), user1.getId(), 5, "리뷰1");
        MatchReview review2 = createReviewWithLikes(match.getId(), user1.getId(), 4, "리뷰2");
        MatchReview review3 = createReviewWithLikes(match.getId(), user1.getId(), 3, "리뷰3");
        MatchReview review4 = createReviewWithLikes(match.getId(), user1.getId(), 2, "리뷰4");
        MatchReview review5 = createReviewWithLikes(match.getId(), user1.getId(), 1, "리뷰5");
        MatchReview review6 = createReviewWithLikes(match.getId(), user1.getId(), 0, "리뷰6");

        matchReviewLikeRepository.save(new MatchReviewLike(review1.getId(), loginUser.getId()));

        // when (limit을 5로 설정)
        MatchDetailMatchReviewResponses result = matchDetailFacadeService.getMatchHotReviews(match.getId(),
                loginUser.getId(), 5);

        // then
        assertThat(result.responses()).hasSize(5);
        assertAll(
                () -> assertThat(result.responses().get(0).reviewId()).isEqualTo(review1.getId()),
                () -> assertThat(result.responses().get(0).isLiked()).isTrue(),
                () -> assertThat(result.responses().get(0).likeCount()).isEqualTo(5),
                () -> assertThat(result.responses().get(4).reviewId()).isEqualTo(review5.getId()),
                () -> assertThat(result.responses().get(4).likeCount()).isEqualTo(1),
                // review6 (좋아요 0개)는 포함되지 않아야 함
                () -> assertThat(result.responses()).extracting("reviewId").doesNotContain(review6.getId())
        );
    }

    @Test
    void 비로그인_유저도_경기의_인기_한줄평을_조회한다() {
        // given
        User user1 = userRepository.save(new User("유저1", "u1@test.com", "pass"));
        MatchReview hotReview = new MatchReview(match.getId(), user1.getId(), 10, "최고의 경기", FanType.HOME);
        hotReview.addOneLikeCount();
        matchReviewRepository.save(hotReview);

        // when
        MatchDetailMatchReviewResponses result = matchDetailFacadeService.getMatchHotReviews(match.getId(), null, 10);

        // then
        assertThat(result.responses()).hasSize(1);
        MatchDetailMatchReviewResponse firstResponse = result.responses().get(0);
        assertAll(
                () -> assertThat(firstResponse.reviewId()).isEqualTo(hotReview.getId()),
                () -> assertThat(firstResponse.isLiked()).isFalse()
        );
    }

    private MatchReview createReviewWithLikes(Long matchId, Long userId, int likeCount, String content) {
        MatchReview review = new MatchReview(matchId, userId, 5, content, FanType.HOME);
        for (int i = 0; i < likeCount; i++) {
            review.addOneLikeCount();
        }
        return matchReviewRepository.save(review);
    }

    @Test
    void 경기의_통계_데이터_조회_시_MOM과_Top3_선수가_정렬_조건에_맞게_반환된다() {
        // given
        Player player1 = playerRepository.save(new Player(homeTeam.getId(), "선수1", "p1"));
        Player player2 = playerRepository.save(new Player(homeTeam.getId(), "선수2", "p2"));
        Player player3 = playerRepository.save(new Player(homeTeam.getId(), "선수3", "p3"));
        Player player4 = playerRepository.save(new Player(homeTeam.getId(), "선수4", "p4"));

        matchLineupRepository.save(new MatchLineup(match.getId(), player1.getId(), 1, ParticipationStatus.STARTER));
        matchLineupRepository.save(new MatchLineup(match.getId(), player2.getId(), 2, ParticipationStatus.STARTER));
        matchLineupRepository.save(new MatchLineup(match.getId(), player3.getId(), 3, ParticipationStatus.STARTER));
        matchLineupRepository.save(new MatchLineup(match.getId(), player4.getId(), 4, ParticipationStatus.STARTER));

        PlayerStatistics stat1 = new PlayerStatistics(player1.getId(), match.getId());
        stat1.addNewReview(9); // 평점 = 4.5
        playerStatisticsRepository.save(stat1);

        PlayerStatistics stat2 = new PlayerStatistics(player2.getId(), match.getId());
        stat2.addNewReview(9);
        stat2.addNewReview(9); // 평점 = 4.5, 투표 = 2
        playerStatisticsRepository.save(stat2);

        PlayerStatistics stat3 = new PlayerStatistics(player3.getId(), match.getId());
        stat3.addNewReview(8); // 평점 = 4.0
        playerStatisticsRepository.save(stat3);

        PlayerStatistics stat4 = new PlayerStatistics(player4.getId(), match.getId());
        stat4.addNewReview(9);
        stat4.addNewReview(9); // 평점 = 4.5, 투표 = 2 (player4.id > player2.id)
        playerStatisticsRepository.save(stat4);

        // when
        MatchDetailStatsResponse result = matchDetailFacadeService.getMatchStatsData(match.getId());

        // then
        assertThat(result.highlights().mom().playerId()).isEqualTo(player4.getId());
        assertThat(result.highlights().mom().name()).isEqualTo("선수4");
        assertThat(result.highlights().mom().averageRating()).isEqualTo(4.5);

        assertThat(result.highlights().top3()).hasSize(3);
        assertThat(result.highlights().top3().get(0).playerId()).isEqualTo(player4.getId());
        assertThat(result.highlights().top3().get(1).playerId()).isEqualTo(player2.getId());
        assertThat(result.highlights().top3().get(2).playerId()).isEqualTo(player1.getId());
    }

    @Test
    void 경기의_통계_데이터_조회_시_평균평점들과_평점분포도가_올바르게_계산된다() {
        // given
        MatchStatistics matchStats = new MatchStatistics(match.getId());
        matchStats.addNewReview(9, FanType.HOME);
        matchStats.addNewReview(9, FanType.HOME);
        matchStats.addNewReview(8, FanType.AWAY);
        matchStats.addNewReview(6, FanType.NEUTRAL);
        matchStats.addNewReview(8, FanType.NEUTRAL);
        matchStatisticsRepository.save(matchStats);

        User user = userRepository.save(new User("테스터", "test@test.com", "pass"));
        matchReviewRepository.save(new MatchReview(match.getId(), user.getId(), 8, "좋은경기", FanType.NEUTRAL));
        matchReviewRepository.save(new MatchReview(match.getId(), user.getId(), 8, "재밌네요", FanType.HOME));
        matchReviewRepository.save(new MatchReview(match.getId(), user.getId(), 5, "그저그럼", FanType.AWAY));

        // when
        MatchDetailStatsResponse result = matchDetailFacadeService.getMatchStatsData(match.getId());

        // then
        assertThat(result.totalAverage()).isEqualTo(4.0);
        assertThat(result.homeAverage()).isEqualTo(4.5);
        assertThat(result.awayAverage()).isEqualTo(4.0);

        Map<Integer, Long> distribution = result.distributionMap();
        assertThat(distribution.get(8)).isEqualTo(2L);
        assertThat(distribution.get(5)).isEqualTo(1L);
    }

    @Test
    void 비회원_경기_개인_데이터_조회시_빈_응답을_반환한다() {
        // when
        MatchDetailPersonalResponse response = matchDetailFacadeService.getMatchPersonalData(match.getId(), null);

        // then
        assertAll(
                () -> assertThat(response.isEvaluated()).isFalse(),
                () -> assertThat(response.myMatchReview()).isNull(),
                () -> assertThat(response.myPlayerReviews()).isEmpty()
        );
    }

    @Test
    void 로그인_사용자_경기_개인_데이터_조회시_리뷰와_플레이어_리뷰가_포함된다() {
        // given
        User user = userRepository.save(new User("테스터", "test@test.com", "pass"));
        MatchReview myMatchReview = new MatchReview(match.getId(), user.getId(), 8, "좋은 경기", FanType.HOME);
        matchReviewRepository.save(myMatchReview);

        Player player = playerRepository.save(new Player(homeTeam.getId(), "선수A", "pA"));
        PlayerStatistics stats = new PlayerStatistics(player.getId(), match.getId());
        playerStatisticsRepository.save(stats);
        PlayerReview playerReview = new PlayerReview(match.getId(), user.getId(), player.getId(), 9, "멋진 플레이",
                FanType.HOME);
        playerReviewRepository.save(playerReview);

        // when
        MatchDetailPersonalResponse response = matchDetailFacadeService.getMatchPersonalData(match.getId(),
                user.getId());

        // then
        assertAll(
                () -> assertThat(response.isEvaluated()).isTrue(),
                () -> assertThat(response.myMatchReview()).isNotNull(),
                () -> assertThat(response.myMatchReview().reviewId()).isEqualTo(myMatchReview.getId()),
                () -> assertThat(response.myMatchReview().likeCount()).isEqualTo(0L),
                () -> assertThat(response.myPlayerReviews()).hasSize(1),
                () -> assertThat(response.myPlayerReviews().get(0).playerId()).isEqualTo(player.getId()),
                () -> assertThat(response.myPlayerReviews().get(0).likeCount()).isEqualTo(0L)
        );
    }

    @Test
    void 리뷰_페이징과_정렬_및_좋아요_상태_조회가_정확하다() {
        // given
        User user = userRepository.save(new User("유저", "u@test.com", "pass"));
        // create 5 reviews with varying like counts
        for (int i = 1; i <= 5; i++) {
            MatchReview review = new MatchReview(match.getId(), user.getId(), i * 2, "리뷰" + i, FanType.HOME);
            review.addOneLikeCount(); // each review gets 1 like
            for (int j = 1; j < i; j++) {
                review.addOneLikeCount(); // additional likes
            }
            matchReviewRepository.save(review);
        }
        // like status: another user liked first three reviews
        User other = userRepository.save(new User("다른유저", "other@test.com", "pass"));
        List<MatchReview> allReviews = matchReviewRepository.findAllByMatchId(match.getId());
        for (int i = 0; i < 3; i++) {
            matchReviewLikeRepository.save(new MatchReviewLike(allReviews.get(i).getId(), other.getId()));
        }

        // when: request first page size 2 sorted by LIKE
        MatchReviewSliceResponse slice1 = matchDetailFacadeService.getMatchReviews(
                match.getId(), null, null, ReviewSortType.LIKE, 2, other.getId());

        // then
        assertAll(
                () -> assertThat(slice1.reviews()).hasSize(2),
                () -> assertThat(slice1.hasNext()).isTrue(),
                () -> assertThat(slice1.reviews().get(0).likeCount()).isGreaterThanOrEqualTo(
                        slice1.reviews().get(1).likeCount()),
                () -> assertThat(slice1.reviews().get(0).isLiked()).isFalse(),
                () -> assertThat(slice1.reviews().get(1).isLiked()).isFalse()
        );

        // when: request second page using cursor
        MatchReviewSliceResponse slice2 = matchDetailFacadeService.getMatchReviews(
                match.getId(), slice1.nextCursorId(), slice1.nextCursorLikeCount(), ReviewSortType.LIKE, 2,
                other.getId());

        // then
        assertAll(
                () -> assertThat(slice2.reviews()).hasSize(2),
                () -> assertThat(slice2.hasNext()).isTrue(),
                () -> assertThat(slice2.reviews().get(0).isLiked()).isTrue(),
                () -> assertThat(slice2.reviews().get(1).isLiked()).isTrue()
        );
    }

    @Test
    void 경기의_상세_통계_조회_시_라인업_선수_평점_평균과_팬_분포가_정확히_반환된다() {
        // given
        Player homePlayer = playerRepository.save(new Player(homeTeam.getId(), "홈선수", "p1"));
        Player awayPlayer = playerRepository.save(new Player(awayTeam.getId(), "원정선수", "p2"));

        matchLineupRepository.save(new MatchLineup(match.getId(), homePlayer.getId(), 7, ParticipationStatus.STARTER));
        matchLineupRepository.save(new MatchLineup(match.getId(), awayPlayer.getId(), 9, ParticipationStatus.STARTER));

        // 선수 평점 통계 정보 셋팅
        PlayerStatistics homeStat = new PlayerStatistics(homePlayer.getId(), match.getId());
        homeStat.addNewReview(8); // 평점 = 4.0
        playerStatisticsRepository.save(homeStat);

        PlayerStatistics awayStat = new PlayerStatistics(awayPlayer.getId(), match.getId());
        awayStat.addNewReview(10); // 평점 = 5.0
        playerStatisticsRepository.save(awayStat);

        // 경기 리뷰 생성 (팬 분포 확인용)
        User user1 = userRepository.save(new User("유저1", "u1@test.com", "pass"));
        User user2 = userRepository.save(new User("유저2", "u2@test.com", "pass"));
        User user3 = userRepository.save(new User("유저3", "u3@test.com", "pass"));

        matchReviewRepository.save(new MatchReview(match.getId(), user1.getId(), 8, "좋음", FanType.HOME));
        matchReviewRepository.save(new MatchReview(match.getId(), user2.getId(), 7, "보통", FanType.AWAY));
        matchReviewRepository.save(new MatchReview(match.getId(), user3.getId(), 9, "훌륭", FanType.NEUTRAL));

        // when
        MatchDetailStatsResponse result = matchDetailFacadeService.getMatchStatsData(match.getId());

        // then
        assertAll(
                () -> assertThat(result.homePlayerAverage()).isEqualTo(4.0),
                () -> assertThat(result.awayPlayerAverage()).isEqualTo(5.0),
                () -> assertThat(result.homeCount()).isEqualTo(1L),
                () -> assertThat(result.awayCount()).isEqualTo(1L),
                () -> assertThat(result.neutralCount()).isEqualTo(1L)
        );
    }

}

