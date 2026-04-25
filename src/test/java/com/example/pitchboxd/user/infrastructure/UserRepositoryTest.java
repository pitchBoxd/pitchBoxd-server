package com.example.pitchboxd.user.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.pitchboxd.support.DatabaseCleaner;
import com.example.pitchboxd.user.domain.User;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@Import(DatabaseCleaner.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();
    }

    @AfterEach
    void tearDown() {
        databaseCleaner.clean();
    }

    @Test
    void 이메일로_사용자를_조회한다() {
        // given
        String email = "test @example.com";
        User user = new User("nickname", email, "password");
        userRepository.save(user);

        // when
        Optional<User> foundUser = userRepository.findByEmail(email);

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo(email);
    }

    @Test
    void 존재하지_않는_이메일로_사용자를_조회하면_빈_Optional을_반환한다() {
        // given
        String email = "notfound@example.com";

        // when
        Optional<User> foundUser = userRepository.findByEmail(email);

        // then
        assertThat(foundUser).isEmpty();
    }

    @Test
    void 이메일이_존재하는_경우_true를_반환한다() {
        // given
        String email = "test@example.com";
        User user = new User("nickname", email, "password");
        userRepository.save(user);

        // when
        boolean exists = userRepository.existsByEmail(email);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void 이메일이_존재하지_않는_경우_false를_반환한다() {
        // given
        String email = "notfound@example.com";

        // when
        boolean exists = userRepository.existsByEmail(email);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void 닉네임이_존재하는_경우_true를_반환한다() {
        // given
        String nickname = "uniqueNickname";
        User user = new User(nickname, "nickname@example.com", "password");
        userRepository.save(user);

        // when
        boolean exists = userRepository.existsByNickname(nickname);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void 닉네임이_존재하지_않는_경우_false를_반환한다() {
        // given
        String nickname = "nonExistentNickname";

        // when
        boolean exists = userRepository.existsByNickname(nickname);

        // then
        assertThat(exists).isFalse();
    }
}
