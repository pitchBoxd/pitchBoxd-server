package com.example.pitchboxd.player.infrastructure;

import com.example.pitchboxd.player.domain.Player;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByNaverId(String naverPlayerId);
}
