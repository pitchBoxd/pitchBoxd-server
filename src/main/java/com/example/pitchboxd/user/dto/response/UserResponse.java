package com.example.pitchboxd.user.dto.response;

import com.example.pitchboxd.user.domain.User;

public record UserResponse(Long id, String nickname) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getNickname());
    }
}
