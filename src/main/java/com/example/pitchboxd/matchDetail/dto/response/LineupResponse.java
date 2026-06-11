package com.example.pitchboxd.matchDetail.dto.response;

import com.example.pitchboxd.match.lineup.domain.ParticipationStatus;
import com.example.pitchboxd.match.lineup.infrastructure.dto.LineupPlayerModel;

public record LineupResponse(
        Long playerId,
        String playerName,
        Integer backNumber,
        ParticipationStatus status,
        Double averageRating
) {
    public static LineupResponse of(LineupPlayerModel lineup, Double averageRating) {
        return new LineupResponse(lineup.playerId(), lineup.playerName(), lineup.backNumber(), lineup.status(), averageRating);
    }
}
