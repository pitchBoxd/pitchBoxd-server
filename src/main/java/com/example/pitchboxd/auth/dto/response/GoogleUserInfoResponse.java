package com.example.pitchboxd.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleUserInfoResponse(
        @JsonProperty("sub")
        String socialId,
        @JsonProperty("email")
        String email,
        @JsonProperty("name")
        String nickname,
        @JsonProperty("picture")
        String profileImageUrl
) {
}
