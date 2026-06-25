package com.example.pitchboxd.admin.dto.response;

import com.example.pitchboxd.player.domain.Player;

public record AdminPlayerResponse(
        Long id,
        Long teamId,
        String teamName,
        String name,
        String naverId
) {
    public static AdminPlayerResponse of(Player player, String teamName) {
        return new AdminPlayerResponse(
                player.getId(),
                player.getTeamId(),
                teamName,
                player.getName(),
                player.getNaverId()
        );
    }
}
