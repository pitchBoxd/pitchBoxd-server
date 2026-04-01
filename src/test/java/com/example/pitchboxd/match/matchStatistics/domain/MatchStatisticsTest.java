package com.example.pitchboxd.match.matchStatistics.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class MatchStatisticsTest {

    @Test
    void 생성_시_초기값은_0이다() {
        // given
        Long matchId = 1L;

        // when
        MatchStatistics matchStatistics = new MatchStatistics(matchId);

        // then
        assertAll(
                () -> assertThat(matchStatistics.getMatchId()).isEqualTo(matchId),
                () -> assertThat(matchStatistics.getTotalRatingSum()).isZero(),
                () -> assertThat(matchStatistics.getTotalReviewCount()).isZero(),
                () -> assertThat(matchStatistics.getHomeFanRatingSum()).isZero(),
                () -> assertThat(matchStatistics.getHomeFanReviewCount()).isZero(),
                () -> assertThat(matchStatistics.getAwayFanRatingSum()).isZero(),
                () -> assertThat(matchStatistics.getAwayFanReviewCount()).isZero(),
                () -> assertThat(matchStatistics.getTotalAverage()).isEqualTo(0.0),
                () -> assertThat(matchStatistics.getHomeAverage()).isEqualTo(0.0),
                () -> assertThat(matchStatistics.getAwayAverage()).isEqualTo(0.0)
        );
    }

    @Test
    void 홈_팬의_리뷰를_추가하면_전체_통계와_홈_통계가_업데이트된다() {
        // given
        MatchStatistics matchStatistics = new MatchStatistics(1L);
        int rating = 8;

        // when
        matchStatistics.addNewReview(rating, FanType.HOME);

        // then
        assertAll(
                () -> assertThat(matchStatistics.getTotalRatingSum()).isEqualTo(8L),
                () -> assertThat(matchStatistics.getTotalReviewCount()).isEqualTo(1),
                () -> assertThat(matchStatistics.getHomeFanRatingSum()).isEqualTo(8L),
                () -> assertThat(matchStatistics.getHomeFanReviewCount()).isEqualTo(1),
                () -> assertThat(matchStatistics.getAwayFanRatingSum()).isZero(),
                () -> assertThat(matchStatistics.getAwayFanReviewCount()).isZero()
        );
    }

    @Test
    void 원정_팬의_리뷰를_추가하면_전체_통계와_원정_통계가_업데이트된다() {
        // given
        MatchStatistics matchStatistics = new MatchStatistics(1L);
        int rating = 6;

        // when
        matchStatistics.addNewReview(rating, FanType.AWAY);

        // then
        assertAll(
                () -> assertThat(matchStatistics.getTotalRatingSum()).isEqualTo(6L),
                () -> assertThat(matchStatistics.getTotalReviewCount()).isEqualTo(1),
                () -> assertThat(matchStatistics.getAwayFanRatingSum()).isEqualTo(6L),
                () -> assertThat(matchStatistics.getAwayFanReviewCount()).isEqualTo(1),
                () -> assertThat(matchStatistics.getHomeFanRatingSum()).isZero(),
                () -> assertThat(matchStatistics.getHomeFanReviewCount()).isZero()
        );
    }

    @Test
    void 중립_팬의_리뷰를_추가하면_전체_통계만_업데이트된다() {
        // given
        MatchStatistics matchStatistics = new MatchStatistics(1L);
        int rating = 7;

        // when
        matchStatistics.addNewReview(rating, null);

        // then
        assertAll(
                () -> assertThat(matchStatistics.getTotalRatingSum()).isEqualTo(7L),
                () -> assertThat(matchStatistics.getTotalReviewCount()).isEqualTo(1),
                () -> assertThat(matchStatistics.getHomeFanRatingSum()).isZero(),
                () -> assertThat(matchStatistics.getHomeFanReviewCount()).isZero(),
                () -> assertThat(matchStatistics.getAwayFanRatingSum()).isZero(),
                () -> assertThat(matchStatistics.getAwayFanReviewCount()).isZero()
        );
    }

    @Test
    void 홈_팬의_평점을_조정하면_전체_통계와_홈_통계가_반영된다() {
        // given
        MatchStatistics matchStatistics = new MatchStatistics(1L);
        matchStatistics.addNewReview(8, FanType.HOME);
        int ratingDelta = -2;

        // when
        matchStatistics.adjustRating(ratingDelta, FanType.HOME);

        // then
        assertAll(
                () -> assertThat(matchStatistics.getTotalRatingSum()).isEqualTo(6L),
                () -> assertThat(matchStatistics.getHomeFanRatingSum()).isEqualTo(6L),
                () -> assertThat(matchStatistics.getAwayFanRatingSum()).isZero()
        );
    }

    @Test
    void 원정_팬의_평점을_조정하면_전체_통계와_원정_통계가_반영된다() {
        // given
        MatchStatistics matchStatistics = new MatchStatistics(1L);
        matchStatistics.addNewReview(6, FanType.AWAY);
        int ratingDelta = 4;

        // when
        matchStatistics.adjustRating(ratingDelta, FanType.AWAY);

        // then
        assertAll(
                () -> assertThat(matchStatistics.getTotalRatingSum()).isEqualTo(10L),
                () -> assertThat(matchStatistics.getAwayFanRatingSum()).isEqualTo(10L),
                () -> assertThat(matchStatistics.getHomeFanRatingSum()).isZero()
        );
    }

    @Test
    void 리뷰가_없을_경우_평균은_0이다() {
        // given
        MatchStatistics matchStatistics = new MatchStatistics(1L);

        // when & then
        assertAll(
                () -> assertThat(matchStatistics.getTotalAverage()).isEqualTo(0.0),
                () -> assertThat(matchStatistics.getHomeAverage()).isEqualTo(0.0),
                () -> assertThat(matchStatistics.getAwayAverage()).isEqualTo(0.0)
        );
    }

    @Test
    void 평균_계산_시_5점_만점으로_변환된다() {
        // given
        MatchStatistics matchStatistics = new MatchStatistics(1L);
        matchStatistics.addNewReview(10, FanType.HOME);
        matchStatistics.addNewReview(6, FanType.HOME);
        // 합 16, 개수 2. (16/2)/2.0 = 4.0

        // when & then
        assertThat(matchStatistics.getTotalAverage()).isEqualTo(4.0);
    }

    @Test
    void 여러_유형의_팬이_리뷰를_남겨도_각각의_통계가_정확히_유지된다() {
        // given
        MatchStatistics matchStatistics = new MatchStatistics(1L);

        // when
        matchStatistics.addNewReview(10, FanType.HOME);
        matchStatistics.addNewReview(8, FanType.AWAY);
        matchStatistics.addNewReview(6, null);

        // then
        assertAll(
                () -> assertThat(matchStatistics.getTotalRatingSum()).isEqualTo(24L),
                () -> assertThat(matchStatistics.getTotalReviewCount()).isEqualTo(3),
                () -> assertThat(matchStatistics.getHomeFanRatingSum()).isEqualTo(10L),
                () -> assertThat(matchStatistics.getHomeFanReviewCount()).isEqualTo(1),
                () -> assertThat(matchStatistics.getAwayFanRatingSum()).isEqualTo(8L),
                () -> assertThat(matchStatistics.getAwayFanReviewCount()).isEqualTo(1),
                () -> assertThat(matchStatistics.getTotalAverage()).isEqualTo(4.0),
                () -> assertThat(matchStatistics.getHomeAverage()).isEqualTo(5.0),
                () -> assertThat(matchStatistics.getAwayAverage()).isEqualTo(4.0)
        );
    }

    @Test
    void 홈_팬의_리뷰를_삭제하면_전체_통계와_홈_통계가_업데이트된다() {
        // given
        MatchStatistics matchStatistics = new MatchStatistics(1L);
        matchStatistics.addNewReview(10, FanType.HOME);
        matchStatistics.addNewReview(8, FanType.AWAY);

        // when
        matchStatistics.removeRating(10, FanType.HOME);

        // then
        assertAll(
                () -> assertThat(matchStatistics.getTotalRatingSum()).isEqualTo(8L),
                () -> assertThat(matchStatistics.getTotalReviewCount()).isEqualTo(1),
                () -> assertThat(matchStatistics.getHomeFanRatingSum()).isZero(),
                () -> assertThat(matchStatistics.getHomeFanReviewCount()).isZero(),
                () -> assertThat(matchStatistics.getAwayFanRatingSum()).isEqualTo(8L),
                () -> assertThat(matchStatistics.getAwayFanReviewCount()).isEqualTo(1)
        );
    }

    @Test
    void 원정_팬의_리뷰를_삭제하면_전체_통계와_원정_통계가_업데이트된다() {
        // given
        MatchStatistics matchStatistics = new MatchStatistics(1L);
        matchStatistics.addNewReview(10, FanType.HOME);
        matchStatistics.addNewReview(8, FanType.AWAY);

        // when
        matchStatistics.removeRating(8, FanType.AWAY);

        // then
        assertAll(
                () -> assertThat(matchStatistics.getTotalRatingSum()).isEqualTo(10L),
                () -> assertThat(matchStatistics.getTotalReviewCount()).isEqualTo(1),
                () -> assertThat(matchStatistics.getHomeFanRatingSum()).isEqualTo(10L),
                () -> assertThat(matchStatistics.getHomeFanReviewCount()).isEqualTo(1),
                () -> assertThat(matchStatistics.getAwayFanRatingSum()).isZero(),
                () -> assertThat(matchStatistics.getAwayFanReviewCount()).isZero()
        );
    }

    @Test
    void 중립_팬의_리뷰를_삭제하면_전체_통계만_업데이트된다() {
        // given
        MatchStatistics matchStatistics = new MatchStatistics(1L);
        matchStatistics.addNewReview(10, FanType.HOME);
        matchStatistics.addNewReview(6, null);

        // when
        matchStatistics.removeRating(6, null);

        // then
        assertAll(
                () -> assertThat(matchStatistics.getTotalRatingSum()).isEqualTo(10L),
                () -> assertThat(matchStatistics.getTotalReviewCount()).isEqualTo(1),
                () -> assertThat(matchStatistics.getHomeFanRatingSum()).isEqualTo(10L),
                () -> assertThat(matchStatistics.getHomeFanReviewCount()).isEqualTo(1)
        );
    }

    @Test
    void 리뷰가_없는_상태에서_삭제를_시도해도_값이_음수가_되지_않는다() {
        // given
        MatchStatistics matchStatistics = new MatchStatistics(1L);

        // when
        matchStatistics.removeRating(10, FanType.HOME);

        // then
        assertAll(
                () -> assertThat(matchStatistics.getTotalRatingSum()).isZero(),
                () -> assertThat(matchStatistics.getTotalReviewCount()).isZero(),
                () -> assertThat(matchStatistics.getHomeFanRatingSum()).isZero(),
                () -> assertThat(matchStatistics.getHomeFanReviewCount()).isZero()
        );
    }

    @Test
    void 삭제하려는_평점이_현재_합계보다_커도_0으로_유지된다() {
        // given
        MatchStatistics matchStatistics = new MatchStatistics(1L);
        matchStatistics.addNewReview(5, FanType.HOME);

        // when
        matchStatistics.removeRating(10, FanType.HOME);

        // then
        assertAll(
                () -> assertThat(matchStatistics.getTotalRatingSum()).isZero(),
                () -> assertThat(matchStatistics.getTotalReviewCount()).isZero(),
                () -> assertThat(matchStatistics.getHomeFanRatingSum()).isZero(),
                () -> assertThat(matchStatistics.getHomeFanReviewCount()).isZero()
        );
    }
}
