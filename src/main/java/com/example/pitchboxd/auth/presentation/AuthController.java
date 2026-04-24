package com.example.pitchboxd.auth.presentation;

import com.example.pitchboxd.auth.application.AuthService;
import com.example.pitchboxd.auth.application.GoogleAuthService;
import com.example.pitchboxd.auth.domain.Tokens;
import com.example.pitchboxd.auth.dto.GoogleLoginResult;
import com.example.pitchboxd.auth.dto.request.GoogleLoginRequest;
import com.example.pitchboxd.auth.dto.request.GoogleSignupRequest;
import com.example.pitchboxd.auth.dto.request.LoginRequest;
import com.example.pitchboxd.auth.dto.response.GoogleLoginResponse;
import com.example.pitchboxd.auth.dto.response.TokenResponse;
import com.example.pitchboxd.global.dto.response.SuccessResponse;
import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final long COOKIE_MAX_AGE = 604800;

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;

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

    @PostMapping("/google/login")
    public ResponseEntity<SuccessResponse<GoogleLoginResponse>> googleLogin(
            @Valid @RequestBody GoogleLoginRequest request) {

        GoogleLoginResult result = googleAuthService.googleLogin(request);
        HttpStatus status = HttpStatus.OK;

        if (result.isRegistered()) {
            GoogleLoginResponse response = GoogleLoginResponse.registered(result.accessToken());
            ResponseCookie cookie = createRefreshTokenCookie(result.refreshToken(), COOKIE_MAX_AGE);

            return ResponseEntity.status(status)
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(SuccessResponse.of(status, response));
        }

        GoogleLoginResponse response = GoogleLoginResponse.newMember(result.userInfo(), result.idToken());
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @PostMapping("/google/signup")
    public ResponseEntity<SuccessResponse<TokenResponse>> googleSignup(
            @Valid @RequestBody GoogleSignupRequest request) {
        Tokens tokens = googleAuthService.googleSignup(request);
        ResponseCookie cookie = createRefreshTokenCookie(tokens.refreshToken().getTokenValue(), COOKIE_MAX_AGE);

        TokenResponse response = new TokenResponse(tokens.accessToken());
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(SuccessResponse.of(status, response));
    }

    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse<TokenResponse>> logout() {
        // 현재는 Redis가 없으므로 별도의 비즈니스 로직 없이 성공 응답만 반환
        // 나중에 로그를 남기거나, 리프레시 토큰을 DB에서 지우는 로직이 추가될 수 있음

        ResponseCookie deleteCookie = createRefreshTokenCookie("", 0);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body(SuccessResponse.of(HttpStatus.OK, null));
    }

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
                .secure(true)
                .path("/")
                .maxAge(maxAge)
                .sameSite("Lax")
                .build();
    }
}
