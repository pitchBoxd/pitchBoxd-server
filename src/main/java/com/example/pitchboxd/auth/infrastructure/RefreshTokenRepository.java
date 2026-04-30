package com.example.pitchboxd.auth.infrastructure;

import com.example.pitchboxd.auth.domain.RefreshToken;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenValue(String refreshTokenValue);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiredAt < :now")
    int deleteAllExpiredSince(@Param("now") LocalDateTime now); // 반환값 int는 지워진 레코드 수
}
