package com.example.pitchboxd.player.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlayerTest {

    @DisplayName("선수 정보를 전체 수정한다.")
    @Test
    void update_all_fields() {
        // given
        Player player = new Player(1L, "기존 이름", "naverId1");
        Long newTeamId = 2L;
        String newName = "새 이름";
        String newNaverId = "naverId2";

        // when
        player.update(newTeamId, newName, newNaverId);

        // then
        assertAll(
                () -> assertThat(player.getTeamId()).isEqualTo(newTeamId),
                () -> assertThat(player.getName()).isEqualTo(newName),
                () -> assertThat(player.getNaverId()).isEqualTo(newNaverId)
        );
    }

    @DisplayName("선수 정보를 부분 수정한다.")
    @Test
    void update_partial_fields() {
        // given
        Player player = new Player(1L, "기존 이름", "naverId1");
        String newName = "새 이름";

        // when
        player.update(null, newName, null);

        // then
        assertAll(
                () -> assertThat(player.getTeamId()).isEqualTo(1L),
                () -> assertThat(player.getName()).isEqualTo(newName),
                () -> assertThat(player.getNaverId()).isEqualTo("naverId1")
        );
    }

    @DisplayName("수정할 정보가 모두 null이면 기존 정보를 유지한다.")
    @Test
    void update_with_all_null() {
        // given
        Player player = new Player(1L, "기존 이름", "naverId1");

        // when
        player.update(null, null, null);

        // then
        assertAll(
                () -> assertThat(player.getTeamId()).isEqualTo(1L),
                () -> assertThat(player.getName()).isEqualTo("기존 이름"),
                () -> assertThat(player.getNaverId()).isEqualTo("naverId1")
        );
    }
}
