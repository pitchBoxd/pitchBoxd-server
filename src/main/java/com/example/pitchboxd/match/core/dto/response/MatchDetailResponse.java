package com.example.pitchboxd.match.core.dto.response;

import java.time.LocalDateTime;

public record MatchDetailResponse(
        String season,
        Integer round,
        LocalDateTime dateTime,
        String location,
        String homeTeam,
        String awayTeam,
        Integer homeScore,
        Integer awayScore,
        Long reviewCount,
        double homeRating,
        double awayRating
) {
}
