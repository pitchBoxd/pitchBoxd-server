package com.example.pitchboxd.match.playerReview.infrastructure;

import com.example.pitchboxd.match.playerReview.domain.PlayerReview;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface PlayerReviewRepository extends JpaRepository<PlayerReview, Long> {
    boolean existsByMatchIdAndPlayerIdAndUserId(Long matchId, Long playerId, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from PlayerReview m where m.id = :id")
    Optional<PlayerReview> findByIdWithPessimisticLock(Long id);
}
