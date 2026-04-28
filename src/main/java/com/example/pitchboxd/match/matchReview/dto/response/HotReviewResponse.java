package com.example.pitchboxd.match.matchReview.dto.response;

import com.example.pitchboxd.match.matchReview.infrastructure.dto.HotReviewSummary;

public record HotReviewResponse(
        Long id,
        Long matchId,
        String authorNickname,
        Long userId,
        String content,
        long likeCount,
        double rating
) {

    public static HotReviewResponse of(HotReviewSummary summary) {
        return new HotReviewResponse(
                summary.reviewId(),
                summary.matchId(),
                summary.authorNickname(),
                summary.authorId(),
                summary.content(),
                summary.likeCount(),
                summary.point()
        );
    }
}
