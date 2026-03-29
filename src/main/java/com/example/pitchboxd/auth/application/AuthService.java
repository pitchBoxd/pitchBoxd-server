package com.example.pitchboxd.auth.application;

import com.example.pitchboxd.auth.dto.request.GoogleLoginRequest;
import com.example.pitchboxd.auth.dto.request.GoogleSignupRequest;
import com.example.pitchboxd.auth.dto.request.LoginRequest;
import com.example.pitchboxd.auth.dto.response.GoogleLoginResponse;
import com.example.pitchboxd.auth.dto.response.GoogleUserInfoResponse;
import com.example.pitchboxd.auth.dto.response.TokenResponse;
import com.example.pitchboxd.auth.infrastructure.GoogleClient;
import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.global.security.JwtProvider;
import com.example.pitchboxd.user.domain.Provider;
import com.example.pitchboxd.user.domain.User;
import com.example.pitchboxd.user.infrastructure.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final GoogleClient googleClient;

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        String accessToken = jwtProvider.createToken(user.getId(), user.getEmail());

        return new TokenResponse(accessToken);
    }

    public GoogleLoginResponse googleLogin(GoogleLoginRequest request) {
        GoogleUserInfoResponse googleUserInfo = googleClient.getUserInfo(request.idToken());

        return userRepository.findByEmail(googleUserInfo.email())
                .map(user -> {
                    String accessToken = jwtProvider.createToken(user.getId(), user.getEmail());
                    return GoogleLoginResponse.registered(accessToken);
                })
                .orElseGet(() -> {
                    return GoogleLoginResponse.newMember(googleUserInfo);
                });
    }

    public TokenResponse googleSignup(GoogleSignupRequest request) {
        GoogleUserInfoResponse googleUserInfo = googleClient.getUserInfo(request.idToken());

        if (userRepository.existsByEmail(googleUserInfo.email())) {
            throw new BusinessException(ErrorCode.USER_EMAIL_CONFLICT);
        }

        String dummyPassword = passwordEncoder.encode(UUID.randomUUID().toString());

        User newUser = new User(
                request.nickname(),
                googleUserInfo.email(),
                dummyPassword,
                request.favoriteTeamId(),
                Provider.GOOGLE);

        User savedUser = userRepository.save(newUser);

        String accessToken = jwtProvider.createToken(savedUser.getId(), savedUser.getEmail());

        return new TokenResponse(accessToken);
    }
}
