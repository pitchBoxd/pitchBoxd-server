package com.example.pitchboxd.auth.dto;

import java.time.LocalDateTime;

public record TokenDto(String tokenValue, LocalDateTime issuedAt, LocalDateTime expiredAt) {
}
