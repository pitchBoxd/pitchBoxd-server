package com.example.pitchboxd.match.matchReview.service.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import com.example.pitchboxd.match.core.infrastructure.MatchRepository;
import com.example.pitchboxd.match.matchReview.domain.MatchReview;
import com.example.pitchboxd.match.matchReview.infrastructure.MatchReviewRepository;
import com.example.pitchboxd.match.matchReview.infrastructure.dto.HotReviewSummary;
import com.example.pitchboxd.match.matchStatistics.domain.FanType;
import com.example.pitchboxd.support.DatabaseCleaner;
import com.example.pitchboxd.support.TestClockHolder;
import com.example.pitchboxd.team.domain.Team;
import com.example.pitchboxd.team.infrastructure.TeamRepository;
import com.example.pitchboxd.user.domain.User;
import com.example.pitchboxd.user.infrastructure.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class MatchReviewQueryServiceTest {

    @Autowired
    private MatchReviewQueryService matchReviewQueryService;

    @Autowired
    private MatchReviewRepository matchReviewRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private TestClockHolder clockHolder;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();
    }

    @AfterEach
    void tearDown() {
        databaseCleaner.clean();
    }

    @Test
    void 리뷰_작성_임계치_시간을_조회한다() {
        // given
        LocalDateTime now = LocalDateTime.of(2026, 4, 22, 12, 0);
        clockHolder.setTime(now);

        // when
        LocalDateTime threshold = matchReviewQueryService.getReviewableThreshold();

        // then
        assertThat(threshold).isEqualTo(now.minusHours(48));
    }

    @Test
    void 경기_ID로_인기_리뷰_목록을_조회한다() {
        // given
        User user = userRepository.save(new User("테스터", "test@example.com", "1234", 1L));

        Team homeTeam = teamRepository.save(new Team("홈팀", "1"));
        Team awayTeam = teamRepository.save(new Team("어웨이팀", "2"));

        Match match1 = matchRepository.save(
                new Match(1L, "3R", homeTeam.getId(), awayTeam.getId(), LocalDateTime.now(), MatchStatus.FINISHED,
                        "STADIUM", "1"));
        Match match2 = matchRepository.save(
                new Match(1L, "3R", homeTeam.getId(), awayTeam.getId(), LocalDateTime.now(), MatchStatus.FINISHED,
                        "STADIUM", "2"));

        MatchReview review1 = matchReviewRepository.save(
                new MatchReview(match1.getId(), user.getId(), 10, "리뷰 1", FanType.NEUTRAL));
        MatchReview review2 = matchReviewRepository.save(
                new MatchReview(match1.getId(), user.getId(), 8, "리뷰 2", FanType.HOME));
        MatchReview review3 = matchReviewRepository.save(
                new MatchReview(match1.getId(), user.getId(), 8, "리뷰 3", FanType.HOME));
        MatchReview review4 = matchReviewRepository.save(
                new MatchReview(match1.getId(), user.getId(), 8, "리뷰 4", FanType.HOME));
        matchReviewRepository.save(
                new MatchReview(match2.getId(), user.getId(), 5, "다른 경기 리뷰", FanType.AWAY));

        review1.addOneLikeCount();

        review2.addOneLikeCount();
        review2.addOneLikeCount();

        review3.addOneLikeCount();
        review3.addOneLikeCount();
        review3.addOneLikeCount();

        review4.addOneLikeCount();
        review4.addOneLikeCount();
        review4.addOneLikeCount();
        review4.addOneLikeCount();

        // when
        List<HotReviewSummary> result = matchReviewQueryService.getTopHotReviewsByMatchId(match1.getId(), 3);

        // then
        assertAll(
                () -> assertThat(result).hasSize(3),
                () -> assertThat(result.get(0).matchId()).isEqualTo(match1.getId()),
                () -> assertThat(result.get(0).reviewId()).isEqualTo(review4.getId()),
                () -> assertThat(result.get(1).matchId()).isEqualTo(match1.getId()),
                () -> assertThat(result.get(1).reviewId()).isEqualTo(review3.getId()),
                () -> assertThat(result.get(2).matchId()).isEqualTo(match1.getId()),
                () -> assertThat(result.get(2).reviewId()).isEqualTo(review2.getId())
        );
    }
}
