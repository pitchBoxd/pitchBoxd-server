package com.example.pitchboxd.season.presentation;

import com.example.pitchboxd.global.dto.response.SuccessResponse;
import com.example.pitchboxd.season.dto.response.SeasonResponses;
import com.example.pitchboxd.season.service.SeasonQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Season API", description = "시즌 API 명세")
@RestController
@RequestMapping("/api/v1/seasons")
@RequiredArgsConstructor
public class SeasonController {

    private final SeasonQueryService seasonQueryService;

    @Operation(summary = "모든 시즌 조회", description = "모든 시즌을 조회합니다.")
    @GetMapping
    public ResponseEntity<SuccessResponse<SeasonResponses>> getAllSeasons() {
        SeasonResponses responses = SeasonResponses.of(seasonQueryService.findAll());
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, responses));
    }
}
