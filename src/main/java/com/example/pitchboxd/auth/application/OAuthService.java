package com.example.pitchboxd.auth.application;

import com.example.pitchboxd.auth.domain.Tokens;
import com.example.pitchboxd.auth.dto.request.OAuthSignupRequest;
import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.user.domain.Provider;
import com.example.pitchboxd.user.domain.User;
import com.example.pitchboxd.user.infrastructure.UserRepository;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenIssuer tokenIssuer;
    private final TokenManager tokenManager;

    @Transactional
    public Tokens signup(OAuthSignupRequest request) {
        Map<String, String> signupInfo = tokenManager.parseSignupToken(request.signupToken());
        String email = signupInfo.get("email");
        Provider provider = Provider.valueOf(signupInfo.get("provider"));
        String providerKey = signupInfo.get("providerKey");

        String nickname = request.nickname();

        if (userRepository.existsByProviderAndProviderKey(provider, providerKey)) {
            throw new BusinessException(ErrorCode.USER_ALREADY_REGISTERED);
        }

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.USER_EMAIL_CONFLICT);
        }

        if (userRepository.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }

        String dummyPassword = passwordEncoder.encode(UUID.randomUUID().toString());

        User newUser = new User(
                nickname,
                email,
                dummyPassword,
                request.favoriteTeamId(),
                provider,
                providerKey
        );
        User savedUser = userRepository.save(newUser);

        return tokenIssuer.issueTokens(savedUser);
    }
}
