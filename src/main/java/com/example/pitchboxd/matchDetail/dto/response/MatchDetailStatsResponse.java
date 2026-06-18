package com.example.pitchboxd.matchDetail.dto.response;

import java.util.List;
import java.util.Map;

public record MatchDetailStatsResponse(
        Double totalAverage,
        Double homeAverage,
        Double awayAverage,
        Map<Integer, Long> distributionMap,
        MatchHighlightsResponse highlights,
        Double homePlayerAverage,
        Double awayPlayerAverage,
        Long homeCount,
        Long awayCount,
        Long neutralCount
) {
    public record HighlightPlayerResponse(
            Long playerId,
            String name,
            Double averageRating
    ) {}

    public record MatchHighlightsResponse(
            HighlightPlayerResponse mom,
            List<HighlightPlayerResponse> top3
    ) {}
}
