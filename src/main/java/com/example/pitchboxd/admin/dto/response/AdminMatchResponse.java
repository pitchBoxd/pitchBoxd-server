package com.example.pitchboxd.admin.dto.response;

import com.example.pitchboxd.match.core.domain.GoalScorer;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import java.time.LocalDateTime;
import java.util.List;

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
    String location,
    List<GoalScorer> homeScorers,
    List<GoalScorer> awayScorers
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
            match.getLocation(),
            match.getMatchResult() != null && match.getMatchResult().getHomeScorers() != null ? match.getMatchResult().getHomeScorers() : List.of(),
            match.getMatchResult() != null && match.getMatchResult().getAwayScorers() != null ? match.getMatchResult().getAwayScorers() : List.of()
        );
    }
}
