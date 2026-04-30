package com.example.pitchboxd.admin.dto.request;

public record UpdatePlayerRequest(
        Long teamId,
        String name,
        String naverId
) {
}
