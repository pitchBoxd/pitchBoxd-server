package com.example.pitchboxd.global.security;

import com.example.pitchboxd.auth.application.TokenIssuer;
import com.example.pitchboxd.auth.domain.Tokens;
import com.example.pitchboxd.user.domain.Provider;
import com.example.pitchboxd.user.domain.User;
import com.example.pitchboxd.user.infrastructure.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final long COOKIE_MAX_AGE = 60 * 60 * 24 * 7; // 일주일
    private final UserRepository userRepository;
    private final TokenIssuer tokenIssuer;
    private final Environment env;

    @Value("${spring.security.oauth2.success-redirect-url}")
    private String successRedirectUrl;

    @Value("${spring.security.oauth2.signup-redirect-url}")
    private String signupRedirectUrl;

    @Value("${expiration.access-token-time}")
    private long accessTokenExpirationTime;

    @Value("${expiration.signup-token-time}")
    private long signupTokenExpirationTime;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        OAuth2UserInfo userInfo = OAuth2UserFactory.get(registrationId, oAuth2User.getAttributes());

        String email = userInfo.getEmail();
        String providerKey = userInfo.getProviderId();
        Provider provider = userInfo.getProvider();

        Optional<User> userOptional = userRepository.findByProviderAndProviderKey(provider, providerKey);

        boolean secure = isSecure();
        String targetUrl;
        if (userOptional.isPresent()) {
            Tokens tokens = tokenIssuer.issueTokens(userOptional.get());

            response.addHeader(HttpHeaders.SET_COOKIE,
                    createRefreshTokenCookie(tokens.refreshToken().getTokenValue(), COOKIE_MAX_AGE, secure).toString());
            response.addHeader(HttpHeaders.SET_COOKIE,
                    createAccessTokenCookie(tokens.accessToken(), accessTokenExpirationTime / 1000, secure).toString());

            targetUrl = successRedirectUrl;
        } else {
            String signupToken = tokenIssuer.createSignupToken(email, provider.name(), providerKey);

            response.addHeader(HttpHeaders.SET_COOKIE,
                    createSignupTokenCookie(signupToken, signupTokenExpirationTime / 1000, secure).toString());

            targetUrl = signupRedirectUrl;
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private boolean isSecure() {
        return !java.util.Arrays.asList(env.getActiveProfiles()).contains("local") &&
                !java.util.Arrays.asList(env.getActiveProfiles()).contains("test");
    }

    private ResponseCookie createRefreshTokenCookie(String token, long maxAge, boolean secure) {
        return ResponseCookie.from("refreshToken", token)
                .domain("pitchboxd.site")
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(maxAge)
                .sameSite("Lax")
                .build();
    }

    private ResponseCookie createAccessTokenCookie(String token, long maxAge, boolean secure) {
        return ResponseCookie.from("accessToken", token)
                .domain("pitchboxd.site")
                .path("/")
                .maxAge(maxAge)
                .httpOnly(false)
                .secure(secure)
                .sameSite("Lax")
                .build();
    }

    private ResponseCookie createSignupTokenCookie(String token, long maxAge, boolean secure) {
        return ResponseCookie.from("signupToken", token)
                .domain("pitchboxd.site")
                .path("/")
                .maxAge(maxAge)
                .httpOnly(false)
                .secure(secure)
                .sameSite("Lax")
                .build();
    }
}
