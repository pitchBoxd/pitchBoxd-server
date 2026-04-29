package com.example.pitchboxd.match.matchReview.service.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.match.matchReview.domain.MatchReviewLike;
import com.example.pitchboxd.match.matchReview.infrastructure.MatchReviewLikeRepository;
import com.example.pitchboxd.support.DatabaseCleaner;
import java.util.Collections;
import java.util.List;
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

    @Test
    void 리뷰_ID_목록이_비어있으면_빈_맵을_반환한다() {
        // when
        java.util.Map<Long, Boolean> result = matchReviewLikeService.checkLikedStatusForReviews(Collections.emptyList(),
                1L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 유저_ID가_null이면_모두_좋아요_하지_않은_상태로_반환한다() {
        // given
        java.util.List<Long> reviewIds = List.of(1L, 2L, 3L);

        // when
        java.util.Map<Long, Boolean> result = matchReviewLikeService.checkLikedStatusForReviews(reviewIds, null);

        // then
        assertAll(
                () -> assertThat(result).hasSize(3),
                () -> assertThat(result.get(1L)).isFalse(),
                () -> assertThat(result.get(2L)).isFalse(),
                () -> assertThat(result.get(3L)).isFalse()
        );
    }

    @Test
    void 리뷰_ID_목록에_대한_좋아요_여부를_정확히_반환한다() {
        // given
        Long userId = 1L;
        Long likedReviewId1 = 1L;
        Long likedReviewId2 = 2L;
        Long notLikedReviewId = 3L;

        matchReviewLikeRepository.save(new MatchReviewLike(likedReviewId1, userId));
        matchReviewLikeRepository.save(new MatchReviewLike(likedReviewId2, userId));
        matchReviewLikeRepository.save(new MatchReviewLike(notLikedReviewId, 999L)); // 다른 유저의 좋아요

        java.util.List<Long> reviewIds = List.of(likedReviewId1, likedReviewId2, notLikedReviewId);

        // when
        java.util.Map<Long, Boolean> result = matchReviewLikeService.checkLikedStatusForReviews(reviewIds, userId);

        // then
        assertAll(
                () -> assertThat(result).hasSize(3),
                () -> assertThat(result.get(likedReviewId1)).isTrue(),
                () -> assertThat(result.get(likedReviewId2)).isTrue(),
                () -> assertThat(result.get(notLikedReviewId)).isFalse()
        );
    }
}
