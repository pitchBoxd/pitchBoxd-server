package com.example.pitchboxd.match.matchReview.presentation;

import com.example.pitchboxd.auth.presentation.LoginUserId;
import com.example.pitchboxd.global.dto.response.SuccessResponse;
import com.example.pitchboxd.match.matchReview.dto.request.MatchReviewCreateRequest;
import com.example.pitchboxd.match.matchReview.dto.request.MatchReviewUpdateRequest;
import com.example.pitchboxd.match.matchReview.dto.response.LikeToggleResponse;
import com.example.pitchboxd.match.matchReview.dto.response.MatchReviewCreateResponse;
import com.example.pitchboxd.match.matchReview.dto.response.MatchReviewUpdateResponse;
import com.example.pitchboxd.match.matchReview.service.facade.MatchReviewFacadeService;
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

@Tag(name = "MatchReviewCommand API", description = "MatchReview 커맨드 API 명세")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MatchReviewCommandController {

    private final MatchReviewFacadeService matchReviewFacadeService;

    @Operation(summary = "매치 리뷰 제출", description = "특정 경기의 리뷰를 저장합니다.")
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

    @Operation(summary = "매치 리뷰 추천", description = "특정 리뷰에 좋아요를 누르거나 취소합니다.")
    @PostMapping("/match-reviews/{matchReviewId}/likes")
    public ResponseEntity<SuccessResponse<LikeToggleResponse>> toggleLike(
            @PathVariable Long matchReviewId,
            @LoginUserId Long userId
    ) {
        LikeToggleResponse response = matchReviewFacadeService.toggleLike(matchReviewId, userId);
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @Operation(summary = "매치 리뷰 수정", description = "특정 경기의 리뷰를 수정합니다.")
    @PatchMapping("/match-reviews/{matchReviewId}")
    public ResponseEntity<SuccessResponse<MatchReviewUpdateResponse>> patchReview(
            @PathVariable Long matchReviewId,
            @LoginUserId Long userId,
            @Valid @RequestBody MatchReviewUpdateRequest request
    ) {
        MatchReviewUpdateResponse response = matchReviewFacadeService.updateMatchReview(matchReviewId, userId, request);
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @Operation(summary = "매치 리뷰 삭제", description = "특정 경기의 리뷰를 삭제합니다.")
    @DeleteMapping("/match-reviews/{matchReviewId}")
    public ResponseEntity<SuccessResponse<Void>> deleteReview(
            @PathVariable Long matchReviewId,
            @LoginUserId Long userId
    ) {
        matchReviewFacadeService.deleteMatchReview(matchReviewId, userId);
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }
}
