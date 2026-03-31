package com.example.pitchboxd.match.playerStatistics.infrastructure;

import com.example.pitchboxd.match.playerStatistics.domain.PlayerMatchStatistics;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerMatchStatisticsRepository extends JpaRepository<PlayerMatchStatistics, Long> {

    Optional<PlayerMatchStatistics> findByMatchIdAndPlayerId(Long matchId, Long playerId);
}
