package com.example.pitchboxd.matchDetail.dto.response;

import com.example.pitchboxd.match.matchReview.infrastructure.dto.HotReviewSummary;
import com.example.pitchboxd.match.matchStatistics.domain.FanType;

public record MatchDetailMatchReviewResponse(
        Long reviewId,
        String authorNickname,
        Long authorId,
        FanType fanType,
        Integer point,
        String content,
        Long likeCount,
        boolean isLiked
) {
    public static MatchDetailMatchReviewResponse of(HotReviewSummary hotReview, Boolean isLiked) {
        return new MatchDetailMatchReviewResponse(
                hotReview.reviewId(),
                hotReview.authorNickname(),
                hotReview.authorId(),
                hotReview.fanType(),
                hotReview.point(),
                hotReview.content(),
                hotReview.likeCount(),
                isLiked
        );
    }
}
