package com.example.pitchboxd.match.playerStatistics.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PlayerMatchStatisticsTest {

    @Test
    void 선수_경기_통계_객체를_생성한다() {
        // given
        Long playerId = 1L;
        Long matchId = 2L;

        // when
        PlayerMatchStatistics statistics = new PlayerMatchStatistics(playerId, matchId);

        // then
        assertAll(
                () -> assertThat(statistics.getPlayerId()).isEqualTo(playerId),
                () -> assertThat(statistics.getMatchId()).isEqualTo(matchId),
                () -> assertThat(statistics.getTotalScore()).isZero(),
                () -> assertThat(statistics.getReviewCount()).isZero()
        );
    }

    @Test
    void 새로운_리뷰를_추가한다() {
        // given
        PlayerMatchStatistics statistics = new PlayerMatchStatistics(1L, 1L);

        // when
        statistics.addNewReview(10);

        // then
        assertAll(
                () -> assertThat(statistics.getTotalScore()).isEqualTo(10L),
                () -> assertThat(statistics.getReviewCount()).isEqualTo(1L)
        );
    }

    @Test
    void 리뷰가_없을_때_평균_평점은_0이다() {
        // given
        PlayerMatchStatistics statistics = new PlayerMatchStatistics(1L, 1L);

        // when
        double averageRating = statistics.getAverageRating();

        // then
        assertThat(averageRating).isEqualTo(0.0);
    }

    @Test
    void 평균_평점을_계산한다() {
        // given
        PlayerMatchStatistics statistics = new PlayerMatchStatistics(1L, 1L);
        statistics.addNewReview(10);
        statistics.addNewReview(8);

        // when
        double averageRating = statistics.getAverageRating();

        // then
        assertThat(averageRating).isEqualTo(4.5);
    }

    @Test
    void 평점을_수정한다() {
        // given
        PlayerMatchStatistics statistics = new PlayerMatchStatistics(1L, 1L);
        statistics.addNewReview(10);

        // when
        statistics.adjustRating(-2);

        // then
        assertAll(
                () -> assertThat(statistics.getTotalScore()).isEqualTo(8L),
                () -> assertThat(statistics.getAverageRating()).isEqualTo(4.0)
        );
    }

    @Test
    void 리뷰를_삭제한다() {
        // given
        PlayerMatchStatistics statistics = new PlayerMatchStatistics(1L, 1L);
        statistics.addNewReview(10);
        statistics.addNewReview(6);

        // when
        statistics.removeReview(6);

        // then
        assertAll(
                () -> assertThat(statistics.getTotalScore()).isEqualTo(10L),
                () -> assertThat(statistics.getReviewCount()).isEqualTo(1L)
        );
    }

    @Test
    void 리뷰_삭제_시_총합은_0보다_작을_수_없다() {
        // given
        PlayerMatchStatistics statistics = new PlayerMatchStatistics(1L, 1L);
        statistics.addNewReview(5);

        // when
        statistics.removeReview(10);

        // then
        assertAll(
                () -> assertThat(statistics.getTotalScore()).isZero(),
                () -> assertThat(statistics.getReviewCount()).isEqualTo(0L)
        );
    }
}
