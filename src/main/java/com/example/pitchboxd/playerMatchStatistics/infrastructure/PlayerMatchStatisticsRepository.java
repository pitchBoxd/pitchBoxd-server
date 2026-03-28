package com.example.pitchboxd.playerMatchStatistics.infrastructure;

import com.example.pitchboxd.playerMatchStatistics.domain.PlayerMatchStatistics;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerMatchStatisticsRepository extends JpaRepository<PlayerMatchStatistics, Long> {

    Optional<PlayerMatchStatistics> findByMatchIdAndPlayerId(Long matchId, Long playerId);
}
