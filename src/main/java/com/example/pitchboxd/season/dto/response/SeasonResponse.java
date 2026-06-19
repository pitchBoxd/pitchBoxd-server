package com.example.pitchboxd.season.dto.response;

import com.example.pitchboxd.season.domain.Season;

public record SeasonResponse(Long id, String name) {

    public static SeasonResponse of(Season season) {
        return new SeasonResponse(season.getId(), season.getName());
    }
}
