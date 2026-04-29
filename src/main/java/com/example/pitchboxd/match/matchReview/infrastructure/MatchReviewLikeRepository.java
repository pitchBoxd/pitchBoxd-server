package com.example.pitchboxd.match.matchReview.infrastructure;

import com.example.pitchboxd.match.matchReview.domain.MatchReviewLike;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchReviewLikeRepository extends JpaRepository<MatchReviewLike, Long> {

    boolean existsByMatchReviewIdAndUserId(Long matchReviewId, Long userId);

    void deleteByMatchReviewIdAndUserId(Long matchReviewId, Long userId);

    List<MatchReviewLike> findByUserIdAndMatchReviewIdIn(Long userId, List<Long> matchReviewIds);
}
