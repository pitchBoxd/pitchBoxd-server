package com.example.pitchboxd.auth.dto;

import com.example.pitchboxd.auth.domain.Tokens;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

public record GoogleLoginResult(
        boolean isRegistered,
        String accessToken,
        String refreshToken,
        GoogleIdToken idToken
) {
    public static GoogleLoginResult registered(Tokens tokens) {
        return new GoogleLoginResult(true, tokens.accessToken(), tokens.refreshToken().getTokenValue(), null);
    }

    public static GoogleLoginResult newMember(GoogleIdToken idToken) {
        return new GoogleLoginResult(false, null, null, idToken);
    }
}
