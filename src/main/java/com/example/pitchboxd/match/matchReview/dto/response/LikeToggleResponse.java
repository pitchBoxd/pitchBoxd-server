package com.example.pitchboxd.match.matchReview.dto.response;

import com.example.pitchboxd.match.matchReview.domain.MatchReview;
import com.example.pitchboxd.match.playerReview.domain.PlayerReview;

public record LikeToggleResponse(
        boolean isLiked,     // 현재 유저가 좋아요를 누른 상태인지 (true: 좋아요 함, false: 취소함)
        long totalLikeCount  // 반영된 최종 좋아요 총 개수
) {

    public static LikeToggleResponse of(boolean isLiked, MatchReview matchReview) {
        return new LikeToggleResponse(isLiked, matchReview.getLikeCount());
    }

    public static LikeToggleResponse of(boolean isLiked, PlayerReview playerReview) {
        return new LikeToggleResponse(isLiked, playerReview.getLikeCount());
    }
}
