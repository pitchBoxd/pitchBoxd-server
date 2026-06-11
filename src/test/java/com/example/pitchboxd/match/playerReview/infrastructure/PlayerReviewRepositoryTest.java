package com.example.pitchboxd.match.playerReview.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.pitchboxd.match.playerReview.domain.PlayerReview;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PlayerReviewRepositoryTest {

    @Autowired
    private PlayerReviewRepository playerReviewRepository;

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
}
