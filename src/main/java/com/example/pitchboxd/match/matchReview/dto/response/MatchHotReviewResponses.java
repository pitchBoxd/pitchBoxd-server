package com.example.pitchboxd.match.matchReview.dto.response;

import java.util.List;

public record MatchHotReviewResponses(List<MatchHotReviewResponse> responses) {
    public static MatchHotReviewResponses of(List<MatchHotReviewResponse> responses) {
        return new MatchHotReviewResponses(responses);
    }
}
