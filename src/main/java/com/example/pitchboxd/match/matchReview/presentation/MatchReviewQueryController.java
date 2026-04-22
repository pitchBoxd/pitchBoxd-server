package com.example.pitchboxd.match.matchReview.presentation;

import com.example.pitchboxd.global.dto.response.SuccessResponse;
import com.example.pitchboxd.match.matchReview.dto.response.HotReviewResponses;
import com.example.pitchboxd.match.matchReview.service.facade.MatchReviewFacadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/match-reviews")
@RequiredArgsConstructor
public class MatchReviewQueryController {

    private final MatchReviewFacadeService matchReviewFacadeService;

    @GetMapping("/hot")
    public ResponseEntity<SuccessResponse<HotReviewResponses>> getHotReviews(
            @RequestParam(defaultValue = "10") int size
    ) {
        HotReviewResponses responses = matchReviewFacadeService.getHotReviews(size);
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, responses));
    }
}
