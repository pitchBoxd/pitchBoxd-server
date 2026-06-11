package com.example.pitchboxd.matchDetail.dto.response;

import java.util.List;

public record MatchReviewSliceResponse(
        List<MatchReviewDetailResponse> reviews,
        Long nextCursorId,
        Long nextCursorLikeCount,
        boolean hasNext
) {}
