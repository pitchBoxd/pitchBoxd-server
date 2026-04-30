package com.example.pitchboxd.user.infrastructure;

import com.example.pitchboxd.user.domain.Provider;
import com.example.pitchboxd.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Optional<User> findByProviderAndProviderKey(Provider provider, String googleSub);

    boolean existsByProviderAndProviderKey(Provider provider, String googleSub);
}
