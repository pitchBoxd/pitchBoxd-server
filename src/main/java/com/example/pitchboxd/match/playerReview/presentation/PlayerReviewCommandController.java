package com.example.pitchboxd.match.playerReview.presentation;

import com.example.pitchboxd.auth.presentation.LoginUserId;
import com.example.pitchboxd.global.dto.response.SuccessResponse;
import com.example.pitchboxd.match.matchReview.dto.response.LikeToggleResponse;
import com.example.pitchboxd.match.playerReview.dto.request.PlayerReviewCreateRequest;
import com.example.pitchboxd.match.playerReview.dto.request.PlayerReviewUpdateRequest;
import com.example.pitchboxd.match.playerReview.dto.response.PlayerReviewCreateResponse;
import com.example.pitchboxd.match.playerReview.dto.response.PlayerReviewUpdateResponse;
import com.example.pitchboxd.match.playerReview.service.facade.PlayerReviewFacadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "PlayerReviewCommand API", description = "PlayerReview 커맨드 API 명세")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PlayerReviewCommandController {

    private final PlayerReviewFacadeService playerReviewFacadeService;

    @Operation(summary = "선수 리뷰 제출", description = "특정 경기에서 활약한 선수의 리뷰를 저장합니다.")
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

    @Operation(summary = "선수 리뷰 추천", description = "선수 리뷰에 좋아요를 누르거나 취소합니다.")
    @PostMapping("/player-reviews/{playerReviewId}/likes")
    public ResponseEntity<SuccessResponse<LikeToggleResponse>> toggleLike(
            @PathVariable Long playerReviewId,
            @LoginUserId Long userId
    ) {
        LikeToggleResponse response = playerReviewFacadeService.toggleLike(playerReviewId, userId);
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @Operation(summary = "선수 리뷰 수정", description = "선수 리뷰를 수정합니다.")
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

    @Operation(summary = "선수 리뷰 삭제", description = "선수 리뷰를 삭제합니다.")
    @DeleteMapping("/player-reviews/{playerReviewId}")
    public ResponseEntity<SuccessResponse<Void>> deleteReview(
            @PathVariable Long playerReviewId,
            @LoginUserId Long userId
    ) {
        playerReviewFacadeService.deleteReview(playerReviewId, userId);
        HttpStatus status = HttpStatus.NO_CONTENT;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }
}
