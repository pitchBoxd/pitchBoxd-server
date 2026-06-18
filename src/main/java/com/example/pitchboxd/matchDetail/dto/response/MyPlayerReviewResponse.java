package com.example.pitchboxd.matchDetail.dto.response;

public record MyPlayerReviewResponse(
        Long playerReviewId,
        Long playerId,
        Integer rating,
        String comment,
        boolean isModified,
        Long likeCount
) {}
