package com.example.pitchboxd.home.presentation;

import com.example.pitchboxd.global.dto.response.SuccessResponse;
import com.example.pitchboxd.home.dto.response.HomeResponses;
import com.example.pitchboxd.home.service.HomeFacadeService;
import com.example.pitchboxd.match.core.domain.MatchFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Home API", description = "Home API 명세")
@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeFacadeService homeFacadeService;

    @Operation(summary = "메인 페이지 데이터", description = "메인 페이지의 데이터를 가져옵니다. 1. 리뷰 가능 경기 2. 해당 경기의 핫한 리뷰")
    @GetMapping
    public ResponseEntity<SuccessResponse<HomeResponses>> getHomeData(
            @RequestParam(required = false) MatchFilter state,
            @RequestParam(required = false) Long season
    ) {
        HomeResponses responses = homeFacadeService.getHomeData(state, season);
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, responses));
    }
}
