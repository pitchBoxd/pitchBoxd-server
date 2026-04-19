package com.example.pitchboxd.match.core.service.facade;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.match.core.dto.response.MatchResponses;
import com.example.pitchboxd.match.core.infrastructure.dto.MatchSummary;
import com.example.pitchboxd.match.core.service.domain.MatchQueryService;
import com.example.pitchboxd.match.matchReview.service.facade.MatchReviewFacadeService;
import com.example.pitchboxd.user.application.UserService;
import com.example.pitchboxd.user.domain.User;
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

    public MatchResponses findReviewableMatches(Long userId, String filter) {
        log.info("userId: {}, filter: {}", userId, filter);
        if (userId == null && "my".equals(filter)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        LocalDateTime threshold = matchReviewFacadeService.getReviewableThreshold();

        if (userId != null && "my".equals(filter)) {
            User user = userService.findById(userId);
            Long favoriteTeamId = user.getFavoriteTeamId();

            List<MatchSummary> myMatchSummaries = matchQueryService.findRecentlyFinishedMatchesByTeam(threshold,
                    favoriteTeamId);
            return MatchResponses.of(myMatchSummaries);
        }

        List<MatchSummary> matchSummaries = matchQueryService.findRecentlyFinishedMatches(threshold);

        return MatchResponses.of(matchSummaries);
    }
}
