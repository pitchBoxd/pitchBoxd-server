package com.example.pitchboxd.match.core.service.facade;

import com.example.pitchboxd.global.domain.ClockHolder;
import com.example.pitchboxd.match.core.domain.Scope;
import com.example.pitchboxd.match.core.dto.response.MatchResponses;
import com.example.pitchboxd.match.core.service.domain.MatchQueryService;
import com.example.pitchboxd.match.core.service.domain.dto.MatchSummary;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchFacadeService {

    private final ClockHolder clockHolder;
    private final MatchQueryService matchQueryService;

    public MatchResponses findMatchesByScope(Scope scope) {
        LocalDate today = clockHolder.now().toLocalDate();

        if (scope == Scope.THIS_WEEK) {
            LocalDateTime startOfWeek = today.with(DayOfWeek.MONDAY).atStartOfDay();
            LocalDateTime endOfWeek = today.with(DayOfWeek.SUNDAY).atTime(LocalTime.MAX);

            List<MatchSummary> matchSummaries = matchQueryService.findMatchesByScope(startOfWeek, endOfWeek);

            return MatchResponses.of(matchSummaries);
        }

        if (scope == Scope.THIS_ROUND) {
            return null;
        }

        return null;
    }
}
