package com.example.pitchboxd.match.core.service.facade;

import com.example.pitchboxd.match.core.domain.MatchFilter;
import com.example.pitchboxd.match.core.dto.response.MatchResponses;
import com.example.pitchboxd.match.core.infrastructure.dto.MatchSummary;
import com.example.pitchboxd.match.core.service.domain.MatchQueryService;
import com.example.pitchboxd.match.matchReview.service.facade.MatchReviewFacadeService;
import com.example.pitchboxd.user.application.UserService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MatchFacadeService {

    private final MatchQueryService matchQueryService;
    private final MatchReviewFacadeService matchReviewFacadeService;
    private final UserService userService;

    public MatchResponses findMatches(MatchFilter state, Long seasonId) {
        log.info("state: {}, seasonId: {}", state, seasonId);

        LocalDateTime threshold = matchReviewFacadeService.getReviewableThreshold();
        List<MatchSummary> matchSummaries = matchQueryService.findMatches(seasonId, state, threshold);

        return MatchResponses.of(matchSummaries);
    }
}
