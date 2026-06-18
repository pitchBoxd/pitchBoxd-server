package com.example.pitchboxd.matchDetail.dto.response;

import java.time.LocalDateTime;

public record PlayerReviewDetailResponse(
        Long id,
        String nickname,
        String favoriteTeamName,
        int point,
        String content,
        String fanType,
        long likeCount,
        boolean isLiked,
        LocalDateTime createdAt
) {}
