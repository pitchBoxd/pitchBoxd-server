package com.example.pitchboxd.player.review.presentation;

import com.example.pitchboxd.auth.presentation.LoginUserId;
import com.example.pitchboxd.global.dto.response.SuccessResponse;
import com.example.pitchboxd.player.review.dto.request.PlayerReviewCreateRequest;
import com.example.pitchboxd.player.review.dto.response.PlayerReviewCreateResponse;
import com.example.pitchboxd.player.review.service.facade.PlayerReviewFacadeService;
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
public class PlayerReviewCommandController {

    private final PlayerReviewFacadeService playerReviewFacadeService;

    @PostMapping("/matches/{matchId}/player-reviews")
    public ResponseEntity<SuccessResponse<PlayerReviewCreateResponse>> createReview(
            @PathVariable Long matchId,
            @LoginUserId Long userId,
            @Valid @RequestBody PlayerReviewCreateRequest request
    ) {
        PlayerReviewCreateResponse response = playerReviewFacadeService.submitReview(request, matchId, userId);

        HttpStatus status = HttpStatus.CREATED;
        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }
}
