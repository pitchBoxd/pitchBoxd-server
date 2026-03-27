package com.example.pitchboxd.match.infrastructure;

import com.example.pitchboxd.match.domain.Match;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<Match, Long> {
}
