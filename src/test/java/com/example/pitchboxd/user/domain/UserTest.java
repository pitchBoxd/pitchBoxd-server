package com.example.pitchboxd.user.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UserTest {

    @Test
    void 유저를_정상적으로_생성한다() {
        //when & then
        assertThatCode(() -> new User("닉네임", "abc@gmail.com", "1234!", 1L, Provider.GOOGLE))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "20자초과닉네임ㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋ"})
    void 잘못된_닉네임으로_유저_생성을_하면_예외가_발생한다(String nickname) {
        assertThatThrownBy(() -> new User(nickname, "abc@gmail.com", "1234!", 1L, Provider.GOOGLE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 유저_생성_시_기본_역할은_USER로_할당된다() {
        User user1 = new User("닉네임1", "email1@example.com", "password!");
        User user2 = new User("닉네임2", "email2@example.com", "password!", 1L);
        User user3 = new User("닉네임3", "email3@example.com", "password!", 1L, Provider.GOOGLE);
        User user4 = new User("닉네임4", "email4@example.com", "password!", 1L, Provider.GOOGLE, "providerKey");

        org.junit.jupiter.api.Assertions.assertAll(
            () -> org.assertj.core.api.Assertions.assertThat(user1.getRole()).isEqualTo(UserRole.USER),
            () -> org.assertj.core.api.Assertions.assertThat(user2.getRole()).isEqualTo(UserRole.USER),
            () -> org.assertj.core.api.Assertions.assertThat(user3.getRole()).isEqualTo(UserRole.USER),
            () -> org.assertj.core.api.Assertions.assertThat(user4.getRole()).isEqualTo(UserRole.USER)
        );
    }

}