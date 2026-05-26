package com.example.pitchboxd.auth.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.pitchboxd.auth.domain.Tokens;
import com.example.pitchboxd.auth.infrastructure.RefreshTokenRepository;
import com.example.pitchboxd.support.DatabaseCleaner;
import com.example.pitchboxd.user.domain.User;
import com.example.pitchboxd.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TokenIssuer tokenIssuer;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    private User user;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();
        user = new User("nickname", "test@example.com", "password");
        userRepository.save(user);
    }

    @Test
    void 로그아웃_시_리프레시_토큰이_삭제된다() {
        // given
        Tokens tokens = tokenIssuer.issueTokens(user);
        String refreshTokenValue = tokens.refreshToken().getTokenValue();
        assertThat(refreshTokenRepository.findByUser(user)).isPresent();

        // when
        authService.logout(refreshTokenValue);

        // then
        assertThat(refreshTokenRepository.findByUser(user)).isEmpty();
    }
}
