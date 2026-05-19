package com.example.pitchboxd.global.security;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import java.util.Map;

public class OAuth2UserFactory {
    public static OAuth2UserInfo get(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equals("google")) {
            return new GoogleUserInfo(attributes);
        }

        throw new BusinessException(ErrorCode.INVALID_OAUTH2_PROVIDER);
    }
}
