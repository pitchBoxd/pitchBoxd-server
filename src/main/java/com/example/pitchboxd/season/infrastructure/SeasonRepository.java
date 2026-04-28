package com.example.pitchboxd.season.infrastructure;

import com.example.pitchboxd.season.domain.Season;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeasonRepository extends JpaRepository<Season, Long> {
}
