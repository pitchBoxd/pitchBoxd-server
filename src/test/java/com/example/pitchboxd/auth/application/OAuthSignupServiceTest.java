package com.example.pitchboxd.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.pitchboxd.auth.domain.Tokens;
import com.example.pitchboxd.auth.dto.request.OAuthSignupRequest;
import com.example.pitchboxd.support.DatabaseCleaner;
import com.example.pitchboxd.user.domain.Provider;
import com.example.pitchboxd.user.domain.User;
import com.example.pitchboxd.user.infrastructure.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class OAuthSignupServiceTest {

    @Autowired
    private OAuthService oAuthService;

    @Autowired
    private TokenManager tokenManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();
    }

    @Test
    void 임시_토큰을_이용해_회원가입을_완료한다() {
        // given
        String email = "test@example.com";
        String provider = Provider.GOOGLE.name();
        String providerKey = "google-sub-123";
        String signupToken = tokenManager.createSignupToken(email, provider, providerKey);

        OAuthSignupRequest request = new OAuthSignupRequest(signupToken, "newNickname", 1L);

        // when
        Tokens tokens = oAuthService.signup(request);

        // then
        User savedUser = userRepository.findByEmail(email).orElseThrow();
        assertAll(
                () -> assertThat(tokens.accessToken()).isNotBlank(),
                () -> assertThat(tokens.refreshToken()).isNotNull(),
                () -> assertThat(savedUser.getNickname()).isEqualTo("newNickname"),
                () -> assertThat(savedUser.getProvider()).isEqualTo(Provider.GOOGLE),
                () -> assertThat(savedUser.getProviderKey()).isEqualTo(providerKey)
        );
    }

    @Test
    void 만료된_임시_토큰으로_회원가입을_시도하면_예외가_발생한다() {
        // given
        String email = "expired@example.com";
        String provider = Provider.GOOGLE.name();
        String providerKey = "google-sub-expired";

        // application-test.yaml의 jwt.secret.signup_key와 동일한 키 사용
        String expiredSignupToken = Jwts.builder()
                .setSubject(email)
                .claim("provider", provider)
                .claim("providerKey", providerKey)
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 30))
                .setExpiration(new Date(System.currentTimeMillis() - 1000 * 60 * 15))
                .signWith(Keys.hmacShaKeyFor(
                                "test-signup-secret-key-at-least-32-characters".getBytes()),
                        SignatureAlgorithm.HS256)
                .compact();

        OAuthSignupRequest request = new OAuthSignupRequest(expiredSignupToken, "nickname", 1L);

        // when & then
        assertThrows(ExpiredJwtException.class, () -> oAuthService.signup(request));
    }
}
