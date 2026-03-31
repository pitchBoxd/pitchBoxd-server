package com.example.pitchboxd.match.playerReview.infrastructure;


import com.example.pitchboxd.match.playerReview.domain.PlayerReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerReviewLikeRepository extends JpaRepository<PlayerReviewLike, Long> {
    boolean existsByPlayerReviewIdAndUserId(Long playerReviewId, Long userId);

    void deleteByPlayerReviewIdAndUserId(Long playerReviewId, Long userId);
}
