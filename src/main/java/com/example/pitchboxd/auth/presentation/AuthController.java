package com.example.pitchboxd.auth.presentation;

import com.example.pitchboxd.auth.application.AuthService;
import com.example.pitchboxd.auth.application.OAuthService;
import com.example.pitchboxd.auth.domain.Tokens;
import com.example.pitchboxd.auth.dto.request.LoginRequest;
import com.example.pitchboxd.auth.dto.request.OAuthSignupRequest;
import com.example.pitchboxd.auth.dto.response.TokenResponse;
import com.example.pitchboxd.global.dto.response.SuccessResponse;
import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth API", description = "Auth API 명세")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final long COOKIE_MAX_AGE = 60 * 60 * 24 * 7; // 일주일

    private final AuthService authService;
    private final OAuthService oAuthService;

    @Deprecated
    @Operation(summary = "로컬 로그인", description = "서비스 자체 로그인 서비스입니다.")
    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        Tokens tokens = authService.login(request);
        ResponseCookie cookie = createRefreshTokenCookie(tokens.refreshToken().getTokenValue(), COOKIE_MAX_AGE);

        TokenResponse response = new TokenResponse(tokens.accessToken());
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(SuccessResponse.of(status, response));
    }

    @Operation(summary = "소셜 회원가입", description = "소셜 회원가입입니다.")
    @PostMapping("/oauth/signup")
    public ResponseEntity<SuccessResponse<TokenResponse>> oAuthSignup(
            @Valid @RequestBody OAuthSignupRequest request) {
        Tokens tokens = oAuthService.signup(request);
        ResponseCookie cookie = createRefreshTokenCookie(tokens.refreshToken().getTokenValue(), COOKIE_MAX_AGE);

        TokenResponse response = new TokenResponse(tokens.accessToken());
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(SuccessResponse.of(status, response));
    }

    @Operation(summary = "로그아웃", description = "로그아웃입니다.")
    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse<Void>> logout(@LoginUserId Long userId) {
        authService.logout(userId);

        ResponseCookie deleteRefreshTokenCookie = createRefreshTokenCookie("", 0);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteRefreshTokenCookie.toString())
                .body(SuccessResponse.of(HttpStatus.OK, null));
    }

    @Operation(summary = "엑세스 토큰 재발급", description = "리프레시 토큰이 유효할 때, 엑세스 토큰과 리프레시 토큰을 재발급합니다.")
    @PostMapping("/reissue")
    public ResponseEntity<SuccessResponse<TokenResponse>> reissue(
            @CookieValue(value = "refreshToken", required = false) String refreshTokenCookie
    ) {

        if (refreshTokenCookie == null || refreshTokenCookie.isBlank()) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_MISSING);
        }

        Tokens tokens = authService.reissue(refreshTokenCookie);
        ResponseCookie cookie = createRefreshTokenCookie(tokens.refreshToken().getTokenValue(), COOKIE_MAX_AGE);
        TokenResponse response = new TokenResponse(tokens.accessToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(SuccessResponse.of(HttpStatus.OK, response));
    }

    private ResponseCookie createRefreshTokenCookie(String token, long maxAge) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(maxAge)
                .sameSite("Lax")
                .build();
    }
}
