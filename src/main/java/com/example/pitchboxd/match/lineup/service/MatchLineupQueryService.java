package com.example.pitchboxd.match.lineup.service;

import com.example.pitchboxd.match.lineup.infrastructure.MatchLineupQueryRepository;
import com.example.pitchboxd.match.lineup.infrastructure.dto.LineupPlayerModel;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchLineupQueryService {

    private final MatchLineupQueryRepository matchLineupQueryRepository;

    public List<LineupPlayerModel> findLineupAndPlayedPlayers(Long matchId) {
        return matchLineupQueryRepository.findLineupPlayersByMatchId(matchId);
    }
}
