package com.example.pitchboxd.player.infrastructure;

import com.example.pitchboxd.player.domain.Player;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByNaverId(String naverPlayerId);

    Optional<Player> findByTeamIdAndName(Long teamId, String name);

    @Query("SELECT p.naverId FROM Player p WHERE p.naverId IN :naverIds")
    List<String> findNaverIdsByNaverIdIn(@Param("naverIds") Collection<String> naverIds);
}
