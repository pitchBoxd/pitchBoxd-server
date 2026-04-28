package com.example.pitchboxd.match.matchReview.service.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import com.example.pitchboxd.match.core.infrastructure.MatchRepository;
import com.example.pitchboxd.match.matchReview.domain.MatchReview;
import com.example.pitchboxd.match.matchReview.dto.request.MatchReviewCreateRequest;
import com.example.pitchboxd.match.matchReview.dto.request.MatchReviewUpdateRequest;
import com.example.pitchboxd.match.matchReview.dto.response.LikeToggleResponse;
import com.example.pitchboxd.match.matchReview.dto.response.MatchReviewCreateResponse;
import com.example.pitchboxd.match.matchReview.dto.response.MatchReviewUpdateResponse;
import com.example.pitchboxd.match.matchReview.infrastructure.MatchReviewRepository;
import com.example.pitchboxd.match.matchStatistics.domain.FanType;
import com.example.pitchboxd.match.matchStatistics.domain.MatchStatistics;
import com.example.pitchboxd.match.matchStatistics.infrastructure.MatchStatisticsRepository;
import com.example.pitchboxd.support.DatabaseCleaner;
import com.example.pitchboxd.support.TestClockHolder;
import com.example.pitchboxd.team.domain.Team;
import com.example.pitchboxd.team.infrastructure.TeamRepository;
import com.example.pitchboxd.user.domain.User;
import com.example.pitchboxd.user.infrastructure.UserRepository;
import java.time.LocalDateTime;
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
    private TeamRepository teamRepository;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private TestClockHolder clockHolder;

    private User user;
    private Match match;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();
        clockHolder.setTime(LocalDateTime.now());
        Team homeTeam = teamRepository.save(new Team("홈팀", "naver1"));
        Team awayTeam = teamRepository.save(new Team("어웨이팀", "naver2"));

        user = userRepository.save(new User("테스터", "test@example.com", "password123!", homeTeam.getId()));
        Match unsavedMatch = new Match(1L, "2", homeTeam.getId(), awayTeam.getId(), clockHolder.now(),
                MatchStatus.FINISHED, "상암월드컵경기장", "1");
        unsavedMatch.finish(clockHolder.now().minusHours(1));
        match = matchRepository.save(unsavedMatch);

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

    @Test
    void 존재하지_않는_경기에_리뷰를_작성하면_예외가_발생한다() {
        // given
        Long invalidMatchId = 9999L;
        MatchReviewCreateRequest request = new MatchReviewCreateRequest("없는 경기", 5);

        // when & then
        assertThatThrownBy(() -> matchReviewFacadeService.submitReview(request, invalidMatchId, user.getId()))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 존재하지_않는_사용자가_리뷰를_작성하면_예외가_발생한다() {
        // given
        Long invalidUserId = 9999L;
        MatchReviewCreateRequest request = new MatchReviewCreateRequest("없는 유저", 5);

        // when & then
        assertThatThrownBy(() -> matchReviewFacadeService.submitReview(request, match.getId(), invalidUserId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 어웨이_팀_팬인_사용자가_리뷰를_성공적으로_등록한다() {
        // given
        User awayFan = userRepository.save(new User("어웨이팬", "away@example.com", "password123!", 2L));
        MatchReviewCreateRequest request = new MatchReviewCreateRequest("어웨이 팬의 리뷰", 4);

        // when
        MatchReviewCreateResponse response = matchReviewFacadeService.submitReview(request, match.getId(),
                awayFan.getId());

        // then
        MatchReview savedReview = matchReviewRepository.findById(response.id()).orElseThrow();

        assertAll(
                () -> assertThat(response.id()).isNotNull(),
                () -> assertThat(savedReview.getUserId()).isEqualTo(awayFan.getId())
        );
    }

    @Test
    void 경기_종료_후_48시간이_지나면_리뷰를_작성할_수_없다() {
        // given
        LocalDateTime startTime = LocalDateTime.now().minusHours(60);
        LocalDateTime endTime = startTime.plusMinutes(120);
        Match oldMatch = new Match(2L, "2", 1L, 2L, startTime, MatchStatus.FINISHED, "상암월드컵경기장", "1123444");
        oldMatch.finish(endTime);

        Match savedOldMatch = matchRepository.save(oldMatch);

        matchStatisticsRepository.save(new MatchStatistics(savedOldMatch.getId()));

        MatchReviewCreateRequest request = new MatchReviewCreateRequest("너무 늦은 리뷰", 5);

        // when & then
        assertThatThrownBy(() -> matchReviewFacadeService.submitReview(request, savedOldMatch.getId(), user.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.MATCH_REVIEW_INVALID_REVIEW_TIME.getMessage());
    }

    @Test
    void 리뷰_좋아요를_처음_누르면_좋아요가_추가된다() {
        // given
        MatchReviewCreateRequest request = new MatchReviewCreateRequest("정말 재미있는 경기였습니다!", 5);
        MatchReviewCreateResponse reviewResponse = matchReviewFacadeService.submitReview(request, match.getId(),
                user.getId());
        Long reviewId = reviewResponse.id();

        // when
        LikeToggleResponse response = matchReviewFacadeService.toggleLike(reviewId, user.getId());

        // then
        MatchReview updatedReview = matchReviewRepository.findById(reviewId).orElseThrow();
        assertAll(
                () -> assertThat(response.isLiked()).isTrue(),
                () -> assertThat(response.totalLikeCount()).isEqualTo(1),
                () -> assertThat(updatedReview.getLikeCount()).isEqualTo(1)
        );
    }

    @Test
    void 이미_좋아요를_누른_상태에서_다시_누르면_좋아요가_취소된다() {
        // given
        MatchReviewCreateRequest request = new MatchReviewCreateRequest("정말 재미있는 경기였습니다!", 5);
        MatchReviewCreateResponse reviewResponse = matchReviewFacadeService.submitReview(request, match.getId(),
                user.getId());
        Long reviewId = reviewResponse.id();
        matchReviewFacadeService.toggleLike(reviewId, user.getId());

        // when
        LikeToggleResponse response = matchReviewFacadeService.toggleLike(reviewId, user.getId());

        // then
        MatchReview updatedReview = matchReviewRepository.findById(reviewId).orElseThrow();
        assertAll(
                () -> assertThat(response.isLiked()).isFalse(),
                () -> assertThat(response.totalLikeCount()).isEqualTo(0),
                () -> assertThat(updatedReview.getLikeCount()).isEqualTo(0)
        );
    }

    @Test
    void 존재하지_않는_리뷰에_좋아요를_누르면_예외가_발생한다() {
        // given
        Long invalidReviewId = 9999L;

        // when & then
        assertThatThrownBy(() -> matchReviewFacadeService.toggleLike(invalidReviewId, user.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.MATCH_REVIEW_NOT_FOUND.getMessage());
    }

    @Test
    void 존재하지_않는_사용자가_좋아요를_누르면_예외가_발생한다() {
        // given
        MatchReviewCreateRequest request = new MatchReviewCreateRequest("정말 재미있는 경기였습니다!", 5);
        MatchReviewCreateResponse reviewResponse = matchReviewFacadeService.submitReview(request, match.getId(),
                user.getId());
        Long invalidUserId = 9999L;

        // when & then
        assertThatThrownBy(() -> matchReviewFacadeService.toggleLike(reviewResponse.id(), invalidUserId))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    void 경기_리뷰를_성공적으로_수정한다() {
        // given
        MatchReview matchReview = matchReviewRepository.save(
                new MatchReview(match.getId(), user.getId(), 3, "기존 내용", FanType.HOME));

        Long reviewId = matchReview.getId();
        MatchReviewUpdateRequest updateRequest = new MatchReviewUpdateRequest("수정된 내용", 5);

        // when
        MatchReviewUpdateResponse updateResponse = matchReviewFacadeService.updateMatchReview(reviewId, user.getId(),
                updateRequest);

        // then
        MatchReview updatedReview = matchReviewRepository.findById(updateResponse.id()).orElseThrow();
        assertAll(
                () -> assertThat(updatedReview.getContent()).isEqualTo("수정된 내용"),
                () -> assertThat(updatedReview.getPoint()).isEqualTo(5)
        );
    }

    @Test
    void 자신의_리뷰가_아니면_수정_시_예외가_발생한다() {
        // given
        MatchReview matchReview = matchReviewRepository.save(
                new MatchReview(match.getId(), user.getId(), 3, "내 리뷰", FanType.HOME));
        Long reviewId = matchReview.getId();

        User anotherUser = userRepository.save(new User("다른유저", "other@example.com", "password123!", 1L));
        MatchReviewUpdateRequest updateRequest = new MatchReviewUpdateRequest("수정 시도", 5);

        // when & then
        assertThatThrownBy(
                () -> matchReviewFacadeService.updateMatchReview(reviewId, anotherUser.getId(), updateRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.ACCESS_DENIED.getMessage());
    }

    @Test
    void 리뷰_점수를_수정하면_경기_통계의_평점_합계가_변경된다() {
        // given
        MatchReviewCreateResponse response = matchReviewFacadeService.submitReview(
                new MatchReviewCreateRequest("3점 리뷰", 3), match.getId(), user.getId());
        Long reviewId = response.id();

        // 3점에서 5점으로 수정 (차이 +2)
        MatchReviewUpdateRequest updateRequest = new MatchReviewUpdateRequest("5점 리뷰", 5);

        // when
        matchReviewFacadeService.updateMatchReview(reviewId, user.getId(), updateRequest);

        // then
        MatchStatistics statistics = matchStatisticsRepository.findByMatchId(match.getId()).orElseThrow();
        // FanType이 HOME이므로
        assertThat(statistics.getHomeFanRatingSum()).isEqualTo(5);
    }

    @Test
    void 경기_리뷰를_성공적으로_삭제한다() {
        // given
        MatchReview matchReview = matchReviewRepository.save(
                new MatchReview(match.getId(), user.getId(), 5, "삭제될 리뷰", FanType.HOME));
        Long reviewId = matchReview.getId();

        // when
        matchReviewFacadeService.deleteMatchReview(reviewId, user.getId());

        // then
        assertThat(matchReviewRepository.findById(reviewId)).isEmpty();
    }

    @Test
    void 자신의_리뷰가_아니면_삭제_시_예외가_발생한다() {
        // given
        MatchReview matchReview = matchReviewRepository.save(
                new MatchReview(match.getId(), user.getId(), 5, "다른 사람의 리뷰", FanType.HOME));
        Long reviewId = matchReview.getId();

        User anotherUser = userRepository.save(new User("다른유저", "other@example.com", "password123!", 1L));

        // when & then
        assertThatThrownBy(() -> matchReviewFacadeService.deleteMatchReview(reviewId, anotherUser.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.ACCESS_DENIED.getMessage());
    }

    @Test
    void 리뷰를_삭제하면_경기_통계의_평점_합계가_차감된다() {
        // given
        MatchReviewCreateResponse response = matchReviewFacadeService.submitReview(
                new MatchReviewCreateRequest("5점 리뷰", 5), match.getId(), user.getId());
        Long reviewId = response.id();

        // when
        matchReviewFacadeService.deleteMatchReview(reviewId, user.getId());

        // then
        MatchStatistics statistics = matchStatisticsRepository.findByMatchId(match.getId()).orElseThrow();
        assertThat(statistics.getHomeFanRatingSum()).isEqualTo(0);
    }
}
