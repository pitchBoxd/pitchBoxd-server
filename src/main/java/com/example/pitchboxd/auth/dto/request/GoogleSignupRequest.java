package com.example.pitchboxd.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GoogleSignupRequest(
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @NotBlank(message = "제공자 키는 필수입니다.")
        String providerKey,

        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 1, max = 20, message = "닉네임은 1자 이상 20자 이하로 입력해주세요.")
        String nickname,

        Long favoriteTeamId
) {
}
