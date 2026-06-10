package com.example.pitchboxd.match.matchReview.service.domain;

import com.example.pitchboxd.global.domain.ClockHolder;
import com.example.pitchboxd.match.matchReview.domain.HotReviews;
import com.example.pitchboxd.match.matchReview.domain.MatchReviewSubmitPolicy;
import com.example.pitchboxd.match.matchReview.infrastructure.MatchReviewQueryRepository;
import com.example.pitchboxd.match.matchReview.infrastructure.dto.HotReviewSummary;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchReviewQueryService {

    private final MatchReviewQueryRepository matchReviewQueryRepository;
    private final MatchReviewSubmitPolicy matchReviewSubmitPolicy;
    private final ClockHolder clockHolder;

    public LocalDateTime getReviewableThreshold() {
        LocalDateTime now = clockHolder.now();
        return matchReviewSubmitPolicy.getReviewableThreshold(now);
    }

    public List<HotReviewSummary> getTopHotReviewsByMatchId(Long matchId, int limit) {
        return matchReviewQueryRepository.findHotReviewsByMatchId(matchId, limit);
    }

    public HotReviews getHotReviewsByMatchIds(List<Long> matchIds) {
        return HotReviews.from(matchReviewQueryRepository.findHotReviewsByMatchIds(matchIds));
    }
}
