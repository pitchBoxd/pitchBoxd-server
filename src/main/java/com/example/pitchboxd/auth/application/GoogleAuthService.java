package com.example.pitchboxd.auth.application;

import com.example.pitchboxd.auth.domain.Tokens;
import com.example.pitchboxd.auth.dto.GoogleLoginResult;
import com.example.pitchboxd.auth.dto.request.GoogleLoginRequest;
import com.example.pitchboxd.auth.dto.request.GoogleSignupRequest;
import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.user.domain.Provider;
import com.example.pitchboxd.user.domain.User;
import com.example.pitchboxd.user.infrastructure.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;
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
    private final GoogleIdTokenVerifier verifier;

    @Transactional
    public GoogleLoginResult googleLogin(GoogleLoginRequest request) {
        GoogleIdToken idToken = verifyToken(request.googleIdToken());
        GoogleIdToken.Payload payload = idToken.getPayload();

        String email = payload.getEmail();
        String googleSub = payload.getSubject();

        Optional<User> userByGoogle = userRepository.findByProviderAndProviderId(Provider.GOOGLE, googleSub);

        if (userByGoogle.isPresent()) {
            Tokens tokens = tokenIssuer.issueTokens(userByGoogle.get());
            return GoogleLoginResult.registered(tokens);
        }

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.ALREADY_SIGNED_UP_OTHER_PROVIDER);
        }

        return GoogleLoginResult.newMember(request.googleIdToken());
    }

    @Transactional
    public Tokens googleSignup(GoogleSignupRequest request) {
        GoogleIdToken idToken = verifyToken(request.googleIdToken());
        GoogleIdToken.Payload payload = idToken.getPayload();

        String email = payload.getEmail();
        String googleSub = payload.getSubject();

        if (userRepository.existsByProviderAndProviderId(Provider.GOOGLE, googleSub)) {
            throw new BusinessException(ErrorCode.USER_ALREADY_REGISTERED);
        }

        userRepository.findByEmail(email).ifPresent(existingUser -> {
            throw new BusinessException(ErrorCode.USER_EMAIL_CONFLICT);
        });

        if (userRepository.existsByNickname(request.nickname())) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }
        
        String dummyPassword = passwordEncoder.encode(UUID.randomUUID().toString());

        User newUser = new User(
                request.nickname(),
                email,
                dummyPassword,
                request.favoriteTeamId(),
                Provider.GOOGLE,
                googleSub
        );
        User savedUser = userRepository.save(newUser);

        return tokenIssuer.issueTokens(savedUser);
    }

    private GoogleIdToken verifyToken(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new BusinessException(ErrorCode.INVALID_TOKEN);
            }
            return idToken;
        } catch (GeneralSecurityException | IOException e) {
            throw new BusinessException(ErrorCode.GOOGLE_AUTH_ERROR);
        }
    }
}
