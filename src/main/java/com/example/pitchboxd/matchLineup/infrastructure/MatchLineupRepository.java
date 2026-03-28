package com.example.pitchboxd.matchLineup.infrastructure;

import com.example.pitchboxd.matchLineup.domain.MatchLineup;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchLineupRepository extends JpaRepository<MatchLineup, Long> {

    Optional<MatchLineup> findByMatchIdAndPlayerId(Long matchId, Long playerId);
}
