package com.example.pitchboxd.auth.dto;

import com.example.pitchboxd.auth.domain.Tokens;
import com.example.pitchboxd.auth.dto.response.GoogleUserInfoResponse;

public record GoogleLoginResult(
        boolean isRegistered,
        String accessToken,      // 가입된 경우 존재
        String refreshToken,
        GoogleUserInfoResponse userInfo// 가입된 경우 존재 (쿠키용)
) {
    public static GoogleLoginResult registered(Tokens tokens) {
        return new GoogleLoginResult(true, tokens.accessToken(), tokens.refreshToken().getTokenValue(), null);
    }

    public static GoogleLoginResult newMember(GoogleUserInfoResponse userInfo) {
        return new GoogleLoginResult(false, null, null, userInfo);
    }
}
