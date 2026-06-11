package com.example.pitchboxd.matchDetail.dto.response;

import java.util.List;

public record MatchDetailPersonalResponse(
        boolean isEvaluated,
        MyMatchReviewResponse myMatchReview,
        List<MyPlayerReviewResponse> myPlayerReviews
) {}
