package com.example.pitchboxd.match.core.dto.response;

import com.example.pitchboxd.match.core.infrastructure.dto.MatchSummary;
import java.util.List;

public record MatchResponses(List<MatchResponse> matchResponses) {

    public static MatchResponses of(List<MatchSummary> matchSummaries) {
        List<MatchResponse> responses = matchSummaries.stream()
                .map(MatchResponse::of)
                .toList();

        return new MatchResponses(responses);
    }

}
