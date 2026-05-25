package com.example.pitchboxd.auth.application;

import com.example.pitchboxd.auth.dto.TokenDto;

public interface TokenManager {
    TokenDto createRefreshToken(Long userId, String email);

    String createAccessToken(Long userId, String email);

    String createSignupToken(String email, String provider, String providerKey);

    Long getUserIdFromToken(String token);

    void verifyAccessToken(String token);

    boolean validateRefreshToken(String token);

    String getEmailFromToken(String token);

    java.util.Map<String, String> parseSignupToken(String token);
}
