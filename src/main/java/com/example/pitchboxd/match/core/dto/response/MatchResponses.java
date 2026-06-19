package com.example.pitchboxd.match.core.dto.response;

import com.example.pitchboxd.match.core.infrastructure.dto.MatchSummary;
import java.util.List;

import com.example.pitchboxd.match.matchReview.domain.MatchReviewSubmitPolicy;

public record MatchResponses(List<MatchResponse> matchResponses) {

    public static MatchResponses of(List<MatchSummary> matchSummaries, MatchReviewSubmitPolicy policy) {
        List<MatchResponse> responses = matchSummaries.stream()
                .map(m -> MatchResponse.of(m, policy))
                .toList();

        return new MatchResponses(responses);
    }

}
