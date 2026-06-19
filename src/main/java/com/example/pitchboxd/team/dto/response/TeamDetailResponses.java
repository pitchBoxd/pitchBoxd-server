package com.example.pitchboxd.team.dto.response;

import java.util.List;

public record TeamDetailResponses(List<TeamDetailResponse> teamDetailResponses) {

    public static TeamDetailResponses of(List<TeamDetailResponse> responses) {
        return new TeamDetailResponses(responses);
    }
}
