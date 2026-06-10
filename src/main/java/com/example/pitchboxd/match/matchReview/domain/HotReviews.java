package com.example.pitchboxd.match.matchReview.domain;

import com.example.pitchboxd.match.matchReview.infrastructure.dto.HotReviewSummary;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HotReviews {
    private final Map<Long, List<HotReviewSummary>> reviewsByMatchId;

    private HotReviews(Map<Long, List<HotReviewSummary>> reviewsByMatchId) {
        this.reviewsByMatchId = reviewsByMatchId;
    }

    public static HotReviews from(List<HotReviewSummary> summaries) {
        Map<Long, List<HotReviewSummary>> grouped = summaries.stream()
                .collect(Collectors.groupingBy(HotReviewSummary::matchId));
        return new HotReviews(grouped);
    }

    public List<HotReviewSummary> getTopReviews(Long matchId, int limit) {
        List<HotReviewSummary> matchReviews = reviewsByMatchId.getOrDefault(matchId, List.of());
        return matchReviews.stream()
                .limit(limit)
                .toList();
    }
}
