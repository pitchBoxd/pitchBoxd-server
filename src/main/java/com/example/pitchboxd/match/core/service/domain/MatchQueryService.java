package com.example.pitchboxd.match.core.service.domain;

import com.example.pitchboxd.match.core.infrastructure.MatchQueryRepository;
import com.example.pitchboxd.match.core.service.domain.dto.MatchSummary;
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

    public List<MatchSummary> findMatchesByScope(LocalDateTime from, LocalDateTime to) {
        return matchQueryRepository.findMatchesBetween(from, to);
    }
}
