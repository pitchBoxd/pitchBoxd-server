package com.example.pitchboxd.auth.infrastructure;

import com.example.pitchboxd.auth.dto.response.GoogleUserInfoResponse;
import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleClient {

    private static final String GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token={idToken}";
    private final RestClient restClient;

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
