package com.example.pitchboxd.global.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            log.info("OAuth2 loadUser start for registrationId: {}", userRequest.getClientRegistration().getRegistrationId());
            OAuth2User oAuth2User = super.loadUser(userRequest);
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            
            log.info("Attributes: {}", oAuth2User.getAttributes());
            OAuth2UserFactory.get(registrationId, oAuth2User.getAttributes());

            return oAuth2User;
        } catch (Exception e) {
            log.error("Error in CustomOAuth2UserService: ", e);
            throw e;
        }
    }
}
