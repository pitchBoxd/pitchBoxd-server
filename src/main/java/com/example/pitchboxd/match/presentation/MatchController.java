package com.example.pitchboxd.match.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
public class MatchController {

//    private final MatchQueryService matchQueryService;
//
//    @GetMapping("/{matchId}/detail")
//    public ResponseEntity<MatchDetailResponse> getMatchDetail(@PathVariable Long matchId) {
//        MatchDetailResponse response = matchQueryService.getMatchDetail(matchId);
//        return ResponseEntity.ok(response);
//    }

}
