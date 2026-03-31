package com.example.pitchboxd.match.playerReview.infrastructure;

import com.example.pitchboxd.match.playerReview.domain.PlayerReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerReviewRepository extends JpaRepository<PlayerReview, Long> {
    boolean existsByMatchIdAndPlayerIdAndUserId(Long matchId, Long playerId, Long userId);
}
