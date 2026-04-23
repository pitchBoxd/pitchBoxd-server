package com.example.pitchboxd.auth.infrastructure;

import com.example.pitchboxd.auth.domain.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenValue(String refreshTokenValue);
}
