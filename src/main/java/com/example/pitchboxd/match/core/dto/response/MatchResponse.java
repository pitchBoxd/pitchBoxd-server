package com.example.pitchboxd.match.core.dto.response;

import com.example.pitchboxd.match.core.infrastructure.dto.MatchSummary;
import java.time.LocalDateTime;

import com.example.pitchboxd.match.matchReview.domain.MatchReviewSubmitPolicy;

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
        double matchRating,
        LocalDateTime reviewEndTime
) {
    public static MatchResponse of(MatchSummary matchSummary, MatchReviewSubmitPolicy policy) {
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
                matchSummary.matchRating(),
                policy.getReviewEndTime(matchSummary.finishedAt())
        );
    }
}
