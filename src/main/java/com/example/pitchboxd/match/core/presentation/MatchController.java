package com.example.pitchboxd.match.core.presentation;

import com.example.pitchboxd.auth.presentation.LoginUserId;
import com.example.pitchboxd.global.dto.response.SuccessResponse;
import com.example.pitchboxd.match.core.dto.request.CreateMatchRequest;
import com.example.pitchboxd.match.core.dto.response.MatchResponses;
import com.example.pitchboxd.match.core.service.domain.MatchSyncService;
import com.example.pitchboxd.match.core.service.facade.MatchFacadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchSyncService matchSyncService;
    private final MatchFacadeService matchFacadeService;

    @PostMapping
    public ResponseEntity<SuccessResponse<Void>> syncKLeagueMatches(
            @RequestBody CreateMatchRequest createMatchRequest
    ) {
        matchSyncService.syncKLeagueMatches(createMatchRequest);
        HttpStatus status = HttpStatus.CREATED;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }

    @GetMapping("/reviewable")
    public ResponseEntity<SuccessResponse<MatchResponses>> getReviewableMatches(
            @LoginUserId(required = false) Long userId,
            @RequestParam(required = false) String filter
    ) {
        MatchResponses responses = matchFacadeService.findReviewableMatches(userId, filter);
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, responses));
    }
}
