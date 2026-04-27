package com.example.pitchboxd.home.dto.response;

import java.util.List;

public record HomeResponses(List<HomeMatchResponse> responses) {
    public static HomeResponses of(List<HomeMatchResponse> responses) {
        return new HomeResponses(responses);
    }
}
