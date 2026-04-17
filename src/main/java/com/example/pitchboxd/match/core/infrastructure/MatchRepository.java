package com.example.pitchboxd.match.core.infrastructure;

import com.example.pitchboxd.match.core.domain.Match;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<Match, Long> {

}
