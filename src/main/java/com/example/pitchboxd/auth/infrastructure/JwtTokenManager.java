package com.example.pitchboxd.auth.infrastructure;

import com.example.pitchboxd.auth.application.TokenManager;
import com.example.pitchboxd.auth.dto.TokenDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenManager implements TokenManager {

    private final long accessTokenExpirationTime;
    private final long refreshTokenExpirationTime;
    private final long signupTokenExpirationTime;
    private final Key accessKey;
    private final Key refreshKey;
    private final Key signupKey;

    public JwtTokenManager(
            @Value("${expiration.access-token-time}") long ACCESS_TOKEN_EXPIRATION_TIME,
            @Value("${expiration.refresh-token-time}") long REFRESH_TOKEN_EXPIRATION_TIME,
            @Value("${expiration.signup-token-time}") long SIGNUP_TOKEN_EXPIRATION_TIME,
            @Value("${jwt.secret.access_key}") String ACCESS_SECRET_KEY,
            @Value("${jwt.secret.refresh_key}") String REFRESH_SECRET_KEY,
            @Value("${jwt.secret.signup_key}") String SIGNUP_SECRET_KEY
    ) {
        this.accessTokenExpirationTime = ACCESS_TOKEN_EXPIRATION_TIME;
        this.refreshTokenExpirationTime = REFRESH_TOKEN_EXPIRATION_TIME;
        this.signupTokenExpirationTime = SIGNUP_TOKEN_EXPIRATION_TIME;
        this.accessKey = Keys.hmacShaKeyFor(ACCESS_SECRET_KEY.getBytes());
        this.refreshKey = Keys.hmacShaKeyFor(REFRESH_SECRET_KEY.getBytes());
        this.signupKey = Keys.hmacShaKeyFor(SIGNUP_SECRET_KEY.getBytes());
    }

    @Override
    public TokenDto createRefreshToken(Long userId, String email) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("userId", userId);

        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiredAt = new Date(now + refreshTokenExpirationTime);

        // 1. 토큰 생성
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
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("userId", userId);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpirationTime))
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
        try {
            Jwts.parserBuilder()
                    .setSigningKey(refreshKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
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
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("provider", provider);
        claims.put("providerKey", providerKey);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + signupTokenExpirationTime))
                .signWith(signupKey, SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public Map<String, String> parseSignupToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signupKey)
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
