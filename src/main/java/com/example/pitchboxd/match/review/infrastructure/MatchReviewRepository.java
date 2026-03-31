package com.example.pitchboxd.match.review.infrastructure;

import com.example.pitchboxd.match.review.domain.MatchReview;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface MatchReviewRepository extends JpaRepository<MatchReview, Long> {
    boolean existsByMatchIdAndUserId(Long matchId, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE) // SELECT ... FOR UPDATE 쿼리가 나갑니다.
    @Query("select m from MatchReview m where m.id = :id")
    Optional<MatchReview> findByIdWithPessimisticLock(Long id);
}
