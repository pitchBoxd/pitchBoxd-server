package com.example.pitchboxd.home.dto.response;

import com.example.pitchboxd.match.core.dto.response.MatchResponse;
import com.example.pitchboxd.match.matchReview.dto.response.HotReviewResponse;
import java.util.List;

public record HomeMatchResponse(
        MatchResponse matchResponse,
        List<HotReviewResponse> hotReviews
) {
    public static HomeMatchResponse of(MatchResponse matchResponse, List<HotReviewResponse> hotReviews) {
        return new HomeMatchResponse(matchResponse, hotReviews);
    }
}
