package com.example.pitchboxd.match.core.presentation;

import com.example.pitchboxd.global.dto.response.SuccessResponse;
import com.example.pitchboxd.match.core.domain.MatchFilter;
import com.example.pitchboxd.match.core.dto.response.MatchResponses;
import com.example.pitchboxd.match.core.service.facade.MatchFacadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchFacadeService matchFacadeService;

    @GetMapping
    public ResponseEntity<SuccessResponse<MatchResponses>> getMatches(
            @RequestParam(required = false) MatchFilter state,
            @RequestParam(required = false) Long season
    ) {
        MatchResponses responses = matchFacadeService.findMatches(state, season);
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, responses));
    }
}
