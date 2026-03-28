package com.example.pitchboxd.matchLineup.service;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.matchLineup.domain.MatchLineup;
import com.example.pitchboxd.matchLineup.infrastructure.MatchLineupRepository;
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
}
