package com.example.pitchboxd.player.infrastructure;

import com.example.pitchboxd.player.domain.Player;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {
}
