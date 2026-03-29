package com.example.pitchboxd.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GoogleSignupRequest(
        @NotBlank(message = "구글 토큰은 필수입니다.")
        String idToken,

        @NotBlank(message = "닉네임은 필수입니다.")
        String nickname,
        
        Long favoriteTeamId
) {
}
