package com.example.pitchboxd.support;

import com.example.pitchboxd.auth.application.TokenManager;
import com.example.pitchboxd.auth.dto.TokenDto;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

public class FakeTokenManager implements TokenManager {

    private final Key accessKey = Keys.hmacShaKeyFor(
            "test-access-secret-key-for-testing-purposes-only-32bytes-long".getBytes());
    private final Key refreshKey = Keys.hmacShaKeyFor(
            "test-refresh-secret-key-for-testing-purposes-only-32bytes-long".getBytes());

    @Override
    public TokenDto createRefreshToken(Long userId, String email) {
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiredAt = new Date(now + 1000 * 60 * 60);

        String token = Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .setIssuedAt(issuedAt)
                .setExpiration(expiredAt)
                .signWith(refreshKey, SignatureAlgorithm.HS256)
                .compact();

        return new TokenDto(
                token,
                issuedAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                expiredAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
        );
    }

    @Override
    public String createAccessToken(Long userId, String email) {
        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(accessKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createExpiredAccessToken(Long userId, String email) {
        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24))
                .setExpiration(new Date(System.currentTimeMillis() - 1000 * 60 * 60))
                .signWith(accessKey, SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public Long getUserIdFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(accessKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("userId", Long.class);
    }

    @Override
    public void verifyAccessToken(String token) {
        Jwts.parserBuilder()
                .setSigningKey(accessKey)
                .build()
                .parseClaimsJws(token);
    }

    @Override
    public boolean validateRefreshToken(String token) {
        Jwts.parserBuilder()
                .setSigningKey(refreshKey)
                .build()
                .parseClaimsJws(token);
        return true;
    }

    @Override
    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(accessKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    @Override
    public String createSignupToken(String email, String provider, String providerKey) {
        return Jwts.builder()
                .setSubject(email)
                .claim("provider", provider)
                .claim("providerKey", providerKey)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 15))
                .signWith(accessKey, SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public Map<String, String> parseSignupToken(String token) {
        var claims = Jwts.parserBuilder()
                .setSigningKey(accessKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return Map.of(
                "email", claims.getSubject(),
                "provider", claims.get("provider", String.class),
                "providerKey", claims.get("providerKey", String.class)
        );
    }
}
