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
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
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
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        String email = oidcUser.getEmail();
        String providerKey = oidcUser.getSubject();

        Optional<User> userOptional = userRepository.findByProviderAndProviderKey(Provider.GOOGLE, providerKey);

        String targetUrl;
        if (userOptional.isPresent()) {
            Tokens tokens = tokenIssuer.issueTokens(userOptional.get());

            ResponseCookie cookie = createRefreshTokenCookie(tokens.refreshToken().getTokenValue(), COOKIE_MAX_AGE);
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            targetUrl = UriComponentsBuilder.fromUriString(successRedirectUrl)
                    .queryParam("accessToken", tokens.accessToken())
                    .build().toUriString();
        } else {
            targetUrl = UriComponentsBuilder.fromUriString(signupRedirectUrl)
                    .queryParam("email", email)
                    .queryParam("providerKey", providerKey)
                    .build().toUriString();
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private ResponseCookie createRefreshTokenCookie(String token, long maxAge) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(maxAge)
                .sameSite("Lax")
                .build();
    }
}
