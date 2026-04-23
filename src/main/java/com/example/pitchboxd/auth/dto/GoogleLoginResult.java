package com.example.pitchboxd.auth.dto;

import com.example.pitchboxd.auth.domain.Tokens;
import com.example.pitchboxd.auth.dto.response.GoogleUserInfoResponse;

public record GoogleLoginResult(
        boolean isRegistered,
        String accessToken,
        String refreshToken,
        GoogleUserInfoResponse userInfo,
        String idToken
) {
    public static GoogleLoginResult registered(Tokens tokens) {
        return new GoogleLoginResult(true, tokens.accessToken(), tokens.refreshToken().getTokenValue(), null, null);
    }

    public static GoogleLoginResult newMember(GoogleUserInfoResponse userInfo, String idToken) {
        return new GoogleLoginResult(false, null, null, userInfo, idToken);
    }
}
