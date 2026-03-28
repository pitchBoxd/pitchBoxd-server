package com.example.pitchboxd.matchReview.service.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.match.domain.Match;
import com.example.pitchboxd.match.domain.MatchResult;
import com.example.pitchboxd.match.domain.MatchStatus;
import com.example.pitchboxd.match.dto.request.MatchReviewCreateRequest;
import com.example.pitchboxd.match.dto.response.MatchReviewCreateResponse;
import com.example.pitchboxd.match.infrastructure.MatchRepository;
import com.example.pitchboxd.matchReview.domain.MatchReview;
import com.example.pitchboxd.matchReview.infrastructure.MatchReviewRepository;
import com.example.pitchboxd.matchStatistics.domain.MatchStatistics;
import com.example.pitchboxd.matchStatistics.infrastructure.MatchStatisticsRepository;
import com.example.pitchboxd.support.DatabaseCleaner;
import com.example.pitchboxd.user.domain.User;
import com.example.pitchboxd.user.infrastructure.UserRepository;
import java.time.LocalDateTime;
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
class MatchReviewFacadeServiceTest {

    @Autowired
    private MatchReviewFacadeService matchReviewFacadeService;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchReviewRepository matchReviewRepository;

    @Autowired
    private MatchStatisticsRepository matchStatisticsRepository;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    private User user;
    private Match match;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();

        user = userRepository.save(new User("테스터", "test@example.com", "password123!", 1L));
        MatchResult matchResult = new MatchResult(0, 0, List.of(), List.of());
        match = matchRepository.save(
                new Match(1L, 2, 1L, 2L, LocalDateTime.now(), MatchStatus.FINISHED, "상암월드컵경기장", matchResult));
        matchStatisticsRepository.save(new MatchStatistics(match.getId()));
    }

    @AfterEach
    void tearDown() {
        databaseCleaner.clean();
    }

    @Test
    void 경기_리뷰를_성공적으로_등록한다() {
        // given
        MatchReviewCreateRequest request = new MatchReviewCreateRequest("정말 재미있는 경기였습니다!", 5);

        // when
        MatchReviewCreateResponse response = matchReviewFacadeService.submitReview(request, match.getId(),
                user.getId());

        // then
        MatchReview savedReview = matchReviewRepository.findById(response.id()).orElseThrow();

        assertAll(
                () -> assertThat(savedReview.getPoint()).isEqualTo(5),
                () -> assertThat(savedReview.getContent()).isEqualTo("정말 재미있는 경기였습니다!"),
                () -> assertThat(savedReview.getMatchId()).isEqualTo(match.getId()),
                () -> assertThat(savedReview.getUserId()).isEqualTo(user.getId())
        );
    }

    @Test
    void 이미_리뷰를_작성한_경기라면_예외가_발생한다() {
        // given
        MatchReviewCreateRequest request = new MatchReviewCreateRequest("첫 번째 리뷰", 5);
        matchReviewFacadeService.submitReview(request, match.getId(), user.getId());

        MatchReviewCreateRequest duplicateRequest = new MatchReviewCreateRequest("두 번째 리뷰", 4);

        // when & then
        assertThatThrownBy(() -> matchReviewFacadeService.submitReview(duplicateRequest, match.getId(), user.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.MATCH_REVIEW_ALREADY_REVIEWED.getMessage());
    }
}
