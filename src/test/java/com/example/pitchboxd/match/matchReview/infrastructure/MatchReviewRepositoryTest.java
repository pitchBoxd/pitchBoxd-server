package com.example.pitchboxd.match.matchReview.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.pitchboxd.match.matchReview.domain.MatchReview;
import com.example.pitchboxd.match.matchStatistics.domain.FanType;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MatchReviewRepositoryTest {

    @Autowired
    private MatchReviewRepository matchReviewRepository;

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
    void 매치_ID로_평점_분포를_조회한다() {
        // given
        matchReviewRepository.save(new MatchReview(1L, 1L, 8, "좋은 매치", FanType.HOME));
        matchReviewRepository.save(new MatchReview(1L, 2L, 8, "재밌네요", FanType.NEUTRAL));
        matchReviewRepository.save(new MatchReview(1L, 3L, 5, "별로네요", FanType.AWAY));

        // when
        List<Object[]> distribution = matchReviewRepository.countPointDistributionByMatchId(1L);

        // then
        assertThat(distribution).isNotEmpty();
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
}
