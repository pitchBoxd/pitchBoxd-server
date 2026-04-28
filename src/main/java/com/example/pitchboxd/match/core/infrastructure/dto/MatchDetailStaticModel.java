package com.example.pitchboxd.match.core.infrastructure.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;

public record MatchDetailStaticModel(
        Long matchId,
        String seasonName,
        String round,
        LocalDateTime startTime,
        String location,
        String homeTeamName,
        Long homeTeamId,
        String awayTeamName,
        Long awayTeamId,
        Integer homeScore,
        Integer awayScore
) {
    @QueryProjection
    public MatchDetailStaticModel {
    }
}
