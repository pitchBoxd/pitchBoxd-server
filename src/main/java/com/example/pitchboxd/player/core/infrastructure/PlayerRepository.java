package com.example.pitchboxd.player.core.infrastructure;

import com.example.pitchboxd.player.core.domain.Player;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {
}
