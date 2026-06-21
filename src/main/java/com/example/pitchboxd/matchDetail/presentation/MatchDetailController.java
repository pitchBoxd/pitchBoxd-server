package com.example.pitchboxd.matchDetail.presentation;

import com.example.pitchboxd.auth.presentation.LoginUserId;
import com.example.pitchboxd.global.dto.response.SuccessResponse;
import com.example.pitchboxd.match.matchReview.domain.ReviewSortType;
import com.example.pitchboxd.matchDetail.dto.response.MatchDetailMatchReviewResponses;
import com.example.pitchboxd.matchDetail.dto.response.MatchDetailPersonalResponse;
import com.example.pitchboxd.matchDetail.dto.response.MatchDetailResultResponse;
import com.example.pitchboxd.matchDetail.dto.response.MatchDetailStatsResponse;
import com.example.pitchboxd.matchDetail.dto.response.MatchReviewSliceResponse;
import com.example.pitchboxd.matchDetail.dto.response.PlayerReviewSliceResponse;
import com.example.pitchboxd.matchDetail.service.MatchDetailFacadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Match 화면 API", description = "경기 화면 API 명세")
@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
public class MatchDetailController {

    private final MatchDetailFacadeService matchDetailFacadeService;

    @Operation(summary = "경기 결과 및 라인업 데이터", description = "경기 상세 페이지의 결과 점수 및 양팀 라인업 데이터를 가져옵니다.")
    @GetMapping("{matchId}/detail/result")
    public ResponseEntity<SuccessResponse<MatchDetailResultResponse>> getMatchResultData(@PathVariable Long matchId) {
        MatchDetailResultResponse response = matchDetailFacadeService.getMatchResultData(matchId);
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @Operation(summary = "경기 평점 통계 및 하이라이트 데이터", description = "경기 상세 페이지의 실시간 평점 평균, 별점 분포도, MOM 및 Top3 플레이어 정보를 가져옵니다.")
    @GetMapping("{matchId}/detail/stats")
    public ResponseEntity<SuccessResponse<MatchDetailStatsResponse>> getMatchStatsData(@PathVariable Long matchId) {
        MatchDetailStatsResponse response = matchDetailFacadeService.getMatchStatsData(matchId);
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @Operation(summary = "경기 페이지 핫한 경기 리뷰", description = "해당 경기의 핫한 경기 리뷰를 가져옵니다. 갯수를 결정할 수 있고, 기본값은 5입니다.")
    @GetMapping("{matchId}/match-reviews/hot")
    public ResponseEntity<SuccessResponse<MatchDetailMatchReviewResponses>> getMatchHotReviewData(
            @PathVariable Long matchId,
            @LoginUserId(required = false) Long userId,
            @RequestParam(defaultValue = "5") int limit

    ) {
        MatchDetailMatchReviewResponses responses = matchDetailFacadeService.getMatchHotReviews(matchId, userId, limit);
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, responses));
    }

    @Operation(summary = "경기 페이지 개인 평가 데이터", description = "로그인 유저의 경기 및 선수 평점/한줄평 조회 데이터를 가져옵니다.")
    @GetMapping("{matchId}/detail/personal")
    public ResponseEntity<SuccessResponse<MatchDetailPersonalResponse>> getMatchPersonalData(
            @PathVariable Long matchId,
            @LoginUserId(required = false) Long userId
    ) {
        MatchDetailPersonalResponse response = matchDetailFacadeService.getMatchPersonalData(matchId, userId);
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @Operation(summary = "경기 페이지 전체 경기 리뷰 조회 (무한 스크롤 페이징)", description = "최신순(LATEST) 및 추천순(LIKE)으로 리뷰를 페이징 조회합니다.")
    @GetMapping("{matchId}/match-reviews")
    public ResponseEntity<SuccessResponse<MatchReviewSliceResponse>> getMatchReviews(
            @PathVariable Long matchId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(required = false) Long cursorLikeCount,
            @RequestParam(defaultValue = "LATEST") ReviewSortType sort,
            @RequestParam(defaultValue = "10") int size,
            @LoginUserId(required = false) Long userId
    ) {
        MatchReviewSliceResponse response = matchDetailFacadeService.getMatchReviews(matchId, cursorId, cursorLikeCount,
                sort, size, userId);
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    @Operation(summary = "경기 페이지 특정 선수 리뷰 조회 (무한 스크롤 페이징)", description = "선수 카드 클릭 시 최신순(LATEST) 및 추천순(LIKE)으로 플레이어 리뷰를 페이징 조회합니다.")
    @GetMapping("{matchId}/players/{playerId}/player-reviews")
    public ResponseEntity<SuccessResponse<PlayerReviewSliceResponse>> getPlayerReviews(
            @PathVariable Long matchId,
            @PathVariable Long playerId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(required = false) Long cursorLikeCount,
            @RequestParam(defaultValue = "LATEST") ReviewSortType sort,
            @RequestParam(defaultValue = "10") int size,
            @LoginUserId(required = false) Long userId
    ) {
        PlayerReviewSliceResponse response = matchDetailFacadeService.getPlayerReviews(matchId, playerId, cursorId,
                cursorLikeCount, sort, size, userId);
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }

    // TODO: 특정 경기의 모든 선수 리뷰를 조회하는 기능 추가? 할까말까~~
}
