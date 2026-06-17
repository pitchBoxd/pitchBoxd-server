package com.example.pitchboxd.match.playerReview.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.match.matchStatistics.domain.FanType;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PlayerReviewTest {

    @Test
    void 한줄평을_정상적으로_생성한다() {
        // given
        Long matchId = 1L;
        Long playerId = 2L;
        Long userId = 3L;
        Integer point = 5;
        String content = "오늘 경기 정말 멋졌습니다!";

        // when
        PlayerReview playerReview = new PlayerReview(matchId, playerId, userId, point, content);

        // then
        assertAll(
                () -> assertThat(playerReview.getMatchId()).isEqualTo(matchId),
                () -> assertThat(playerReview.getPlayerId()).isEqualTo(playerId),
                () -> assertThat(playerReview.getUserId()).isEqualTo(userId),
                () -> assertThat(playerReview.getPoint()).isEqualTo(point),
                () -> assertThat(playerReview.getContent()).isEqualTo(content),
                () -> assertThat(playerReview.getLikeCount()).isEqualTo(0L)
        );
    }

    @Test
    void 한줄평_내용이_100자를_초과하면_예외가_발생한다() {
        // given
        String longContent = "a".repeat(101);

        // when & then
        assertThatThrownBy(() -> new PlayerReview(1L, 1L, 1L, 5, longContent))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("한줄평은 100자를 넘길 수 없습니다.");
    }

    @Test
    void 한줄평_내용이_딱_100자인_경우_정상적으로_생성된다() {
        // given
        String boundaryContent = "a".repeat(100);

        // when
        PlayerReview playerReview = new PlayerReview(1L, 1L, 1L, 5, boundaryContent);

        // then
        assertThat(playerReview.getContent()).isEqualTo(boundaryContent);
    }

    @Test
    void 포인트가_0_미만인_경우_예외가_발생한다() {
        // given
        Integer invalidPoint = -1;

        // when & then
        assertThatThrownBy(() -> new PlayerReview(1L, 1L, 1L, invalidPoint, "내용"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 포인트가_11_이상인_경우_예외가_발생한다() {
        // given
        Integer invalidPoint = 11;

        // when & then
        assertThatThrownBy(() -> new PlayerReview(1L, 1L, 1L, invalidPoint, "내용"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 좋아요_개수를_1_증가시킨다() {
        // given
        PlayerReview playerReview = new PlayerReview(1L, 1L, 1L, 5, "내용");

        // when
        playerReview.addOneLikeCount();

        // then
        assertThat(playerReview.getLikeCount()).isEqualTo(1L);
    }

    @Test
    void 좋아요_개수를_1_감소시킨다() {
        // given
        PlayerReview playerReview = new PlayerReview(1L, 1L, 1L, 5, "내용");
        playerReview.addOneLikeCount();

        // when
        playerReview.minusOneLikeCount();

        // then
        assertThat(playerReview.getLikeCount()).isEqualTo(0L);
    }

    @Test
    void 좋아요_개수가_0인_상태에서_감소시켜도_0_미만으로_내려가지_않는다() {
        // given
        PlayerReview playerReview = new PlayerReview(1L, 1L, 1L, 5, "내용");

        // when
        playerReview.minusOneLikeCount();

        // then
        assertThat(playerReview.getLikeCount()).isEqualTo(0L);
    }

    @Test
    void 선수_리뷰의_소유자를_확인한다() {
        // given
        Long ownerId = 1L;
        Long otherId = 9999L;
        PlayerReview playerReview = new PlayerReview(1L, 1L, ownerId, 5, "내용");

        // when & then
        assertAll(
                () -> assertThat(playerReview.isOwner(ownerId)).isTrue(),
                () -> assertThat(playerReview.isOwner(otherId)).isFalse()
        );
    }

    @Test
    void 선수_리뷰를_수정한다() {
        // given
        String afterContent = "바뀔 내용";
        int afterPoint = 10;
        PlayerReview playerReview = new PlayerReview(1L, 1L, 1L, 5, "내용");

        // when
        playerReview.update(afterContent, afterPoint);

        // then
        assertAll(
                () -> assertThat(playerReview.getContent()).isEqualTo(afterContent),
                () -> assertThat(playerReview.getPoint()).isEqualTo(afterPoint),
                () -> assertThat(playerReview.getUpdatedAt()).isNotNull()
        );
    }

    @Test
    void 선수_리뷰_수정_시_잘못된_내용으로_수정할_수_없다() {
        // given
        String wrongContent = "a".repeat(101);
        PlayerReview playerReview = new PlayerReview(1L, 1L, 1L, 5, "내용");

        // when & then
        assertThatThrownBy(() -> playerReview.update(wrongContent, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 선수_리뷰_수정_시_잘못된_점수로_수정할_수_없다() {
        // given
        int wrongPoint = 11;
        PlayerReview playerReview = new PlayerReview(1L, 1L, 1L, 5, "내용");

        // when & then
        assertThatThrownBy(() -> playerReview.update("zz", wrongPoint))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void isUpdated_ShouldReturnTrue_WhenUpdatedAtIsNotNull() {
        PlayerReview playerReview = new PlayerReview(1L, 1L, 1L, 8, "Great match!", FanType.HOME);
        playerReview.update("Awesome match!", 9);
        
        assertThat(playerReview.isUpdated()).isTrue();
    }

    @Test
    void isUpdated_ShouldReturnFalse_WhenUpdatedAtIsNull() {
        PlayerReview playerReview = new PlayerReview(1L, 1L, 1L, 8, "Great match!", FanType.HOME);
        
        assertThat(playerReview.isUpdated()).isFalse();
    }
}
