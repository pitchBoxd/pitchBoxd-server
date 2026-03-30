package com.example.pitchboxd.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.support.DatabaseCleaner;
import com.example.pitchboxd.user.domain.User;
import com.example.pitchboxd.user.dto.request.UserCreateRequest;
import com.example.pitchboxd.user.dto.response.EmailAvailabilityResponse;
import com.example.pitchboxd.user.dto.response.UserCreateResponse;
import com.example.pitchboxd.user.dto.response.UserResponse;
import com.example.pitchboxd.user.infrastructure.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
    void 유저를_정상적으로_추가한다() {
        // given
        UserCreateRequest request = new UserCreateRequest("테스트유저", "test@example.com", "password123!");

        // when
        UserCreateResponse response = userService.addUser(request);

        // then
        User savedUser = userRepository.findById(response.id()).orElseThrow();
        assertAll(
                () -> assertThat(savedUser.getNickname()).isEqualTo("테스트유저"),
                () -> assertThat(savedUser.getEmail()).isEqualTo("test@example.com"),
                () -> assertThat(passwordEncoder.matches("password123!", savedUser.getPassword())).isTrue()
        );
    }

    @Test
    void 이미_존재하는_이메일로_유저를_추가하면_예외가_발생한다() {
        // given
        userRepository.save(new User("기존유저", "test@example.com", "password123!"));
        UserCreateRequest request = new UserCreateRequest("새유저", "test@example.com", "password456!");

        // when & then
        assertThatThrownBy(() -> userService.addUser(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.USER_EMAIL_CONFLICT.getMessage());
    }

    @Test
    void 유저를_정상적으로_탈퇴시킨다() {
        // given
        User user = userRepository.save(new User("테스트유저", "test@example.com", "password123!"));

        // when
        userService.withdraw(user.getId());

        // then
        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

    @Test
    void 존재하지_않는_유저_아이디로_탈퇴를_시도하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> userService.withdraw(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    void 유저_정보를_정상적으로_조회한다() {
        // given
        String nickname = "테스트유저";
        User user = userRepository.save(new User(nickname, "test@example.com", "password123!"));

        // when
        UserResponse response = userService.getUserInfo(user.getId());

        // then
        assertAll(
                () -> assertThat(user.getId()).isEqualTo(response.id()),
                () -> assertThat(response.nickname()).isEqualTo(nickname)
        );
    }

    @Test
    void 존재하지_않는_유저_정보를_조회하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> userService.getUserInfo(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.USER_UNAUTHENTICATED.getMessage());
    }

    @Test
    void 유저_아이디_검증_시_일치하면_예외가_발생하지_않는다() {
        // given
        User user = userRepository.save(new User("테스트유저", "test@example.com", "password123!"));

        // when & then
        userService.validateUser(user, user.getId());
    }

    @Test
    void 유저_아이디_검증_시_일치하지_않으면_예외가_발생한다() {
        // given
        User user = userRepository.save(new User("테스트유저", "test@example.com", "password123!"));

        // when & then
        assertThatThrownBy(() -> userService.validateUser(user, 999L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.ACCESS_DENIED.getMessage());
    }

    @Test
    void 이메일_중복_여부를_확인한다() {
        // given
        String email = "test@example.com";
        userRepository.save(new User("테스트유저", email, "password123!"));

        // when
        EmailAvailabilityResponse response = userService.isEmailDuplicated(email);

        // then
        assertThat(response).isNotNull();
    }

    @Test
    void 유저_엔티티를_정상적으로_조회한다() {
        // given
        User user = userRepository.save(new User("테스트유저", "test@example.com", "password123!"));

        // when
        User result = userService.findById(user.getId());

        // then
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void 존재하지_않는_유저를_조회하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> userService.findById(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
    }
}
