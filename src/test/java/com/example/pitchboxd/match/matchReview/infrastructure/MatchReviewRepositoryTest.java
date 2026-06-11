package com.example.pitchboxd.match.matchReview.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.pitchboxd.global.infrastructure.SystemClockHolder;
import com.example.pitchboxd.match.matchReview.domain.MatchReview;
import com.example.pitchboxd.match.playerReview.domain.PlayerReview;
import com.example.pitchboxd.match.playerReview.infrastructure.PlayerReviewRepository;
import com.example.pitchboxd.match.playerStatistics.domain.PlayerStatistics;
import com.example.pitchboxd.match.playerStatistics.infrastructure.PlayerStatisticsRepository;
import com.example.pitchboxd.match.matchStatistics.domain.FanType;
import com.example.pitchboxd.support.DatabaseCleaner;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@Import({DatabaseCleaner.class, SystemClockHolder.class})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MatchReviewRepositoryTest {

    @Autowired
    private MatchReviewRepository matchReviewRepository;

    @Autowired
    private PlayerReviewRepository playerReviewRepository;

    @Autowired
    private PlayerStatisticsRepository playerStatisticsRepository;

    @Test
    void 매치_ID와_유저_ID로_매치_리뷰를_조회한다() {
        // given
        MatchReview savedReview = matchReviewRepository.save(new MatchReview(1L, 10L, 8, "좋은 매치", FanType.HOME));

        // when
        Optional<MatchReview> foundReview = matchReviewRepository.findByMatchIdAndUserId(1L, 10L);

        // then
        assertThat(foundReview).isPresent();
        assertThat(foundReview.get().getId()).isEqualTo(savedReview.getId());
    }

    @Test
    void pointDistributionTest() {
        // given
        matchReviewRepository.save(new MatchReview(1L, 1L, 8, "좋은 매치", FanType.HOME));
        matchReviewRepository.save(new MatchReview(1L, 2L, 8, "재밌네요", FanType.NEUTRAL));
        matchReviewRepository.save(new MatchReview(1L, 3L, 5, "별로네요", FanType.AWAY));

        // when
        List<Object[]> distribution = matchReviewRepository.countPointDistributionByMatchId(1L);

        // then
        assertThat(distribution).isNotEmpty();
        // First entry: point 8 has count 2
        // Second entry: point 5 has count 1
        // Note: ORDER by is not specified in the query, so we just verify contents.
        boolean found8 = false;
        boolean found5 = false;
        for (Object[] row : distribution) {
            Integer point = (Integer) row[0];
            Long count = (Long) row[1];
            if (point == 8) {
                assertThat(count).isEqualTo(2L);
                found8 = true;
            } else if (point == 5) {
                assertThat(count).isEqualTo(1L);
                found5 = true;
            }
        }
        assertThat(found8).isTrue();
        assertThat(found5).isTrue();
    }

    @Test
    void 매치_ID와_유저_ID로_모든_선수_리뷰를_조회한다() {
        // given
        PlayerReview review1 = playerReviewRepository.save(new PlayerReview(1L, 100L, 10L, 8, "선수 1 좋은 활약"));
        PlayerReview review2 = playerReviewRepository.save(new PlayerReview(1L, 101L, 10L, 7, "선수 2 무난함"));
        playerReviewRepository.save(new PlayerReview(1L, 100L, 20L, 9, "다른 유저의 리뷰"));
        playerReviewRepository.save(new PlayerReview(2L, 100L, 10L, 6, "다른 매치의 리뷰"));

        // when
        List<PlayerReview> userReviews = playerReviewRepository.findAllByMatchIdAndUserId(1L, 10L);

        // then
        assertThat(userReviews).hasSize(2)
                .extracting(PlayerReview::getId)
                .containsExactlyInAnyOrder(review1.getId(), review2.getId());
    }

    @Test
    void 매치_ID로_모든_선수_통계를_조회한다() {
        // given
        PlayerStatistics stats1 = playerStatisticsRepository.save(new PlayerStatistics(100L, 1L));
        PlayerStatistics stats2 = playerStatisticsRepository.save(new PlayerStatistics(101L, 1L));
        playerStatisticsRepository.save(new PlayerStatistics(100L, 2L)); // 다른 매치

        // when
        List<PlayerStatistics> matchStats = playerStatisticsRepository.findAllByMatchId(1L);

        // then
        assertThat(matchStats).hasSize(2)
                .extracting(PlayerStatistics::getId)
                .containsExactlyInAnyOrder(stats1.getId(), stats2.getId());
    }
}
