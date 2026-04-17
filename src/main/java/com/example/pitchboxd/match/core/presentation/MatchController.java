package com.example.pitchboxd.match.core.presentation;

import com.example.pitchboxd.global.dto.response.SuccessResponse;
import com.example.pitchboxd.match.core.domain.Scope;
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

    @GetMapping
    public ResponseEntity<SuccessResponse<MatchResponses>> getCurrentMatches(
            @RequestParam(name = "filter", required = false, defaultValue = "this_week") String rawScope
    ) {
        Scope scope = parseScope(rawScope);
        MatchResponses responses = matchFacadeService.findMatchesByScope(scope);
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, responses));
    }

    private Scope parseScope(String rawScope) {
        if (rawScope.equals("this_week")) {
            return Scope.THIS_WEEK;
        }

        if (rawScope.equals("this_round")) {
            return Scope.THIS_ROUND;
        }

        // TODO: 이거 추후에 Parsing 클래스를 만들고 지원하지 않는 string 값이 오면 예외주게 해야할듯
        return Scope.ALL;
    }
}
