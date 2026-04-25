package com.example.pitchboxd.user.presentation;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.auth.application.TokenManager;
import com.example.pitchboxd.support.DatabaseCleaner;
import com.example.pitchboxd.user.application.UserService;
import com.example.pitchboxd.user.dto.request.UserCreateRequest;
import com.example.pitchboxd.user.dto.response.NicknameAvailabilityResponse;
import com.example.pitchboxd.user.dto.response.UserCreateResponse;
import com.example.pitchboxd.user.dto.response.UserResponse;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DatabaseCleaner dbCleaner;

    @Autowired
    private TokenManager tokenManager;

    @Autowired
    private UserService userService; // Add UserService to directly create a user for token generation

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        dbCleaner.clean();
    }

    @AfterEach
    void down() {
        dbCleaner.clean();
    }

    @Test
    void 유저를_생성한다() {
        // given
        UserCreateRequest request = new UserCreateRequest("yush", "yush@example.com", "password123!");

        // when
        UserCreateResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/users")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .jsonPath()
                .getObject("data", UserCreateResponse.class);

        // then
        assertThat(response.id()).isNotNull();
    }

    @Test
    void 회원을_탈퇴한다() {
        // given
        String email = "delete@example.com";
        String password = "password123!";
        UserCreateRequest request = new UserCreateRequest("deleteTarget", email, password);

        UserCreateResponse createdUser = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(request)
                .post("/api/v1/users")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .jsonPath()
                .getObject("data", UserCreateResponse.class);

        String accessToken = tokenManager.createAccessToken(createdUser.id(), email);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when().delete("/api/v1/users")
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    void 내_정보를_조회한다() {
        // given
        String username = "loggedInUser";
        String email = "loggedin@example.com";
        String password = "securepassword";
        UserCreateRequest userCreateRequest = new UserCreateRequest(username, email, password);
        UserCreateResponse createdUser = userService.addUser(userCreateRequest); // Directly create user for simplicity

        String accessToken = tokenManager.createAccessToken(createdUser.id(), email);

        // when
        UserResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when().get("/api/v1/users/me")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", UserResponse.class);

        // then
        assertAll(
                () -> assertThat(response.id()).isEqualTo(createdUser.id()),
                () -> assertThat(response.nickname()).isEqualTo(username)
        );
    }

    @Test
    void 중복된_닉네임인지_확인한다_중복인_경우() {
        // given
        String nickname = "duplicatedNickname";
        userService.addUser(new UserCreateRequest(nickname, "test@example.com", "password123!"));

        // when
        NicknameAvailabilityResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .param("nickname", nickname)
                .when().get("/api/v1/users/nickname/exist")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", NicknameAvailabilityResponse.class);

        // then
        assertThat(response.isDuplicated()).isTrue();
    }

    @Test
    void 중복된_닉네임인지_확인한다_중복이_아닌_경우() {
        // given
        String nickname = "uniqueNickname";

        // when
        NicknameAvailabilityResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .param("nickname", nickname)
                .when().get("/api/v1/users/nickname/exist")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", NicknameAvailabilityResponse.class);

        // then
        assertThat(response.isDuplicated()).isFalse();
    }
}
