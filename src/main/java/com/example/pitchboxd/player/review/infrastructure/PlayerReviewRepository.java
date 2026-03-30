package com.example.pitchboxd.player.review.infrastructure;

import com.example.pitchboxd.player.review.domain.PlayerReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerReviewRepository extends JpaRepository<PlayerReview, Long> {
    boolean existsByMatchIdAndPlayerIdAndUserId(Long matchId, Long playerId, Long userId);
}
