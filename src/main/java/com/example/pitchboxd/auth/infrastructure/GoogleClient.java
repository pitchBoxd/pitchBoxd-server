package com.example.pitchboxd.auth.infrastructure;

import com.example.pitchboxd.auth.dto.response.GoogleUserInfoResponse;
import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class GoogleClient {

    private static final String GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";
    private final RestTemplate restTemplate;

    public GoogleUserInfoResponse getUserInfo(String idToken) {
        String url = GOOGLE_TOKEN_INFO_URL + idToken;

        try {
            GoogleUserInfoResponse response = restTemplate.getForObject(url, GoogleUserInfoResponse.class);

            if (response == null || response.email() == null) {
                throw new BusinessException(ErrorCode.INVALID_TOKEN);
            }

            return response;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }
}
