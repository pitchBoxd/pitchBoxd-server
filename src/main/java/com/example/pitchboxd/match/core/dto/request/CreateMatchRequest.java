package com.example.pitchboxd.match.core.dto.request;

import java.time.LocalDate;

public record CreateMatchRequest(LocalDate from, LocalDate to) {

}
