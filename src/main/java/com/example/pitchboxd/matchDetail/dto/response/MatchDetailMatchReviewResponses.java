package com.example.pitchboxd.matchDetail.dto.response;

import com.example.pitchboxd.match.matchReview.infrastructure.dto.HotReviewSummary;
import java.util.List;
import java.util.Map;

public record MatchDetailMatchReviewResponses(
        List<MatchDetailMatchReviewResponse> responses
) {
    public static MatchDetailMatchReviewResponses of(List<HotReviewSummary> topHotReviewsByMatchId,
                                                     Map<Long, Boolean> likedStatusForReviews) {

        List<MatchDetailMatchReviewResponse> responses = topHotReviewsByMatchId.stream()
                .map(hotReview -> {
                    boolean isLiked = likedStatusForReviews.getOrDefault(hotReview.reviewId(), false);

                    return MatchDetailMatchReviewResponse.of(hotReview, isLiked);
                })
                .toList();

        return new MatchDetailMatchReviewResponses(responses);
    }
}
