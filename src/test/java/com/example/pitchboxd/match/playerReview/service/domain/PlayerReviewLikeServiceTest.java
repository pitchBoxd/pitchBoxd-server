package com.example.pitchboxd.match.playerReview.service.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.match.playerReview.domain.PlayerReviewLike;
import com.example.pitchboxd.match.playerReview.infrastructure.PlayerReviewLikeRepository;
import com.example.pitchboxd.support.DatabaseCleaner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PlayerReviewLikeServiceTest {

    @Autowired
    private PlayerReviewLikeService playerReviewLikeService;

    @Autowired
    private PlayerReviewLikeRepository playerReviewLikeRepository;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();
    }

    @AfterEach
    void tearDown() {
        databaseCleaner.clean();
    }

    @Test
    void 좋아요_여부를_확인한다() {
        // given
        Long playerReviewId = 1L;
        Long userId = 1L;
        playerReviewLikeRepository.save(new PlayerReviewLike(playerReviewId, userId));

        // when
        boolean isLiked = playerReviewLikeService.isLiked(playerReviewId, userId);
        boolean isNotLiked = playerReviewLikeService.isLiked(playerReviewId, 999L);

        // then
        assertAll(
                () -> assertThat(isLiked).isTrue(),
                () -> assertThat(isNotLiked).isFalse()
        );
    }

    @Test
    void 좋아요를_저장한다() {
        // given
        Long playerReviewId = 1L;
        Long userId = 1L;

        // when
        playerReviewLikeService.save(playerReviewId, userId);

        // then
        boolean exists = playerReviewLikeRepository.existsByPlayerReviewIdAndUserId(playerReviewId, userId);
        assertThat(exists).isTrue();
    }

    @Test
    void 좋아요를_삭제한다() {
        // given
        Long playerReviewId = 1L;
        Long userId = 1L;
        playerReviewLikeRepository.save(new PlayerReviewLike(playerReviewId, userId));

        // when
        playerReviewLikeService.delete(playerReviewId, userId);

        // then
        boolean exists = playerReviewLikeRepository.existsByPlayerReviewIdAndUserId(playerReviewId, userId);
        assertThat(exists).isFalse();
    }
}
