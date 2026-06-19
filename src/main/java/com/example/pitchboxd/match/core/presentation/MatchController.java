package com.example.pitchboxd.match.core.presentation;

import com.example.pitchboxd.global.dto.response.SuccessResponse;
import com.example.pitchboxd.match.core.dto.response.MatchResponses;
import com.example.pitchboxd.match.core.service.domain.MatchQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.time.LocalDateTime;
import com.example.pitchboxd.match.matchReview.domain.MatchReviewSubmitPolicy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Match API", description = "경기 API 명세")
@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchQueryService matchQueryService;
    private final MatchReviewSubmitPolicy matchReviewSubmitPolicy;

    @Operation(summary = "경기 목록 조회", description = "팀 필터링 및 최신순 정렬로 경기를 조회합니다.")
    @GetMapping
    public ResponseEntity<SuccessResponse<MatchResponses>> getMatches(
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) Long seasonId
    ) {
        MatchResponses responses = MatchResponses.of(
                matchQueryService.findMatchesWithFilters(teamId, seasonId, LocalDateTime.now()),
                matchReviewSubmitPolicy
        );
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, responses));
    }
}
