package com.example.pitchboxd.matchDetail.presentation;

import com.example.pitchboxd.global.dto.response.SuccessResponse;
import com.example.pitchboxd.matchDetail.dto.response.MatchDetailResponse;
import com.example.pitchboxd.matchDetail.service.MatchDetailFacadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Match 화면 API", description = "경기 화면 API 명세")
@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
public class MatchDetailController {

    private final MatchDetailFacadeService matchDetailFacadeService;

    @Operation(summary = "경기 페이지 정적 데이터", description = "경기 페이지의 정적 데이터를 가져옵니다. 1. 경기 결과 관련 데이터 2. 라인업 관련 데이터")
    @GetMapping("{matchId}/detail/static")
    public ResponseEntity<SuccessResponse<MatchDetailResponse>> getMatchStaticData(@PathVariable Long matchId) {
        MatchDetailResponse responses = matchDetailFacadeService.getMatchStaticData(matchId);
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, responses));
    }
}
