package com.example.pitchboxd.matchDetail.dto.response;

import com.example.pitchboxd.match.matchStatistics.domain.FanType;
import java.time.LocalDateTime;

public record MatchReviewDetailResponse(
        Long reviewId,
        Long userId,
        String nickname,
        String profileImage,
        FanType fanType,
        Integer point,
        String content,
        Long likeCount,
        boolean isLiked,
        boolean isOwner,
        LocalDateTime createdAt
) {}
