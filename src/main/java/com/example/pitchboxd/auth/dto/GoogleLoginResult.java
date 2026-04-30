package com.example.pitchboxd.auth.dto;

import com.example.pitchboxd.auth.domain.Tokens;

public record GoogleLoginResult(
        boolean isRegistered,
        String accessToken,
        String refreshToken,
        String idToken
) {
    public static GoogleLoginResult registered(Tokens tokens) {
        return new GoogleLoginResult(true, tokens.accessToken(), tokens.refreshToken().getTokenValue(), null);
    }

    public static GoogleLoginResult newMember(String idToken) {
        return new GoogleLoginResult(false, null, null, idToken);
    }
}
