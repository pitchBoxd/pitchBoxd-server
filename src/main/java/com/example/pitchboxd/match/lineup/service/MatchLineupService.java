package com.example.pitchboxd.match.lineup.service;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.match.lineup.domain.MatchLineup;
import com.example.pitchboxd.match.lineup.infrastructure.MatchLineupRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchLineupService {

    private final MatchLineupRepository matchLineupRepository;

    public MatchLineup findMatchLineup(Long matchId, Long playerId) {
        return matchLineupRepository.findByMatchIdAndPlayerId(matchId, playerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_LINEUP_NOT_FOUND));
    }

    public List<MatchLineup> createAllMatchLineup(List<MatchLineup> matchLineups) {
        return matchLineupRepository.saveAll(matchLineups);
    }
}
