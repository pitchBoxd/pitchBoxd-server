package com.example.pitchboxd.match.playerStatistics.infrastructure;

import com.example.pitchboxd.match.playerStatistics.domain.PlayerStatistics;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlayerStatisticsRepository extends JpaRepository<PlayerStatistics, Long> {

    Optional<PlayerStatistics> findByMatchIdAndPlayerId(Long matchId, Long playerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pms FROM PlayerMatchStatistics pms WHERE pms.matchId = :matchId AND pms.playerId = :playerId")
    Optional<PlayerStatistics> findByMatchIdAndPlayerIdForUpdate(
            @Param("matchId") Long matchId,
            @Param("playerId") Long playerId
    );
}
