package com.example.pitchboxd.auth.dto.response;

public record GoogleLoginResponse(
        boolean isRegistered,
        String accessToken,
        GoogleUserInfoResponse userInfo
) {

    public static GoogleLoginResponse registered(String accessToken) {
        return new GoogleLoginResponse(true, accessToken, null);
    }

    public static GoogleLoginResponse newMember(GoogleUserInfoResponse userInfo) {
        return new GoogleLoginResponse(false, null, userInfo);
    }
}
