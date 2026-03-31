package com.example.pitchboxd.match.matchStatistics.infrastructure;

import com.example.pitchboxd.match.matchStatistics.domain.MatchStatistics;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MatchStatisticsRepository extends JpaRepository<MatchStatistics, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ms FROM MatchStatistics ms WHERE ms.matchId = :matchId")
    Optional<MatchStatistics> findByIdForUpdate(@Param("matchId") Long matchId);
}
