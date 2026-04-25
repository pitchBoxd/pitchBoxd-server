package com.example.pitchboxd.auth.infrastructure;

import com.example.pitchboxd.auth.dto.GoogleAccessToken;
import com.example.pitchboxd.auth.dto.response.GoogleUserInfoResponse;
import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class GoogleOAuthClient {

    private static final String GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token={idToken}";
    private final RestClient restClient;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    public GoogleOAuthClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl("https://oauth2.googleapis.com")
                .build();
    }

    public GoogleAccessToken getAccessToken(String authorizationCode) {
        // OAuth2 표준에 따라 MultiValueMap으로 파라미터 구성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", authorizationCode);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("grant_type", "authorization_code");

        return restClient.post()
                .uri("/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED) // ⭐️ 필수 설정
                .body(body)
                .retrieve()
                // 4xx, 5xx 에러 발생 시 예외 처리 정의 가능
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new BusinessException(ErrorCode.GOOGLE_AUTH_ERROR);
                })
                .body(GoogleAccessToken.class);
    }

    public GoogleUserInfoResponse getUserInfo(String idToken) {
        try {
            return restClient.get().uri(GOOGLE_TOKEN_INFO_URL, idToken)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new BusinessException(ErrorCode.INVALID_TOKEN_GOOGLE);
                    })
                    .body(GoogleUserInfoResponse.class);
        } catch (Exception e) {
            log.warn("구글 계정 연동 에러");
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
