package com.example.pitchboxd.match.matchReview.presentation;

import com.example.pitchboxd.auth.presentation.LoginUserId;
import com.example.pitchboxd.global.dto.response.SuccessResponse;
import com.example.pitchboxd.match.matchReview.dto.request.MatchReviewCreateRequest;
import com.example.pitchboxd.match.matchReview.dto.response.LikeToggleResponse;
import com.example.pitchboxd.match.matchReview.dto.response.MatchReviewCreateResponse;
import com.example.pitchboxd.match.matchReview.service.facade.MatchReviewFacadeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MatchReviewCommandController {

    private final MatchReviewFacadeService matchReviewFacadeService;

    @PostMapping("/matches/{matchId}/match-reviews")
    public ResponseEntity<SuccessResponse<MatchReviewCreateResponse>> createReview(
            @PathVariable Long matchId,
            @LoginUserId Long userId,
            @Valid @RequestBody MatchReviewCreateRequest request
    ) {
        MatchReviewCreateResponse response = matchReviewFacadeService.submitReview(request, matchId, userId);
        HttpStatus status = HttpStatus.CREATED;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @PostMapping("/match-reviews/{matchReviewId}/likes")
    public ResponseEntity<SuccessResponse<LikeToggleResponse>> toggleLike(
            @PathVariable Long matchReviewId,
            @LoginUserId Long userId
    ) {
        LikeToggleResponse response = matchReviewFacadeService.toggleLike(matchReviewId, userId);
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }
}
