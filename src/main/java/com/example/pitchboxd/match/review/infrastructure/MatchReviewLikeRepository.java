package com.example.pitchboxd.match.review.infrastructure;

import com.example.pitchboxd.match.review.domain.MatchReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchReviewLikeRepository extends JpaRepository<MatchReviewLike, Long> {

    boolean existsByMatchReviewIdAndUserId(Long matchReviewId, Long userId);

    void deleteByMatchReviewIdAndUserId(Long matchReviewId, Long userId);
}
