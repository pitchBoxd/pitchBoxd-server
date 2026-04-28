package com.example.pitchboxd.match.matchReview.presentation;

import com.example.pitchboxd.auth.application.TokenManager;
import com.example.pitchboxd.match.core.infrastructure.MatchRepository;
import com.example.pitchboxd.match.matchReview.infrastructure.MatchReviewRepository;
import com.example.pitchboxd.support.DatabaseCleaner;
import com.example.pitchboxd.support.TestClockHolder;
import com.example.pitchboxd.team.infrastructure.TeamRepository;
import com.example.pitchboxd.user.domain.User;
import com.example.pitchboxd.user.infrastructure.UserRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(ReplaceUnderscores.class)
class MatchReviewQueryControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchReviewRepository matchReviewRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private TestClockHolder clockHolder;

    @Autowired
    private TokenManager tokenManager;

    private String accessToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.clean();

        User user = userRepository.save(new User("테스트유저", "test@example.com", "password123!"));
        accessToken = tokenManager.createAccessToken(user.getId(), user.getEmail());
    }

    @AfterEach
    void tearDown() {
        databaseCleaner.clean();
    }

}
