package com.example.pitchboxd.player.review.service.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.player.review.domain.PlayerReview;
import com.example.pitchboxd.player.review.dto.request.PlayerReviewCreateRequest;
import com.example.pitchboxd.player.review.infrastructure.PlayerReviewRepository;
import com.example.pitchboxd.support.DatabaseCleaner;
import org.junit.jupiter.api.AfterEach;
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
class PlayerReviewServiceTest {

    @Autowired
    private PlayerReviewService playerReviewService;

    @Autowired
    private PlayerReviewRepository playerReviewRepository;

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
    void 플레이어_리뷰를_저장한다() {
        // given
        Long matchId = 1L;
        Long playerId = 1L;
        Long userId = 1L;
        int point = 5;
        String content = "훌륭한 경기력이었습니다.";
        PlayerReviewCreateRequest request = new PlayerReviewCreateRequest(playerId, content, point);

        // when
        PlayerReview savedReview = playerReviewService.save(request, matchId, userId);

        // then
        assertAll(
                () -> assertThat(savedReview.getId()).isNotNull(),
                () -> assertThat(playerReviewRepository.existsById(savedReview.getId())).isTrue(),
                () -> assertThat(savedReview.getMatchId()).isEqualTo(matchId),
                () -> assertThat(savedReview.getPlayerId()).isEqualTo(playerId),
                () -> assertThat(savedReview.getUserId()).isEqualTo(userId),
                () -> assertThat(savedReview.getPoint()).isEqualTo(point),
                () -> assertThat(savedReview.getContent()).isEqualTo(content)
        );
    }

    @Test
    void 이미_리뷰를_작성했는지_확인한다() {
        // given
        Long matchId = 1L;
        Long playerId = 1L;
        Long userId = 1L;
        PlayerReviewCreateRequest request = new PlayerReviewCreateRequest(playerId, "좋아요", 5);
        playerReviewService.save(request, matchId, userId);

        // when
        boolean exists = playerReviewService.hasAlreadyReviewed(matchId, playerId, userId);
        boolean notExists = playerReviewService.hasAlreadyReviewed(matchId, playerId, 999L);

        // then
        assertAll(
                () -> assertThat(exists).isTrue(),
                () -> assertThat(notExists).isFalse()
        );
    }
}
