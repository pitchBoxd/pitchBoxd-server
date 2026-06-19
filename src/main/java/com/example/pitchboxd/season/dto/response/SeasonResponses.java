package com.example.pitchboxd.season.dto.response;

import com.example.pitchboxd.season.domain.Season;
import java.util.List;

public record SeasonResponses(List<SeasonResponse> seasonResponses) {

    public static SeasonResponses of(List<Season> seasons) {
        List<SeasonResponse> responses = seasons.stream()
                .map(SeasonResponse::of)
                .toList();

        return new SeasonResponses(responses);
    }
}
