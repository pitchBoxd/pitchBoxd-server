package com.example.pitchboxd.team.infrastructure;

import com.example.pitchboxd.team.domain.Team;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByNaverId(String naverCode);
}
