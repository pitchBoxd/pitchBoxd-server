package com.example.pitchboxd.matchDetail.dto.response;

import com.example.pitchboxd.match.core.infrastructure.dto.MatchDetailStaticModel;
import com.example.pitchboxd.match.lineup.infrastructure.dto.LineupPlayerModel;
import java.time.LocalDateTime;
import java.util.List;

public record MatchDetailResponse(
        String season,
        String round,
        LocalDateTime dateTime,
        String location,
        String homeTeam,
        String awayTeam,
        Integer homeScore,
        Integer awayScore,
        LineupResponses homeLineups,
        LineupResponses awayLineups
) {

    public static MatchDetailResponse from(MatchDetailStaticModel matchDetail,
                                           List<LineupPlayerModel> homeLineups,
                                           List<LineupPlayerModel> awayLineups) {

        List<LineupResponse> homeLineupResponses = homeLineups.stream()
                .map(LineupResponse::of)
                .toList();

        List<LineupResponse> awayLineupResponses = awayLineups.stream()
                .map(LineupResponse::of)
                .toList();

        return new MatchDetailResponse(
                matchDetail.seasonName(),
                matchDetail.round(),
                matchDetail.startTime(),
                matchDetail.location(),
                matchDetail.homeTeamName(),
                matchDetail.awayTeamName(),
                matchDetail.homeScore(),
                matchDetail.awayScore(),
                new LineupResponses(homeLineupResponses),
                new LineupResponses(awayLineupResponses)
        );
    }
}
