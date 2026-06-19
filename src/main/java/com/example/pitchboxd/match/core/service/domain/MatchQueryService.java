package com.example.pitchboxd.match.core.service.domain;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.match.core.domain.MatchFilter;
import com.example.pitchboxd.match.core.infrastructure.MatchQueryRepository;
import com.example.pitchboxd.match.core.infrastructure.dto.MatchDetailStaticModel;
import com.example.pitchboxd.match.core.infrastructure.dto.MatchSummary;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class MatchQueryService {

    private final MatchQueryRepository matchQueryRepository;

    public MatchDetailStaticModel findMatchStaticDetailById(Long matchId) {
        return matchQueryRepository.findMatchStaticDetailById(matchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));
    }

    public List<MatchSummary> findRecentlyFinishedMatches(LocalDateTime finishedAtThreshold) {
        return matchQueryRepository.findFinishedMatchesSince(finishedAtThreshold);
    }

    public List<MatchSummary> findRecentlyFinishedMatchesByTeam(LocalDateTime finishedAtThreshold, Long teamId) {
        return matchQueryRepository.findFinishedMatchesSince(finishedAtThreshold, teamId);
    }

    public List<MatchSummary> findMatches(Long seasonId, MatchFilter state, LocalDateTime reviewableThreshold) {
        return matchQueryRepository.findMatches(seasonId, state, reviewableThreshold);
    }

    public List<MatchSummary> findMatchesWithFilters(Long teamId, Long seasonId, LocalDateTime now) {
        return matchQueryRepository.findMatchesWithFilters(teamId, seasonId, now);
    }
}
