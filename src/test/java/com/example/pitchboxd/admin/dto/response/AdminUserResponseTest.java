package com.example.pitchboxd.admin.dto.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.user.domain.Provider;
import com.example.pitchboxd.user.domain.User;
import com.example.pitchboxd.user.domain.UserRole;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AdminUserResponseTest {

    @Test
    void 유저_엔티티를_AdminUserResponse로_정상_변환한다() {
        // given
        User user = new User("테스트유저", "test@example.com", "password123!", 1L, Provider.GOOGLE, "googleKey");
        user.assignRole(UserRole.USER);

        LocalDateTime now = LocalDateTime.now();
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "createdAt", now);

        // when
        AdminUserResponse response = AdminUserResponse.from(user);

        // then
        assertAll(
                () -> assertThat(response.id()).isEqualTo(1L),
                () -> assertThat(response.nickname()).isEqualTo("테스트유저"),
                () -> assertThat(response.email()).isEqualTo("test@example.com"),
                () -> assertThat(response.favoriteTeamId()).isEqualTo(1L),
                () -> assertThat(response.provider()).isEqualTo("GOOGLE"),
                () -> assertThat(response.role()).isEqualTo("USER"),
                () -> assertThat(response.createdAt()).isEqualTo(now)
        );
    }

    @Test
    void 유저_엔티티_변환_시_favoriteTeamId와_provider가_null인_경우도_정상_매핑된다() {
        // given
        User user = new User("테스트유저2", "test2@example.com", "password123!");
        user.assignRole(UserRole.USER);

        LocalDateTime now = LocalDateTime.now();
        ReflectionTestUtils.setField(user, "id", 2L);
        ReflectionTestUtils.setField(user, "createdAt", now);

        // when
        AdminUserResponse response = AdminUserResponse.from(user);

        // then
        assertAll(
                () -> assertThat(response.id()).isEqualTo(2L),
                () -> assertThat(response.nickname()).isEqualTo("테스트유저2"),
                () -> assertThat(response.email()).isEqualTo("test2@example.com"),
                () -> assertThat(response.favoriteTeamId()).isNull(),
                () -> assertThat(response.provider()).isNull(),
                () -> assertThat(response.role()).isEqualTo("USER"),
                () -> assertThat(response.createdAt()).isEqualTo(now)
        );
    }
}
