package com.example.pitchboxd.playerReview.presentation;

import com.example.pitchboxd.global.security.JwtProvider;
import com.example.pitchboxd.player.domain.Player;
import com.example.pitchboxd.player.infrastructure.PlayerRepository;
import com.example.pitchboxd.playerReview.dto.request.PlayerReviewCreateRequest;
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
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PlayerReviewCommandControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private JwtProvider jwtProvider;

    private String accessToken;
    private User user;
    private Player player;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.clean();

        User user = userRepository.save(new User("nickname", "email@gmail.com", "abcd1234!"));

        accessToken = jwtProvider.createToken(user.getId(), user.getEmail());

        //player = playerRepository.save(new Player());
    }

    @AfterEach
    void tearDown() {
        databaseCleaner.clean();
    }

    @Test
    void 경기_선수_리뷰를_성공적으로_생성한다() {
        // given
        Long matchId = 1L;
        Long playerId = 1L;
        PlayerReviewCreateRequest request = new PlayerReviewCreateRequest(playerId, "기성용의 대지를 가르는 패스! 경기를 바꿨다.", 10);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(request)
                .when()
                .post("/api/v1/matches/{matchId}/player-reviews", matchId)
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }
}
