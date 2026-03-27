package com.example.pitchboxd.matchReview.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class MatchReviewTest {

    @Test
    void 경기_리뷰를_성공적으로_생성한다() {
        // given
        Long matchId = 1L;
        Long userId = 1L;
        Integer point = 5;
        String content = "양 팀 모두 훌륭한 경기력을 보여주었습니다.";

        // when
        MatchReview matchReview = new MatchReview(matchId, userId, point, content);

        // then
        assertAll(
                () -> assertThat(matchReview.getMatchId()).isEqualTo(matchId),
                () -> assertThat(matchReview.getUserId()).isEqualTo(userId),
                () -> assertThat(matchReview.getPoint()).isEqualTo(point),
                () -> assertThat(matchReview.getContent()).isEqualTo(content),
                () -> assertThat(matchReview.getLikeCount()).isEqualTo(0L)
        );
    }

    @Test
    void 한줄평이_100자를_초과하는_경우_예외가_발생한다() {
        // given
        String longContent = "a".repeat(101);

        // when & then
        assertThatThrownBy(() -> new MatchReview(1L, 1L, 5, longContent))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("한줄평은 100자를 넘길 수 없습니다.");
    }

    @Test
    void 포인트가_0_미만인_경우_예외가_발생한다() {
        // given
        Integer point = -1;

        // when & then
        assertThatThrownBy(() -> new MatchReview(1L, 1L, point, "재미있어요"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("포인트는 0 이상, 10 이하이어야 합니다.");
    }

    @Test
    void 포인트가_10_이상인_경우_예외가_발생한다() {
        // given
        Integer point = 11;

        // when & then
        assertThatThrownBy(() -> new MatchReview(1L, 1L, point, "재미있어요"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("포인트는 0 이상, 10 이하이어야 합니다.");
    }

    @Test
    void 포인트가_0인_경우_정상적으로_생성된다() {
        // given
        Integer point = 0;

        // when
        MatchReview matchReview = new MatchReview(1L, 1L, point, "아쉬운 경기");

        // then
        assertThat(matchReview.getPoint()).isEqualTo(point);
    }
}
