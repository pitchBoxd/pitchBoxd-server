package com.example.pitchboxd.team.dto.response;

import com.example.pitchboxd.team.domain.Team;
import java.util.List;

public record TeamResponses(List<TeamResponse> teamResponses) {

    public static TeamResponses of(List<Team> allTeams) {
        List<TeamResponse> responses = allTeams.stream()
                .map(TeamResponse::of)
                .toList();

        return new TeamResponses(responses);
    }
}
