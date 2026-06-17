package com.example.pitchboxd.user.presentation;

import com.example.pitchboxd.auth.presentation.LoginUserId;
import com.example.pitchboxd.global.dto.response.SuccessResponse;
import com.example.pitchboxd.user.application.UserService;
import com.example.pitchboxd.user.dto.request.UserCreateRequest;
import com.example.pitchboxd.user.dto.response.EmailAvailabilityResponse;
import com.example.pitchboxd.user.dto.response.NicknameAvailabilityResponse;
import com.example.pitchboxd.user.dto.response.UserCreateResponse;
import com.example.pitchboxd.user.dto.response.UserResponse;
import com.example.pitchboxd.global.logging.LoggingExclude;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User API", description = "User API 명세")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "본인 정보 가져오기", description = "유저 자신의 정보를 가져옵니다.")
    @GetMapping("/me")
    public ResponseEntity<SuccessResponse<UserResponse>> getMyInfo(@LoginUserId Long userId) {
        UserResponse response = userService.getUserInfo(userId);
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @Operation(summary = "이메일 중복검사", description = "이미 회원가입된 이메일인지 확인합니다.")
    @GetMapping("/email/exists")
    public ResponseEntity<SuccessResponse<EmailAvailabilityResponse>> checkEmailDuplicate(@RequestParam String email) {
        EmailAvailabilityResponse response = userService.isEmailDuplicated(email);
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @Operation(summary = "닉네임 중복검사", description = "이미 회원가입된 닉네임인지 확인합니다.")
    @GetMapping("/nickname/exist")
    public ResponseEntity<SuccessResponse<NicknameAvailabilityResponse>> checkNicknameDuplicate(
            @RequestParam String nickname) {
        NicknameAvailabilityResponse response = userService.isNicknameDuplicated(nickname);
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @Operation(summary = "유저 생성", description = "유저를 생성합니다.")
    @LoggingExclude
    @PostMapping
    public ResponseEntity<SuccessResponse<UserCreateResponse>> createUser(
            @Valid @RequestBody UserCreateRequest request) {
        UserCreateResponse response = userService.addUser(request);
        HttpStatus status = HttpStatus.CREATED;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @Operation(summary = "유저 삭제", description = "유저를 삭제합니다.")
    @DeleteMapping
    public ResponseEntity<SuccessResponse<Void>> deleteUser(@LoginUserId Long userId) {
        userService.withdraw(userId);
        HttpStatus status = HttpStatus.NO_CONTENT;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }
}
