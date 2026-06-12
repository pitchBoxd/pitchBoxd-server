package com.example.pitchboxd.match.matchReview.infrastructure;

import com.example.pitchboxd.match.matchReview.domain.MatchReview;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

public interface MatchReviewRepository extends JpaRepository<MatchReview, Long> {
    List<MatchReview> findAllByMatchId(Long matchId);
    
    // existing methods
    boolean existsByMatchIdAndUserId(Long matchId, Long userId);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "3000")}) // 3초 대기
    @Query("select m from MatchReview m where m.id = :id")
    Optional<MatchReview> findByIdWithPessimisticLock(Long id);
    
    Optional<MatchReview> findByMatchIdAndUserId(Long matchId, Long userId);
    
    @Query("select r.point as point, count(r) as count from MatchReview r where r.matchId = :matchId group by r.point")
    List<Object[]> countPointDistributionByMatchId(@Param("matchId") Long matchId);
}
