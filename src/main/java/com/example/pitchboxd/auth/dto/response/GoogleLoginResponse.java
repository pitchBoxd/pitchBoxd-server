package com.example.pitchboxd.auth.dto.response;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

public record GoogleLoginResponse(
        boolean isRegistered,
        String accessToken,
        GoogleIdToken idToken
) {

    public static GoogleLoginResponse registered(String accessToken) {
        return new GoogleLoginResponse(true, accessToken, null);
    }

    public static GoogleLoginResponse newMember(GoogleIdToken idToken) {
        return new GoogleLoginResponse(false, null, idToken);
    }
}
