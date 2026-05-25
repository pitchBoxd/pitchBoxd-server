package com.example.pitchboxd.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OAuthSignupRequest(
        @NotBlank(message = "임시 토큰은 필수입니다.")
        String signupToken,

        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 1, max = 20, message = "닉네임은 1자 이상 20자 이하로 입력해주세요.")
        String nickname,

        Long favoriteTeamId
) {
}
