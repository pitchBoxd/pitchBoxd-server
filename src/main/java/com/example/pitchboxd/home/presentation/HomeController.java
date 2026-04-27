package com.example.pitchboxd.home.presentation;

import com.example.pitchboxd.global.dto.response.SuccessResponse;
import com.example.pitchboxd.home.dto.response.HomeResponses;
import com.example.pitchboxd.home.service.HomeFacadeService;
import com.example.pitchboxd.match.core.domain.MatchFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeFacadeService homeFacadeService;

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
