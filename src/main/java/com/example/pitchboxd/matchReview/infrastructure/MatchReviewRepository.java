package com.example.pitchboxd.matchReview.infrastructure;

import com.example.pitchboxd.matchReview.domain.MatchReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchReviewRepository extends JpaRepository<MatchReview, Long> {
    boolean existsByMatchIdAndUserId(Long matchId, Long userId);
}
