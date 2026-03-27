package com.example.pitchboxd.auth.presentation;

import com.example.pitchboxd.auth.application.AuthService;
import com.example.pitchboxd.auth.dto.request.LoginRequest;
import com.example.pitchboxd.auth.dto.response.TokenResponse;
import com.example.pitchboxd.global.dto.response.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        TokenResponse response = authService.login(request);
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse<TokenResponse>> logout() {
        // 현재는 Redis가 없으므로 별도의 비즈니스 로직 없이 성공 응답만 반환
        // 나중에 로그를 남기거나, 리프레시 토큰을 DB에서 지우는 로직이 추가될 수 있음

        HttpStatus status = HttpStatus.NO_CONTENT;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }
}
