package com.example.pitchboxd.admin.dto.response;

import com.example.pitchboxd.user.domain.User;
import java.time.LocalDateTime;

public record AdminUserResponse(
    Long id,
    String nickname,
    String email,
    Long favoriteTeamId,
    String provider,
    String role,
    LocalDateTime createdAt
) {
    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
            user.getId(),
            user.getNickname(),
            user.getEmail(),
            user.getFavoriteTeamId(),
            user.getProvider() != null ? user.getProvider().name() : null,
            user.getRole().name(),
            user.getCreatedAt()
        );
    }
}
