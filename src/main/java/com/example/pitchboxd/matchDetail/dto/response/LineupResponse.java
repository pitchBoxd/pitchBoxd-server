package com.example.pitchboxd.matchDetail.dto.response;

import com.example.pitchboxd.match.lineup.domain.ParticipationStatus;
import com.example.pitchboxd.match.lineup.infrastructure.dto.LineupPlayerModel;

public record LineupResponse(
        String playerName,
        Integer backNumber,
        ParticipationStatus status
) {

    public static LineupResponse of(LineupPlayerModel lineup) {
        return new LineupResponse(lineup.playerName(), lineup.backNumber(), lineup.status());
    }
}
