package com.example.pitchboxd.team.dto.response;

import com.example.pitchboxd.team.domain.Team;

public record TeamResponse(Long id, String teamName) {

    public static TeamResponse of(Team team) {
        return new TeamResponse(team.getId(), team.getName());
    }
}
