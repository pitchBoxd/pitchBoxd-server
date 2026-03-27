package com.example.pitchboxd.user.dto.response;


import com.example.pitchboxd.user.domain.User;

public record UserCreateResponse(Long id) {
    
    public static UserCreateResponse from(User user) {
        return new UserCreateResponse(user.getId());
    }
}
