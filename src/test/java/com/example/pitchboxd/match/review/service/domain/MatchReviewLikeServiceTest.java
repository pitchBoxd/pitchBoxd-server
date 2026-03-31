package com.example.pitchboxd.match.review.service.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.match.review.domain.MatchReviewLike;
import com.example.pitchboxd.match.review.infrastructure.MatchReviewLikeRepository;
import com.example.pitchboxd.support.DatabaseCleaner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@DisplayNameGeneration(ReplaceUnderscores.class)
class MatchReviewLikeServiceTest {

    @Autowired
    private MatchReviewLikeService matchReviewLikeService;

    @Autowired
    private MatchReviewLikeRepository matchReviewLikeRepository;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();
    }

    @Test
    void 리뷰_좋아요_여부를_확인한다() {
        // given
        Long matchReviewId = 1L;
        Long userId = 1L;
        matchReviewLikeRepository.save(new MatchReviewLike(matchReviewId, userId));

        // when
        boolean isLiked = matchReviewLikeService.isLiked(matchReviewId, userId);
        boolean isNotLiked = matchReviewLikeService.isLiked(matchReviewId, 999L);

        // then
        assertAll(
                () -> assertThat(isLiked).isTrue(),
                () -> assertThat(isNotLiked).isFalse()
        );
    }

    @Test
    void 리뷰_좋아요를_저장한다() {
        // given
        Long matchReviewId = 1L;
        Long userId = 1L;

        // when
        matchReviewLikeService.save(matchReviewId, userId);

        // then
        boolean exists = matchReviewLikeRepository.existsByMatchReviewIdAndUserId(matchReviewId, userId);
        assertThat(exists).isTrue();
    }

    @Test
    void 리뷰_좋아요를_삭제한다() {
        // given
        Long matchReviewId = 1L;
        Long userId = 1L;
        matchReviewLikeRepository.save(new MatchReviewLike(matchReviewId, userId));

        // when
        matchReviewLikeService.delete(matchReviewId, userId);

        // then
        boolean exists = matchReviewLikeRepository.existsByMatchReviewIdAndUserId(matchReviewId, userId);
        assertThat(exists).isFalse();
    }
}
