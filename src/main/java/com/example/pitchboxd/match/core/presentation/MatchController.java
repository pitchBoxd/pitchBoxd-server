package com.example.pitchboxd.match.core.presentation;

import com.example.pitchboxd.global.dto.response.SuccessResponse;
import com.example.pitchboxd.match.core.dto.request.CreateMatchRequest;
import com.example.pitchboxd.match.core.service.domain.MatchSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchSyncService matchSyncService;

    @PostMapping
    public ResponseEntity<SuccessResponse<Void>> syncKLeagueMatches(
            @RequestBody CreateMatchRequest createMatchRequest
    ) {
        matchSyncService.syncKLeagueMatches(createMatchRequest);
        HttpStatus status = HttpStatus.CREATED;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }
}
