package com.example.pitchboxd.team.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.auth.application.TokenManager;
import com.example.pitchboxd.support.DatabaseCleaner;
import com.example.pitchboxd.team.domain.Team;
import com.example.pitchboxd.team.dto.response.TeamResponses;
import com.example.pitchboxd.team.infrastructure.TeamRepository;
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
class TeamControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private TeamRepository teamRepository;

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
    void 모든_팀을_조회한다() {
        // given
        String teamNameSeoul = "FC서울";
        String teamNameJeju = "제주SK";
        teamRepository.save(new Team(teamNameSeoul, "asdfdd"));
        teamRepository.save(new Team(teamNameJeju, "ggekeke"));

        // when
        TeamResponses response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/teams")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", TeamResponses.class);

        // then
        assertAll(
                () -> assertThat(response.teamResponses()).hasSize(2),
                () -> assertThat(response.teamResponses())
                        .extracting("teamName")
                        .containsExactlyInAnyOrder(teamNameSeoul, teamNameJeju)
        );
    }

    @Test
    void 팀별_팔로워_수를_조회한다() {
        // given
        Team teamSeoul = teamRepository.save(new Team("FC서울", "naverSeoul"));
        Team teamJeju = teamRepository.save(new Team("제주SK", "naverJeju"));

        // User 1, 2 support Seoul
        userRepository.save(new User("fan1", "fan1@gmail.com", "1234!", teamSeoul.getId()));
        userRepository.save(new User("fan2", "fan2@gmail.com", "1234!", teamSeoul.getId()));
        // User 3 supports Jeju
        userRepository.save(new User("fan3", "fan3@gmail.com", "1234!", teamJeju.getId()));

        // when
        com.example.pitchboxd.team.dto.response.TeamFollowerCountResponses response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/teams/followers")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", com.example.pitchboxd.team.dto.response.TeamFollowerCountResponses.class);

        // then
        assertAll(
                () -> assertThat(response.teamFollowerCountResponses()).hasSize(2),
                () -> assertThat(response.teamFollowerCountResponses())
                        .extracting("teamName")
                        .containsExactlyInAnyOrder("FC서울", "제주SK"),
                () -> {
                    com.example.pitchboxd.team.dto.response.TeamFollowerCountResponse seoulResult = response.teamFollowerCountResponses().stream()
                            .filter(t -> t.teamName().equals("FC서울"))
                            .findFirst()
                            .orElseThrow();
                    assertThat(seoulResult.followerCount()).isEqualTo(2);
                },
                () -> {
                    com.example.pitchboxd.team.dto.response.TeamFollowerCountResponse jejuResult = response.teamFollowerCountResponses().stream()
                            .filter(t -> t.teamName().equals("제주SK"))
                            .findFirst()
                            .orElseThrow();
                    assertThat(jejuResult.followerCount()).isEqualTo(1);
                }
        );
    }
}
