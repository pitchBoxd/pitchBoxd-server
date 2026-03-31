package com.example.pitchboxd.match.playerReview.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PlayerReviewCreateRequest(
        @NotNull(message = "선수 ID는 필수입니다.")
        Long playerId,

        @Size(max = 100, message = "한줄평은 100자를 초과할 수 없습니다.")
        String content,

        @NotNull(message = "평점은 필수입니다.")
        @Min(value = 0, message = "평점은 최소 0점 이상이어야 합니다.")
        @Max(value = 10, message = "평점은 최대 10점까지 가능합니다.")
        Integer point
) {
}
