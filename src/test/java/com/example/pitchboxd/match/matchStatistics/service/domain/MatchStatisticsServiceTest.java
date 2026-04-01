package com.example.pitchboxd.match.matchStatistics.service.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.match.matchStatistics.domain.FanType;
import com.example.pitchboxd.match.matchStatistics.domain.MatchStatistics;
import com.example.pitchboxd.match.matchStatistics.infrastructure.MatchStatisticsRepository;
import com.example.pitchboxd.support.DatabaseCleaner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
@DisplayNameGeneration(ReplaceUnderscores.class)
class MatchStatisticsServiceTest {

    @Autowired
    private MatchStatisticsService matchStatisticsService;

    @Autowired
    private MatchStatisticsRepository matchStatisticsRepository;

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
    void 홈팀_팬이_리뷰를_등록하면_전체_통계와_홈팀_통계가_함께_업데이트된다() {
        // given
        Long matchId = 1L;
        matchStatisticsRepository.save(new MatchStatistics(matchId));
        int rating = 10;
        FanType fanType = FanType.HOME;

        // when
        matchStatisticsService.updateReview(matchId, rating, fanType);

        // then
        MatchStatistics updatedStatistics = matchStatisticsRepository.findById(matchId).orElseThrow();

        assertAll(
                () -> assertThat(updatedStatistics.getTotalRatingSum()).isEqualTo(10L),
                () -> assertThat(updatedStatistics.getTotalReviewCount()).isEqualTo(1),
                () -> assertThat(updatedStatistics.getHomeFanRatingSum()).isEqualTo(10L),
                () -> assertThat(updatedStatistics.getHomeFanReviewCount()).isEqualTo(1),
                () -> assertThat(updatedStatistics.getAwayFanRatingSum()).isEqualTo(0L),
                () -> assertThat(updatedStatistics.getAwayFanReviewCount()).isEqualTo(0)
        );

    }

    @Test
    void 원정팀_팬이_리뷰를_등록하면_전체_통계와_원정팀_통계가_함께_업데이트된다() {
        // given
        Long matchId = 1L;
        matchStatisticsRepository.save(new MatchStatistics(matchId));
        int rating = 8;
        FanType fanType = FanType.AWAY;

        // when
        matchStatisticsService.updateReview(matchId, rating, fanType);

        // then
        MatchStatistics updatedStatistics = matchStatisticsRepository.findById(matchId).orElseThrow();
        assertAll(
                () -> assertThat(updatedStatistics.getTotalRatingSum()).isEqualTo(8L),
                () -> assertThat(updatedStatistics.getTotalReviewCount()).isEqualTo(1),
                () -> assertThat(updatedStatistics.getHomeFanRatingSum()).isEqualTo(0L),
                () -> assertThat(updatedStatistics.getHomeFanReviewCount()).isEqualTo(0),
                () -> assertThat(updatedStatistics.getAwayFanRatingSum()).isEqualTo(8L),
                () -> assertThat(updatedStatistics.getAwayFanReviewCount()).isEqualTo(1)
        );
    }

    @Test
    void 중립팀_팬이_리뷰를_등록하면_전체_통계만_업데이트된다() {
        // given
        Long matchId = 1L;
        matchStatisticsRepository.save(new MatchStatistics(matchId));
        int rating = 6;
        FanType fanType = FanType.NEUTRAL;

        // when
        matchStatisticsService.updateReview(matchId, rating, fanType);

        // then
        MatchStatistics updatedStatistics = matchStatisticsRepository.findById(matchId).orElseThrow();
        assertAll(
                () -> assertThat(updatedStatistics.getTotalRatingSum()).isEqualTo(6L),
                () -> assertThat(updatedStatistics.getTotalReviewCount()).isEqualTo(1),
                () -> assertThat(updatedStatistics.getHomeFanRatingSum()).isEqualTo(0L),
                () -> assertThat(updatedStatistics.getHomeFanReviewCount()).isEqualTo(0),
                () -> assertThat(updatedStatistics.getAwayFanRatingSum()).isEqualTo(0L),
                () -> assertThat(updatedStatistics.getAwayFanReviewCount()).isEqualTo(0)
        );
    }

