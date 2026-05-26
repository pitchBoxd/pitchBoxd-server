package com.example.pitchboxd.auth.application;

import com.example.pitchboxd.auth.domain.RefreshToken;
import com.example.pitchboxd.auth.domain.Tokens;
import com.example.pitchboxd.auth.dto.TokenDto;
import com.example.pitchboxd.auth.infrastructure.RefreshTokenRepository;
import com.example.pitchboxd.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TokenIssuer {

    private final TokenManager tokenManager;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public Tokens issueTokens(User user) {
        String accessToken = tokenManager.createAccessToken(user.getId(), user.getEmail());

        TokenDto refreshTokenInfo = tokenManager.createRefreshToken(user.getId(), user.getEmail());

        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .map(existingToken -> {
                    existingToken.renew(refreshTokenInfo.tokenValue(), refreshTokenInfo.issuedAt(),
                            refreshTokenInfo.expiredAt());
                    return existingToken;
                })
                .orElseGet(() -> new RefreshToken(
                        refreshTokenInfo.tokenValue(),
                        user,
                        refreshTokenInfo.issuedAt(),
                        refreshTokenInfo.expiredAt()));

        refreshTokenRepository.save(refreshToken);

        return new Tokens(accessToken, refreshToken);
    }

    @Transactional
    public Tokens reissueToken(RefreshToken refreshToken) {
        User user = refreshToken.getUser();
        String accessToken = tokenManager.createAccessToken(user.getId(), user.getEmail());
        TokenDto refreshTokenInfo = tokenManager.createRefreshToken(user.getId(), user.getEmail());

        refreshToken.renew(refreshTokenInfo.tokenValue(), refreshTokenInfo.issuedAt(), refreshTokenInfo.expiredAt());
        refreshTokenRepository.save(refreshToken);

        return new Tokens(accessToken, refreshToken);
    }

    public String createSignupToken(String email, String provider, String providerKey) {
        return tokenManager.createSignupToken(email, provider, providerKey);
    }
}
