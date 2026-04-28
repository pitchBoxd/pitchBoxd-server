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

}