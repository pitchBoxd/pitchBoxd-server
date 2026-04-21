package com.example.pitchboxd.global.infrastructure.naver.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NaverPlayerResponse(ResultNode result) {

    public List<NaverPlayerNode> getPlayers() {
        if (result == null || result.seasonPlayerStats() == null) {
            return List.of();
        }
        return result.seasonPlayerStats();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResultNode(List<NaverPlayerNode> seasonPlayerStats) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NaverPlayerNode(
            String playerId,   // "20240254"
            String playerName, // "후이즈"
            String teamId      // "09" (네이버 팀 코드)
    ) {
    }
}
