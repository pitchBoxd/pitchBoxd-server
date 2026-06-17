package com.example.pitchboxd.match.core.infrastructure;

import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<Match, Long> {

    Optional<Match> findByNaverId(String matchCode);

    List<Match> findByStatusAndStartTimeBetween(MatchStatus status, LocalDateTime timeLimit,
                                                LocalDateTime now);

    List<Match> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
}
