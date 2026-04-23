package com.example.pitchboxd.auth.application;

import com.example.pitchboxd.auth.domain.RefreshToken;
import com.example.pitchboxd.auth.domain.Tokens;
import com.example.pitchboxd.auth.dto.request.LoginRequest;
import com.example.pitchboxd.auth.infrastructure.RefreshTokenRepository;
import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.user.domain.User;
import com.example.pitchboxd.user.infrastructure.UserRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenIssuer tokenIssuer;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenManager tokenManager;

    public Tokens login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        return tokenIssuer.issueTokens(user);
    }

    @Transactional
    public Tokens reissue(String refreshTokenValue) {
        if (!tokenManager.validateRefreshToken(refreshTokenValue)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        RefreshToken oldRefreshToken = refreshTokenRepository.findByTokenValue(refreshTokenValue)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (oldRefreshToken.isExpired(LocalDateTime.now())) {
            refreshTokenRepository.delete(oldRefreshToken);

            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }

        return tokenIssuer.reissueToken(oldRefreshToken);
    }
}
