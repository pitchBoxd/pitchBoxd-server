package com.example.pitchboxd.matchDetail.dto.response;

import java.time.LocalDateTime;

public record MatchDetailResultResponse(
        String seasonName,
        String round,
        LocalDateTime startTime,
        String location,
        String homeTeamName,
        String awayTeamName,
        Integer homeScore,
        Integer awayScore,
        LineupResponses homeLineups,
        LineupResponses awayLineups,
        LocalDateTime reviewEndTime
) {}
