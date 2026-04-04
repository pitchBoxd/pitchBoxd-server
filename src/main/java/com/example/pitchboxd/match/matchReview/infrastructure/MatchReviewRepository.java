package com.example.pitchboxd.match.matchReview.infrastructure;

import com.example.pitchboxd.match.matchReview.domain.MatchReview;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

public interface MatchReviewRepository extends JpaRepository<MatchReview, Long> {
    boolean existsByMatchIdAndUserId(Long matchId, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "3000")}) // 3초 대기
    @Query("select m from MatchReview m where m.id = :id")
    Optional<MatchReview> findByIdWithPessimisticLock(Long id);
}
