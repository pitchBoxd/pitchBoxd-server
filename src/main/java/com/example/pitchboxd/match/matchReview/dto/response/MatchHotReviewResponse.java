package com.example.pitchboxd.match.matchReview.dto.response;

import java.util.List;

public record MatchHotReviewResponse(
        Long matchId,
        List<HotReviewResponse> hotReviews
) {
    public static MatchHotReviewResponse of(Long matchId, List<HotReviewResponse> hotReviews) {
        return new MatchHotReviewResponse(matchId, hotReviews);
    }
}
