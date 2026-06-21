package com.example.pitchboxd.admin.dto.response;

import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import java.time.LocalDateTime;

public record AdminMatchResponse(
    Long id,
    String naverId,
    Long homeTeamId,
    String homeTeamName,
    Long awayTeamId,
    String awayTeamName,
    String round,
    LocalDateTime startTime,
    LocalDateTime finishedAt,
    MatchStatus status,
    Integer homeScore,
    Integer awayScore,
    String location
) {
    public static AdminMatchResponse of(Match match, String homeTeamName, String awayTeamName) {
        return new AdminMatchResponse(
            match.getId(),
            match.getNaverId(),
            match.getHomeTeamId(),
            homeTeamName,
            match.getAwayTeamId(),
            awayTeamName,
            match.getRound(),
            match.getStartTime(),
            match.getFinishedAt(),
            match.getStatus(),
            match.getMatchResult() != null ? match.getMatchResult().getHomeScore() : null,
            match.getMatchResult() != null ? match.getMatchResult().getAwayScore() : null,
            match.getLocation()
        );
    }
}
