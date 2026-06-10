package com.example.pitchboxd.home.service;

import com.example.pitchboxd.home.dto.response.HomeMatchResponse;
import com.example.pitchboxd.home.dto.response.HomeResponses;
import com.example.pitchboxd.match.core.domain.MatchFilter;
import com.example.pitchboxd.match.core.dto.response.MatchResponse;
import com.example.pitchboxd.match.core.infrastructure.dto.MatchSummary;
import com.example.pitchboxd.match.core.service.domain.MatchQueryService;
import com.example.pitchboxd.match.matchReview.domain.HotReviews;
import com.example.pitchboxd.match.matchReview.dto.response.HotReviewResponse;
import com.example.pitchboxd.match.matchReview.service.domain.MatchReviewQueryService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeFacadeService {

    private final MatchQueryService matchQueryService;
    private final MatchReviewQueryService matchReviewQueryService;

    public HomeResponses getHomeData(MatchFilter state, Long seasonId) {
        LocalDateTime threshold = matchReviewQueryService.getReviewableThreshold();
        List<MatchSummary> matchSummaries = matchQueryService.findMatches(seasonId, state, threshold);

        List<Long> matchIds = matchSummaries.stream()
                .map(MatchSummary::id)
                .toList();

        HotReviews hotReviews = matchReviewQueryService.getHotReviewsByMatchIds(matchIds);

        List<HomeMatchResponse> homeMatchResponses = new ArrayList<>();
        for (MatchSummary matchSummary : matchSummaries) {
            List<HotReviewResponse> hotReviewResponses = hotReviews.getTopReviews(matchSummary.id(), 3).stream()
                    .map(HotReviewResponse::of)
                    .toList();

            homeMatchResponses.add(HomeMatchResponse.of(MatchResponse.of(matchSummary), hotReviewResponses));
        }

        return HomeResponses.of(homeMatchResponses);
    }
}
