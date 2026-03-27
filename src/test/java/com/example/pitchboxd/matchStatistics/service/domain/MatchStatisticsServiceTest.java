package com.example.pitchboxd.matchStatistics.service.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.matchStatistics.domain.FanType;
import com.example.pitchboxd.matchStatistics.domain.MatchStatistics;
import com.example.pitchboxd.matchStatistics.infrastructure.MatchStatisticsRepository;
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
}
