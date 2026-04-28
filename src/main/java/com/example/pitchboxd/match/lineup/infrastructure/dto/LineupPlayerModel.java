package com.example.pitchboxd.match.lineup.infrastructure.dto;

import com.example.pitchboxd.match.lineup.domain.ParticipationStatus;
import com.querydsl.core.annotations.QueryProjection;

public record LineupPlayerModel(
        Long playerId,
        String playerName,
        Integer backNumber,
        Long teamId,
        ParticipationStatus status
) {
    @QueryProjection
    public LineupPlayerModel {
    }
}
