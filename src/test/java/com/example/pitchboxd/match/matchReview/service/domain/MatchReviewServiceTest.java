package com.example.pitchboxd.match.matchReview.service.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.match.matchReview.domain.MatchReview;
import com.example.pitchboxd.match.matchReview.dto.request.MatchReviewCreateRequest;
import com.example.pitchboxd.match.matchReview.infrastructure.MatchReviewRepository;
import com.example.pitchboxd.match.matchStatistics.domain.FanType;
import com.example.pitchboxd.support.DatabaseCleaner;
import java.util.List;
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
class MatchReviewServiceTest {

    @Autowired
    private MatchReviewService matchReviewService;

    @Autowired
    private MatchReviewRepository matchReviewRepository;

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
    void 경기_리뷰를_저장한다() {
        // given
        Long userId = 1L;
        Long matchId = 1L;
        Integer point = 5;
        String content = "재밌는 경기였습니다.";
        MatchReviewCreateRequest request = new MatchReviewCreateRequest(content, point);

        // when
        MatchReview savedReview = matchReviewService.save(request, FanType.HOME, matchId, userId);

        // then
        List<MatchReview> allReviews = matchReviewRepository.findAll();
        assertAll(
                () -> assertThat(savedReview.getId()).isNotNull(),
                () -> assertThat(allReviews).hasSize(1),
                () -> assertThat(allReviews.get(0).getPoint()).isEqualTo(point),
                () -> assertThat(allReviews.get(0).getContent()).isEqualTo(content),
                () -> assertThat(allReviews.get(0).getUserId()).isEqualTo(userId),
                () -> assertThat(allReviews.get(0).getMatchId()).isEqualTo(matchId)
        );
    }

    @Test
    void 경기_리뷰가_존재하는지_확인한다() {
        // given
        Long matchId = 1L;
        Long userId = 1L;
        matchReviewRepository.save(new MatchReview(matchId, userId, 5, "좋아요", FanType.HOME));

        // when
        boolean exists = matchReviewService.isExist(matchId, userId);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void 존재하지_않는_경기_리뷰인_경우_거짓을_반환한다() {
        // given
        Long matchId = 1L;
        Long userId = 1L;

        // when
        boolean exists = matchReviewService.isExist(matchId, userId);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void 경기_리뷰를_ID로_조회한다() {
        // given
        MatchReview review = matchReviewRepository.save(new MatchReview(1L, 1L, 5, "좋아요", FanType.HOME));

        // when
        MatchReview foundReview = matchReviewService.findById(review.getId());

        // then
        assertThat(foundReview.getId()).isEqualTo(review.getId());
    }

    @Test
    void 존재하지_않는_경기_리뷰_ID로_조회시_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> matchReviewService.findById(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.MATCH_REVIEW_NOT_FOUND.getMessage());
    }

    @Test
    void 경기_리뷰를_비관적_락을_걸고_조회한다() {
        // given
        MatchReview review = matchReviewRepository.save(new MatchReview(1L, 1L, 5, "좋아요", FanType.HOME));

        // when
        MatchReview foundReview = matchReviewService.findByIdForUpdate(review.getId());

        // then
        assertThat(foundReview.getId()).isEqualTo(review.getId());
    }

    @Test
    void 존재하지_않는_경기_리뷰_ID로_비관적_락_조회시_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> matchReviewService.findByIdForUpdate(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.MATCH_REVIEW_NOT_FOUND.getMessage());
    }

    @Test
    void 경기_리뷰를_삭제한다() {
        // given
        MatchReview review = matchReviewRepository.save(new MatchReview(1L, 1L, 5, "좋아요", FanType.HOME));

        // when
        matchReviewService.delete(review.getId());

        // then
        assertThat(matchReviewRepository.findById(review.getId())).isEmpty();
    }
}