    @Test
    void 존재하지_않는_경기_통계_업데이트_시_예외가_발생한다() {
        // given
        Long nonExistentMatchId = 999L;
        int rating = 5;
        FanType fanType = FanType.HOME;

        // when & then
        assertThatThrownBy(() -> matchStatisticsService.updateReview(nonExistentMatchId, rating, fanType))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 동시에_100명이_리뷰를_등록해도_비관적_락을_통해_정확히_통계가_반영된다() throws InterruptedException {
        // given
        Long matchId = 1L;
        matchStatisticsRepository.save(new MatchStatistics(matchId));
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    matchStatisticsService.updateReview(matchId, 10, FanType.HOME);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        // then
        MatchStatistics updatedStatistics = matchStatisticsRepository.findById(matchId).orElseThrow();
        assertAll(
                () -> assertThat(updatedStatistics.getTotalReviewCount()).isEqualTo(threadCount),
                () -> assertThat(updatedStatistics.getTotalRatingSum()).isEqualTo(threadCount * 10L),
                () -> assertThat(updatedStatistics.getHomeFanReviewCount()).isEqualTo(threadCount),
                () -> assertThat(updatedStatistics.getHomeFanRatingSum()).isEqualTo(threadCount * 10L)
        );
    }

    @Test
    void 홈팀_팬의_평점을_수정하면_전체_통계와_홈팀_통계가_함께_반영된다() {
        // given
        Long matchId = 1L;
        MatchStatistics statistics = new MatchStatistics(matchId);
        statistics.addNewReview(5, FanType.HOME);
        matchStatisticsRepository.save(statistics);
        int ratingDelta = 3;

        // when
        matchStatisticsService.adjustReviewStatistics(matchId, ratingDelta, FanType.HOME);

        // then
        MatchStatistics updatedStatistics = matchStatisticsRepository.findById(matchId).orElseThrow();
        assertAll(
                () -> assertThat(updatedStatistics.getTotalRatingSum()).isEqualTo(8L),
                () -> assertThat(updatedStatistics.getHomeFanRatingSum()).isEqualTo(8L),
                () -> assertThat(updatedStatistics.getAwayFanRatingSum()).isEqualTo(0L)
        );
    }

    @Test
    void 원정팀_팬의_평점을_수정하면_전체_통계와_원정팀_통계가_함께_반영된다() {
        // given
        Long matchId = 1L;
        MatchStatistics statistics = new MatchStatistics(matchId);
        statistics.addNewReview(7, FanType.AWAY);
        matchStatisticsRepository.save(statistics);
        int ratingDelta = -2;

        // when
        matchStatisticsService.adjustReviewStatistics(matchId, ratingDelta, FanType.AWAY);

        // then
        MatchStatistics updatedStatistics = matchStatisticsRepository.findById(matchId).orElseThrow();
        assertAll(
                () -> assertThat(updatedStatistics.getTotalRatingSum()).isEqualTo(5L),
                () -> assertThat(updatedStatistics.getAwayFanRatingSum()).isEqualTo(5L),
                () -> assertThat(updatedStatistics.getHomeFanRatingSum()).isEqualTo(0L)
        );
    }

    @Test
    void 중립팀_팬의_평점을_수정하면_전체_통계만_반영된다() {
        // given
        Long matchId = 1L;
        MatchStatistics statistics = new MatchStatistics(matchId);
        statistics.addNewReview(4, FanType.NEUTRAL);
        matchStatisticsRepository.save(statistics);
        int ratingDelta = 1;

        // when
        matchStatisticsService.adjustReviewStatistics(matchId, ratingDelta, FanType.NEUTRAL);

        // then
        MatchStatistics updatedStatistics = matchStatisticsRepository.findById(matchId).orElseThrow();
        assertAll(
                () -> assertThat(updatedStatistics.getTotalRatingSum()).isEqualTo(5L),
                () -> assertThat(updatedStatistics.getHomeFanRatingSum()).isEqualTo(0L),
                () -> assertThat(updatedStatistics.getAwayFanRatingSum()).isEqualTo(0L)
        );
    }

    @Test
    void 존재하지_않는_경기_통계_수정_시_예외가_발생한다() {
        // given
        Long nonExistentMatchId = 999L;
        int ratingDelta = 2;
        FanType fanType = FanType.HOME;

        // when & then
        assertThatThrownBy(
                () -> matchStatisticsService.adjustReviewStatistics(nonExistentMatchId, ratingDelta, fanType))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 홈팀_팬의_리뷰를_삭제하면_전체_통계와_홈팀_통계가_함께_차감된다() {
        // given
        Long matchId = 1L;
        MatchStatistics statistics = new MatchStatistics(matchId);
        statistics.addNewReview(10, FanType.HOME);
        matchStatisticsRepository.save(statistics);

        // when
        matchStatisticsService.removeReview(matchId, 10, FanType.HOME);

        // then
        MatchStatistics updatedStatistics = matchStatisticsRepository.findById(matchId).orElseThrow();
        assertAll(
                () -> assertThat(updatedStatistics.getTotalRatingSum()).isEqualTo(0L),
                () -> assertThat(updatedStatistics.getTotalReviewCount()).isEqualTo(0),
                () -> assertThat(updatedStatistics.getHomeFanRatingSum()).isEqualTo(0L),
                () -> assertThat(updatedStatistics.getHomeFanReviewCount()).isEqualTo(0)
        );
    }

    @Test
    void 원정팀_팬의_리뷰를_삭제하면_전체_통계와_원정팀_통계가_함께_차감된다() {
        // given
        Long matchId = 1L;
        MatchStatistics statistics = new MatchStatistics(matchId);
        statistics.addNewReview(8, FanType.AWAY);
        matchStatisticsRepository.save(statistics);

        // when
        matchStatisticsService.removeReview(matchId, 8, FanType.AWAY);

        // then
        MatchStatistics updatedStatistics = matchStatisticsRepository.findById(matchId).orElseThrow();
        assertAll(
                () -> assertThat(updatedStatistics.getTotalRatingSum()).isEqualTo(0L),
                () -> assertThat(updatedStatistics.getTotalReviewCount()).isEqualTo(0),
                () -> assertThat(updatedStatistics.getAwayFanRatingSum()).isEqualTo(0L),
                () -> assertThat(updatedStatistics.getAwayFanReviewCount()).isEqualTo(0)
        );
    }

    @Test
    void 중립팀_팬의_리뷰를_삭제하면_전체_통계만_차감된다() {
        // given
        Long matchId = 1L;
        MatchStatistics statistics = new MatchStatistics(matchId);
        statistics.addNewReview(5, FanType.NEUTRAL);
        matchStatisticsRepository.save(statistics);

        // when
        matchStatisticsService.removeReview(matchId, 5, FanType.NEUTRAL);

        // then
        MatchStatistics updatedStatistics = matchStatisticsRepository.findById(matchId).orElseThrow();
        assertAll(
                () -> assertThat(updatedStatistics.getTotalRatingSum()).isEqualTo(0L),
                () -> assertThat(updatedStatistics.getTotalReviewCount()).isEqualTo(0),
                () -> assertThat(updatedStatistics.getHomeFanReviewCount()).isEqualTo(0),
                () -> assertThat(updatedStatistics.getAwayFanReviewCount()).isEqualTo(0)
        );
    }

    @Test
    void 존재하지_않는_경기_통계_삭제_시_예외가_발생한다() {
        // given
        Long nonExistentMatchId = 999L;
        int rating = 5;
        FanType fanType = FanType.HOME;

        // when & then
        assertThatThrownBy(() -> matchStatisticsService.removeReview(nonExistentMatchId, rating, fanType))
                .isInstanceOf(BusinessException.class);
    }
}
