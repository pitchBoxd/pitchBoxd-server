package com.example.pitchboxd.season.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.auth.application.TokenManager;
import com.example.pitchboxd.season.domain.Season;
import com.example.pitchboxd.season.dto.response.SeasonResponses;
import com.example.pitchboxd.season.infrastructure.SeasonRepository;
import com.example.pitchboxd.support.DatabaseCleaner;
import com.example.pitchboxd.user.domain.User;
import com.example.pitchboxd.user.infrastructure.UserRepository;
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

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class SeasonControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private SeasonRepository seasonRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenManager tokenManager;

    private String accessToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.clean();

        User user = userRepository.save(new User("nickname", "email@gmail.com", "1234!"));
        accessToken = tokenManager.createAccessToken(user.getId(), user.getEmail());
    }

    @AfterEach
    void tearDown() {
        databaseCleaner.clean();
    }

    @Test
    void 모든_시즌을_조회한다() {
        // given
        seasonRepository.save(new Season("2024"));
        seasonRepository.save(new Season("2025"));

        // when
        SeasonResponses response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/seasons")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", SeasonResponses.class);

        // then
        assertAll(
                () -> assertThat(response.seasonResponses()).hasSize(2),
                () -> assertThat(response.seasonResponses())
                        .extracting("name")
                        .containsExactlyInAnyOrder("2024", "2025")
        );
    }
}
