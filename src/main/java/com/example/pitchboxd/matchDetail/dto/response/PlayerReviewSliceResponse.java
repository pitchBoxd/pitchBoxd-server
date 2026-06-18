package com.example.pitchboxd.matchDetail.dto.response;

import java.util.List;

public record PlayerReviewSliceResponse(
        List<PlayerReviewDetailResponse> reviews,
        Long nextCursorId,
        Long nextCursorLikeCount,
        boolean hasNext
) {}
