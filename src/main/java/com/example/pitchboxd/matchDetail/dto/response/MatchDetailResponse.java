package com.example.pitchboxd.matchDetail.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record MatchDetailResponse(
        String season,
        String round,
        LocalDateTime dateTime,
        String location,
        String homeTeam,
        String awayTeam,
        Integer homeScore,
        Integer awayScore,
        LineupResponses homeLineups,
        LineupResponses awayLineups,
        Double matchAverageRating,
        Double homeFanAverageRating,
        Double awayFanAverageRating,
        Double neutralFanAverageRating,
        Map<Integer, Long> ratingDistribution,
        MatchHighlightsResponse highlights
) {
    public record MatchHighlightsResponse(
            HighlightPlayerResponse mom,
            List<HighlightPlayerResponse> top3
    ) {}
    
    public record HighlightPlayerResponse(
            Long playerId,
            String name,
            Double averageRating
    ) {}
}
