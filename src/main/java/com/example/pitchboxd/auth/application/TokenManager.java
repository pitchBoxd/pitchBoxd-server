package com.example.pitchboxd.auth.application;

public interface TokenManager {
    String createRefreshToken(Long userId, String email);

    String createAccessToken(Long userId, String email);

    Long getUserIdFromToken(String token);

    boolean validateToken(String token);

    String getEmailFromToken(String token);
}
