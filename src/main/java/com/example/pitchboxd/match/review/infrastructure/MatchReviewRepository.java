package com.example.pitchboxd.match.review.infrastructure;

import com.example.pitchboxd.match.review.domain.MatchReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchReviewRepository extends JpaRepository<MatchReview, Long> {
    boolean existsByMatchIdAndUserId(Long matchId, Long userId);
}
