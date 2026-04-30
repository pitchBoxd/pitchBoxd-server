package com.example.pitchboxd.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
        @NotBlank(message = "구글 코드는 필수입니다.")
        String googleIdToken
) {
}
