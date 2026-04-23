package com.example.pitchboxd.auth.dto.response;

public record GoogleLoginResponse(
        boolean isRegistered,
        String accessToken,
        GoogleUserInfoResponse userInfo,
        String idToken
) {

    public static GoogleLoginResponse registered(String accessToken) {
        return new GoogleLoginResponse(true, accessToken, null, null);
    }

    public static GoogleLoginResponse newMember(GoogleUserInfoResponse userInfo, String idToken) {
        return new GoogleLoginResponse(false, null, userInfo, idToken);
    }
}
