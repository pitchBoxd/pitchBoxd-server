package com.example.pitchboxd.user.application;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.user.domain.User;
import com.example.pitchboxd.user.dto.request.UserCreateRequest;
import com.example.pitchboxd.user.dto.response.EmailAvailabilityResponse;
import com.example.pitchboxd.user.dto.response.NicknameAvailabilityResponse;
import com.example.pitchboxd.user.dto.response.UserCreateResponse;
import com.example.pitchboxd.user.dto.response.UserResponse;
import com.example.pitchboxd.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserCreateResponse addUser(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.USER_EMAIL_CONFLICT);
        }
        String encodedPassword = passwordEncoder.encode(request.password());
        User user = new User(request.nickname(), request.email(), encodedPassword);

        User savedUser = userRepository.save(user);

        return UserCreateResponse.from(savedUser);
    }

    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        validateUser(user, userId);
        userRepository.delete(user);
    }

    public UserResponse getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_UNAUTHENTICATED));

        return UserResponse.from(user);
    }

    public void validateUser(User user, Long id) {
        if (!user.matchId(id)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    public EmailAvailabilityResponse isEmailDuplicated(String email) {
        return new EmailAvailabilityResponse(userRepository.existsByEmail(email));
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    public NicknameAvailabilityResponse isNicknameDuplicated(String nickname) {
        return new NicknameAvailabilityResponse(userRepository.existsByNickname(nickname));
    }

    public java.util.List<User> findAllUsers() {
        return userRepository.findAll();
    }
}
