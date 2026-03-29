package com.example.pitchboxd.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
        @NotBlank(message = "구글 토큰은 필수입니다.")
        String idToken
) {
}
