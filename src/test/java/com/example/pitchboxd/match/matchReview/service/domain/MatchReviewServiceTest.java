package com.example.pitchboxd.match.matchReview.service.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.match.matchReview.domain.MatchReview;
import com.example.pitchboxd.match.matchReview.dto.request.MatchReviewCreateRequest;
import com.example.pitchboxd.match.matchReview.infrastructure.MatchReviewRepository;
import com.example.pitchboxd.support.DatabaseCleaner;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@DisplayNameGeneration(ReplaceUnderscores.class)
class MatchReviewServiceTest {

    @Autowired
    private MatchReviewService matchReviewService;

    @Autowired
    private MatchReviewRepository matchReviewRepository;

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
    void 경기_리뷰를_저장한다() {
        // given
        Long userId = 1L;
        Long matchId = 1L;
        Integer point = 5;
        String content = "재밌는 경기였습니다.";
        MatchReviewCreateRequest request = new MatchReviewCreateRequest(content, point);

        // when
        MatchReview savedReview = matchReviewService.save(request, userId, matchId);

        // then
        List<MatchReview> allReviews = matchReviewRepository.findAll();
        assertAll(
                () -> assertThat(savedReview.getId()).isNotNull(),
                () -> assertThat(allReviews).hasSize(1),
                () -> assertThat(allReviews.get(0).getPoint()).isEqualTo(point),
                () -> assertThat(allReviews.get(0).getContent()).isEqualTo(content),
                () -> assertThat(allReviews.get(0).getUserId()).isEqualTo(userId),
                () -> assertThat(allReviews.get(0).getMatchId()).isEqualTo(matchId)
        );
    }
}
