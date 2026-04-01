package com.example.pitchboxd.match.playerReview.presentation;

import com.example.pitchboxd.auth.presentation.LoginUserId;
import com.example.pitchboxd.global.dto.response.SuccessResponse;
import com.example.pitchboxd.match.matchReview.dto.response.LikeToggleResponse;
import com.example.pitchboxd.match.playerReview.dto.request.PlayerReviewCreateRequest;
import com.example.pitchboxd.match.playerReview.dto.request.PlayerReviewUpdateRequest;
import com.example.pitchboxd.match.playerReview.dto.response.PlayerReviewCreateResponse;
import com.example.pitchboxd.match.playerReview.dto.response.PlayerReviewUpdateResponse;
import com.example.pitchboxd.match.playerReview.service.facade.PlayerReviewFacadeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
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

    @PostMapping("/player-reviews/{playerReviewId}/likes")
    public ResponseEntity<SuccessResponse<LikeToggleResponse>> toggleLike(
            @PathVariable Long playerReviewId,
            @LoginUserId Long userId
    ) {
        LikeToggleResponse response = playerReviewFacadeService.toggleLike(playerReviewId, userId);
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @PatchMapping("/player-reviews/{playerReviewId}")
    public ResponseEntity<SuccessResponse<PlayerReviewUpdateResponse>> patchReview(
            @PathVariable Long playerReviewId,
            @LoginUserId Long userId,
            @Valid @RequestBody PlayerReviewUpdateRequest request
    ) {
        PlayerReviewUpdateResponse response = playerReviewFacadeService.updateReview(request, playerReviewId, userId);
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }
}
