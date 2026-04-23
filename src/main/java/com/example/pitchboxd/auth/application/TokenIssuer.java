package com.example.pitchboxd.auth.application;

import com.example.pitchboxd.auth.dto.response.TokenResponse;
import com.example.pitchboxd.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TokenIssuer {

    private final TokenManager tokenManager;

    @Transactional
    public TokenResponse issueTokens(User user) {
        String accessToken = tokenManager.createAccessToken(user.getId(), user.getEmail());

        // 💡 힌트: 나중에 여기에 tokenManager.createRefreshToken() 과
        // refreshTokenRepository.save() 로직이 한 줄씩 추가될 것입니다.

        return new TokenResponse(accessToken); // 추후 Tokens(accessToken, refreshToken) 형태로 확장
    }
}
