package com.example.pitchboxd.user.presentation;

import com.example.pitchboxd.auth.presentation.LoginUserId;
import com.example.pitchboxd.global.dto.response.SuccessResponse;
import com.example.pitchboxd.user.application.UserService;
import com.example.pitchboxd.user.dto.request.UserCreateRequest;
import com.example.pitchboxd.user.dto.response.EmailAvailabilityResponse;
import com.example.pitchboxd.user.dto.response.UserCreateResponse;
import com.example.pitchboxd.user.dto.response.UserResponse;
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

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<SuccessResponse<UserResponse>> getMyInfo(@LoginUserId Long userId) {
        UserResponse response = userService.getUserInfo(userId);
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @GetMapping("/exists")
    public ResponseEntity<SuccessResponse<EmailAvailabilityResponse>> checkEmailDuplicate(@RequestParam String email) {
        EmailAvailabilityResponse response = userService.isEmailDuplicated(email);
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @PostMapping
    public ResponseEntity<SuccessResponse<UserCreateResponse>> createUser(
            @Valid @RequestBody UserCreateRequest request) {
        UserCreateResponse response = userService.addUser(request);
        HttpStatus status = HttpStatus.CREATED;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @DeleteMapping
    public ResponseEntity<SuccessResponse<Void>> deleteUser(@LoginUserId Long userId) {
        userService.withdraw(userId);
        HttpStatus status = HttpStatus.NO_CONTENT;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }
}
