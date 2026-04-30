package com.example.pitchboxd.auth.dto.response;

public record GoogleLoginResponse(
        boolean isRegistered,
        String accessToken,
        String idToken
) {

    public static GoogleLoginResponse registered(String accessToken) {
        return new GoogleLoginResponse(true, accessToken, null);
    }

    public static GoogleLoginResponse newMember(String idToken) {
        return new GoogleLoginResponse(false, null, idToken);
    }
}
