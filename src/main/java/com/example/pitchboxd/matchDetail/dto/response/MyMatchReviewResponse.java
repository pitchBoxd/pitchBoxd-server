package com.example.pitchboxd.matchDetail.dto.response;

public record MyMatchReviewResponse(
        Long reviewId,
        Integer rating,
        String comment,
        boolean isModified,
        Long likeCount
) {}
