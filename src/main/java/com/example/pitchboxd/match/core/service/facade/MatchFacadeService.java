package com.example.pitchboxd.match.core.service.facade;

import com.example.pitchboxd.match.core.dto.response.MatchResponses;
import com.example.pitchboxd.match.core.infrastructure.dto.MatchSummary;
import com.example.pitchboxd.match.core.service.domain.MatchQueryService;
import com.example.pitchboxd.match.matchReview.service.facade.MatchReviewFacadeService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchFacadeService {

    private final MatchQueryService matchQueryService;
    private final MatchReviewFacadeService matchReviewFacadeService;

    public MatchResponses findReviewableMatches() {
        LocalDateTime threshold = matchReviewFacadeService.getReviewableThreshold();
        List<MatchSummary> matchSummaries = matchQueryService.findRecentlyFinishedMatches(threshold);

        return MatchResponses.of(matchSummaries);
    }
}
