package com.example.pitchboxd.match.matchReview.service.domain;

import com.example.pitchboxd.match.matchReview.infrastructure.MatchReviewQueryRepository;
import com.example.pitchboxd.match.matchReview.infrastructure.dto.HotReviewSummary;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchReviewQueryService {

    private final MatchReviewQueryRepository matchReviewQueryRepository;

    public List<HotReviewSummary> getTopHotReviews(List<Long> reviewableMatchIds, int limit) {
        return matchReviewQueryRepository.findHotReviews(reviewableMatchIds, limit);
    }
}
