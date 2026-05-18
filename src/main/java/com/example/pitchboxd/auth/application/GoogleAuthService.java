package com.example.pitchboxd.auth.application;

import com.example.pitchboxd.auth.domain.Tokens;
import com.example.pitchboxd.auth.dto.request.GoogleSignupRequest;
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

    @Transactional
    public Tokens googleSignup(GoogleSignupRequest request) {
        if (userRepository.existsByProviderAndProviderKey(Provider.GOOGLE, request.providerKey())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_REGISTERED);
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.USER_EMAIL_CONFLICT);
        }

        if (userRepository.existsByNickname(request.nickname())) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }

        String dummyPassword = passwordEncoder.encode(UUID.randomUUID().toString());

        User newUser = new User(
                request.nickname(),
                request.email(),
                dummyPassword,
                request.favoriteTeamId(),
                Provider.GOOGLE,
                request.providerKey()
        );
        User savedUser = userRepository.save(newUser);

        return tokenIssuer.issueTokens(savedUser);
    }
}
