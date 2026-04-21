package com.example.pitchboxd.admin.dto.request;

import java.time.LocalDate;

public record CreateMatchRequest(LocalDate from, LocalDate to) {

}
