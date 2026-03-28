package com.example.pitchboxd.playerReview.infrastructure;

import com.example.pitchboxd.playerReview.domain.PlayerReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerReviewRepository extends JpaRepository<PlayerReview, Long> {
    boolean existsByMatchIdAndPlayerIdAndUserId(Long matchId, Long playerId, Long userId);
}
