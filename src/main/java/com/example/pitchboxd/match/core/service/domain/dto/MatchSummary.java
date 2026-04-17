package com.example.pitchboxd.match.core.service.domain.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;

public record MatchSummary(
        Long id,
        String round,
        LocalDateTime startDateTime,
        String stadium,
        String homeTeam,
        int homeTeamScore,
        String awayTeam,
        int awayTeamScore,
        int reviewCount,
        double matchRating
) {

    @QueryProjection
    public MatchSummary {
    }
}
