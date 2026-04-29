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
import com.example.pitchboxd.match.matchReview.infrastructure.MatchReviewLikeRepository;
import com.example.pitchboxd.match.matchReview.infrastructure.MatchReviewRepository;
import com.example.pitchboxd.match.matchStatistics.domain.FanType;
import com.example.pitchboxd.matchDetail.dto.response.MatchDetailMatchReviewResponse;
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
import java.time.LocalDateTime;
import java.util.List;
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
    private DatabaseCleaner databaseCleaner;

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
    void 경기의_정적_데이터를_조회한다() {
        // given
        Player homePlayer1 = playerRepository.save(new Player(homeTeam.getId(), "홈선수1", "p1"));
        Player homePlayer2 = playerRepository.save(new Player(homeTeam.getId(), "홈선수2", "p2"));
        Player awayPlayer1 = playerRepository.save(new Player(awayTeam.getId(), "원정선수1", "p3"));
        Player awayPlayer2 = playerRepository.save(new Player(awayTeam.getId(), "원정선수2", "p4"));

        matchLineupRepository.save(new MatchLineup(match.getId(), homePlayer1.getId(), 7, ParticipationStatus.STARTER));
        matchLineupRepository.save(new MatchLineup(match.getId(), homePlayer2.getId(), 10, ParticipationStatus.BENCH));
        matchLineupRepository.save(new MatchLineup(match.getId(), awayPlayer1.getId(), 9, ParticipationStatus.STARTER));
        matchLineupRepository.save(
                new MatchLineup(match.getId(), awayPlayer2.getId(), 11, ParticipationStatus.SUBSTITUTED_IN));

        // when
        MatchDetailResponse result = matchDetailFacadeService.getMatchStaticData(match.getId());

        // then
        assertAll(
                () -> assertThat(result.season()).isEqualTo("2026 K리그1"),
                () -> assertThat(result.round()).isEqualTo("1R"),
                () -> assertThat(result.dateTime()).isEqualTo(LocalDateTime.of(2026, 4, 28, 19, 0)),
                () -> assertThat(result.location()).isEqualTo("상암"),
                () -> assertThat(result.homeTeam()).isEqualTo("홈팀"),
                () -> assertThat(result.awayTeam()).isEqualTo("원정팀"),
                () -> assertThat(result.homeScore()).isEqualTo(2),
                () -> assertThat(result.awayScore()).isEqualTo(1),
                () -> assertThat(result.homeLineups().responses()).hasSize(1),
                () -> assertThat(result.homeLineups().responses().get(0).playerName()).isEqualTo("홈선수1"),
                () -> assertThat(result.awayLineups().responses()).hasSize(2),
                () -> assertThat(result.awayLineups().responses())
                        .extracting("playerName")
                        .containsExactlyInAnyOrder("원정선수1", "원정선수2")
        );
    }

    @Test
    void 라인업_데이터가_없는_경우에도_정적_데이터를_조회한다() {
        // when
        MatchDetailResponse result = matchDetailFacadeService.getMatchStaticData(match.getId());

        // then
        assertAll(
                () -> assertThat(result.season()).isEqualTo("2026 K리그1"),
                () -> assertThat(result.homeTeam()).isEqualTo("홈팀"),
                () -> assertThat(result.awayTeam()).isEqualTo("원정팀"),
                () -> assertThat(result.homeLineups().responses()).isEmpty(),
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
}
