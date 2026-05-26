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
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final long COOKIE_MAX_AGE = 60 * 60 * 24 * 7; // 일주일
    private final UserRepository userRepository;
    private final TokenIssuer tokenIssuer;

    @Value("${spring.security.oauth2.success-redirect-url}")
    private String successRedirectUrl;

    @Value("${spring.security.oauth2.signup-redirect-url}")
    private String signupRedirectUrl;

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

        String targetUrl;
        if (userOptional.isPresent()) {
            Tokens tokens = tokenIssuer.issueTokens(userOptional.get());

            response.addHeader(HttpHeaders.SET_COOKIE, createRefreshTokenCookie(tokens.refreshToken().getTokenValue(), COOKIE_MAX_AGE).toString());
            response.addHeader(HttpHeaders.SET_COOKIE, createAccessTokenCookie(tokens.accessToken()).toString());

            targetUrl = successRedirectUrl;
        } else {
            String signupToken = tokenIssuer.createSignupToken(email, provider.name(), providerKey);

            response.addHeader(HttpHeaders.SET_COOKIE, createSignupTokenCookie(signupToken).toString());

            targetUrl = signupRedirectUrl;
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private ResponseCookie createRefreshTokenCookie(String token, long maxAge) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(false) // 로컬 테스트를 위해 false
                .path("/")
                .maxAge(maxAge)
                .sameSite("Lax")
                .build();
    }

    private ResponseCookie createAccessTokenCookie(String token) {
        return ResponseCookie.from("accessToken", token)
                .path("/")
                .maxAge(60) // 1분만 유지
                .httpOnly(false)
                .secure(false) // 로컬 테스트를 위해 false
                .sameSite("Lax")
                .build();
    }

    private ResponseCookie createSignupTokenCookie(String token) {
        return ResponseCookie.from("signupToken", token)
                .path("/")
                .maxAge(60) // 1분만 유지
                .httpOnly(false)
                .secure(false) // 로컬 테스트를 위해 false
                .sameSite("Lax")
                .build();
    }
}
