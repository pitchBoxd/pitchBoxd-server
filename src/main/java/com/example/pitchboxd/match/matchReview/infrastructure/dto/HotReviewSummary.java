package com.example.pitchboxd.match.matchReview.infrastructure.dto;

import com.example.pitchboxd.match.matchStatistics.domain.FanType;

public record HotReviewSummary(
        Long reviewId,
        Long matchId,
        String homeTeamName,
        String awayTeamName,
        String authorNickname,
        Long authorId,
        FanType fanType,
        Integer point,
        String content,
        Long likeCount
) {
}
