package com.example.pitchboxd.auth.presentation;

import com.example.pitchboxd.auth.application.TokenManager;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.support.DatabaseCleaner;
import com.example.pitchboxd.support.FakeTokenManager;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AuthControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private TokenManager tokenManager;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public TokenManager tokenManager() {
            return new FakeTokenManager();
        }
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.clean();
    }

    @AfterEach
    void tearDown() {
        databaseCleaner.clean();
    }

    @Test
    void 만료된_엑세스_토큰으로_요청을_보내면_401_에러와_함께_토큰_만료_메시지를_반환한다() {
        // given
        FakeTokenManager fakeTokenManager = (FakeTokenManager) tokenManager;
        String expiredAccessToken = fakeTokenManager.createExpiredAccessToken(1L, "test@example.com");

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + expiredAccessToken)
                .when()
                .post("/api/v1/auth/logout")
                .then().log().all()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("code", org.hamcrest.Matchers.equalTo(ErrorCode.TOKEN_EXPIRED.getCode()))
                .body("message", org.hamcrest.Matchers.equalTo(ErrorCode.TOKEN_EXPIRED.getMessage()));
    }
}
