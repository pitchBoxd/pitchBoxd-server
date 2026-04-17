package com.example.pitchboxd.match.core.dto.response;

import com.example.pitchboxd.match.core.service.domain.dto.MatchSummary;
import java.time.LocalDateTime;

public record MatchResponse(
        Long id,
        String round,
        LocalDateTime startDate,
        String stadium,
        String homeTeam,
        int homeTeamScore,
        String awayTeam,
        int awayTeamScore,
        int reviewCount,
        double matchRating
) {
    public static MatchResponse of(MatchSummary matchSummary) {
        return new MatchResponse(
                matchSummary.id(),
                matchSummary.round(),
                matchSummary.startDateTime(),
                matchSummary.stadium(),
                matchSummary.homeTeam(),
                matchSummary.homeTeamScore(),
                matchSummary.awayTeam(),
                matchSummary.awayTeamScore(),
                matchSummary.reviewCount(),
                matchSummary.matchRating()
        );
    }
}
