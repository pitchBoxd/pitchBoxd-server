package com.example.pitchboxd.match.matchReview.dto.response;

import com.example.pitchboxd.match.matchReview.infrastructure.dto.HotReviewSummary;
import java.util.List;

public record HotReviewResponses(List<HotReviewResponse> responses) {

    public static HotReviewResponses of(List<HotReviewSummary> hotReviews) {
        List<HotReviewResponse> hotReviewResponses = hotReviews.stream()
                .map(HotReviewResponse::of)
                .toList();
        
        return new HotReviewResponses(hotReviewResponses);
    }
}
