package com.example.pitchboxd.auth.application;

import com.example.pitchboxd.auth.domain.Tokens;
import com.example.pitchboxd.auth.dto.GoogleAccessToken;
import com.example.pitchboxd.auth.dto.GoogleLoginResult;
import com.example.pitchboxd.auth.dto.request.GoogleLoginRequest;
import com.example.pitchboxd.auth.dto.request.GoogleSignupRequest;
import com.example.pitchboxd.auth.dto.response.GoogleUserInfoResponse;
import com.example.pitchboxd.auth.infrastructure.GoogleOAuthClient;
import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
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
public class GoogleAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenIssuer tokenIssuer;
    private final GoogleOAuthClient googleOAuthClient;

    public GoogleLoginResult googleLogin(GoogleLoginRequest request) {
        GoogleAccessToken googleToken = googleOAuthClient.getAccessToken(request.authorizationCode());
        GoogleUserInfoResponse googleUserInfo = googleOAuthClient.getUserInfo(googleToken.idToken());

        return userRepository.findByEmail(googleUserInfo.email())
                .map(user -> {
                    Tokens tokens = tokenIssuer.issueTokens(user);
                    return GoogleLoginResult.registered(tokens);
                })
                .orElseGet(() -> GoogleLoginResult.newMember(googleUserInfo, googleToken.idToken()));
    }

    @Transactional
    public Tokens googleSignup(GoogleSignupRequest request) {
        GoogleUserInfoResponse googleUserInfo = googleOAuthClient.getUserInfo(request.idToken());

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

        return tokenIssuer.issueTokens(savedUser);
    }
}
