package com.example.pitchboxd.auth.infrastructure;

import com.example.pitchboxd.auth.application.TokenManager;
import com.example.pitchboxd.auth.dto.TokenDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.ZoneId;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenManager implements TokenManager {

    private final long accessTokenExpirationTime;
    private final long refreshTokenExpirationTime;
    private final Key accessKey;
    private final Key refreshKey;

    public JwtTokenManager(
            @Value("${expiration.access-token-time}") long ACCESS_TOKEN_EXPIRATION_TIME,
            @Value("${expiration.refresh-token-time}") long REFRESH_TOKEN_EXPIRATION_TIME,
            @Value("${jwt.secret.access_key}") String ACCESS_SECRET_KEY,
            @Value("${jwt.secret.refresh_key}") String REFRESH_SECRET_KEY
    ) {
        this.accessTokenExpirationTime = ACCESS_TOKEN_EXPIRATION_TIME;
        this.refreshTokenExpirationTime = REFRESH_TOKEN_EXPIRATION_TIME;
        this.accessKey = Keys.hmacShaKeyFor(ACCESS_SECRET_KEY.getBytes());
        this.refreshKey = Keys.hmacShaKeyFor(REFRESH_SECRET_KEY.getBytes());
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
    public boolean validateAccessToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(accessKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean validateRefreshToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(refreshKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
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
}
