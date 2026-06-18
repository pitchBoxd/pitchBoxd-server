package com.example.pitchboxd.match.playerReview.infrastructure;


import com.example.pitchboxd.match.playerReview.domain.PlayerReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlayerReviewLikeRepository extends JpaRepository<PlayerReviewLike, Long> {
    boolean existsByPlayerReviewIdAndUserId(Long playerReviewId, Long userId);

    void deleteByPlayerReviewIdAndUserId(Long playerReviewId, Long userId);

    @Query("SELECT prl.playerReviewId FROM PlayerReviewLike prl WHERE prl.playerReviewId IN :reviewIds AND prl.userId = :userId")
    java.util.List<Long> findLikedReviewIdsIn(
            @Param("reviewIds") java.util.Collection<Long> reviewIds,
            @Param("userId") Long userId
    );
}
