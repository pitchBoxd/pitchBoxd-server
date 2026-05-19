package com.example.pitchboxd.global.security;

import com.example.pitchboxd.user.domain.Provider;

public interface OAuth2UserInfo {
    String getProviderId();

    Provider getProvider();

    String getEmail();

    String getName();
}
