package com.example.pitchboxd.match.playerReview.service.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import com.example.pitchboxd.match.core.infrastructure.MatchRepository;
import com.example.pitchboxd.match.lineup.domain.MatchLineup;
import com.example.pitchboxd.match.lineup.domain.ParticipationStatus;
import com.example.pitchboxd.match.lineup.infrastructure.MatchLineupRepository;
import com.example.pitchboxd.match.matchReview.dto.response.LikeToggleResponse;
import com.example.pitchboxd.match.playerReview.domain.PlayerReview;
import com.example.pitchboxd.match.playerReview.domain.PlayerReviewLike;
import com.example.pitchboxd.match.playerReview.dto.request.PlayerReviewCreateRequest;
import com.example.pitchboxd.match.playerReview.dto.request.PlayerReviewUpdateRequest;
import com.example.pitchboxd.match.playerReview.dto.response.PlayerReviewCreateResponse;
import com.example.pitchboxd.match.playerReview.dto.response.PlayerReviewUpdateResponse;
import com.example.pitchboxd.match.playerReview.infrastructure.PlayerReviewLikeRepository;
import com.example.pitchboxd.match.playerReview.infrastructure.PlayerReviewRepository;
import com.example.pitchboxd.match.playerStatistics.domain.PlayerMatchStatistics;
import com.example.pitchboxd.match.playerStatistics.infrastructure.PlayerMatchStatisticsRepository;
import com.example.pitchboxd.player.domain.Player;
import com.example.pitchboxd.player.infrastructure.PlayerRepository;
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
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PlayerReviewFacadeServiceTest {

    @Autowired
    private PlayerReviewFacadeService playerReviewFacadeService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private MatchLineupRepository matchLineupRepository;

    @Autowired
    private PlayerReviewRepository playerReviewRepository;

    @Autowired
    private PlayerMatchStatisticsRepository playerMatchStatisticsRepository;

    @Autowired
    private PlayerReviewLikeRepository playerReviewLikeRepository;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private TestClockHolder clockHolder;

    private Team homeTeam;
    private Team awayTeam;
    private Player homeTeamPlayer;
    private Match match;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();

        LocalDateTime now = LocalDateTime.now();
        homeTeam = teamRepository.save(new Team("FC서울"));
        awayTeam = teamRepository.save(new Team("수원삼성"));
        homeTeamPlayer = playerRepository.save(new Player(homeTeam.getId(), "기성용"));

        Match unsavedMatch = new Match(1L, "1", homeTeam.getId(), awayTeam.getId(), now.minusHours(3),
                MatchStatus.FINISHED, "상암 월드컵 경기장");
        unsavedMatch.finish(LocalDateTime.now().minusHours(1));

        match = matchRepository.save(unsavedMatch);
        matchLineupRepository.save(
                new MatchLineup(match.getId(), homeTeamPlayer.getId(), 6, ParticipationStatus.STARTER));

        PlayerMatchStatistics playerMatchStatistics = new PlayerMatchStatistics(homeTeamPlayer.getId(), match.getId());
        playerMatchStatisticsRepository.save(playerMatchStatistics);
    }

    @AfterEach
    void tearDown() {
        databaseCleaner.clean();
        clockHolder.setTime(LocalDateTime.now());
    }

    @Test
    void 선수를_성공적으로_리뷰한다() {
        // given
        User user = userRepository.save(new User("유저", "user @gmail.com", "password", homeTeam.getId()));
        PlayerReviewCreateRequest request = new PlayerReviewCreateRequest(homeTeamPlayer.getId(), "최고의 활약이었습니다.", 5);

        // when
        PlayerReviewCreateResponse response = playerReviewFacadeService.submitReview(request, match.getId(),
                user.getId());

        // then
        assertAll(
                () -> assertThat(response.id()).isNotNull(),
                () -> assertThat(playerReviewRepository.existsById(response.id())).isTrue()
        );
    }

    @Test
    void 이미_리뷰를_남긴_선수에게_다시_리뷰를_남기면_예외가_발생한다() {
        // given
        User user = userRepository.save(new User("유저", "user @gmail.com", "password", homeTeam.getId()));

        PlayerReviewCreateRequest request = new PlayerReviewCreateRequest(homeTeamPlayer.getId(), "최고의 활약이었습니다.", 5);
        playerReviewFacadeService.submitReview(request, match.getId(), user.getId());

        // when & then
        assertThatThrownBy(() -> playerReviewFacadeService.submitReview(request, match.getId(), user.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.PLAYER_REVIEW_ALREADY_REVIEWED.getMessage());
    }

    @Test
    void 경기에_출전하지_않은_선수에게_리뷰를_남기면_예외가_발생한다() {
        // given
        Player benchPlayer = playerRepository.save(new Player(homeTeam.getId(), "벤치선수"));
        matchLineupRepository.save(
                new MatchLineup(match.getId(), benchPlayer.getId(), 99, ParticipationStatus.BENCH));
        playerMatchStatisticsRepository.save(new PlayerMatchStatistics(benchPlayer.getId(), match.getId()));

        User user = userRepository.save(new User("유저", "user @gmail.com", "password", homeTeam.getId()));
        PlayerReviewCreateRequest request = new PlayerReviewCreateRequest(benchPlayer.getId(), "최고의 활약이었습니다.", 5);

        // when & then
        assertThatThrownBy(() -> playerReviewFacadeService.submitReview(request, match.getId(), user.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.MATCH_LINEUP_DID_NOT_PARTICIPATE.getMessage());
    }

    @Test
    void 자신이_응원하는_팀의_선수가_아닌_경우_리뷰를_남기면_예외가_발생한다() {
        // given
        User awayTeamFan = userRepository.save(
                new User("유저", "user @gmail.com", "password", awayTeam.getId())); // 다른 팀 팬

        PlayerReviewCreateRequest request = new PlayerReviewCreateRequest(homeTeamPlayer.getId(), "최고의 활약이었습니다.", 5);

        // when & then
        assertThatThrownBy(() -> playerReviewFacadeService.submitReview(request, match.getId(), awayTeamFan.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.PLAYER_REVIEW_NOT_FAN.getMessage());
    }

    @Test
    void 경기_리뷰_가능_시간이_지나면_리뷰를_할_수_없다() {
        // given
        User user = userRepository.save(new User("유저", "user @gmail.com", "password", homeTeam.getId()));
        PlayerReviewCreateRequest request = new PlayerReviewCreateRequest(homeTeamPlayer.getId(), "최고의 활약이었습니다.", 5);

        // 24시간이 지난 후로 설정
        clockHolder.plusHours(25);

        // when & then
        assertThatThrownBy(() -> playerReviewFacadeService.submitReview(request, match.getId(), user.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.MATCH_REVIEW_TIME_LIMIT_PASSED.getMessage());
    }

    @Test
    void 선수_리뷰에_좋아요를_누른다() {
        // given
        User user = userRepository.save(new User("유저", "user @gmail.com", "password", homeTeam.getId()));
        PlayerReview playerReview = playerReviewRepository.save(
                new PlayerReview(match.getId(), homeTeamPlayer.getId(), user.getId(), 5, "쩐다 ㄷㄷ"));

        // when
        LikeToggleResponse toggleResponse = playerReviewFacadeService.toggleLike(playerReview.getId(), user.getId());

        // then
        PlayerReview result = playerReviewRepository.findById(playerReview.getId()).orElseThrow();
        assertAll(
                () -> assertThat(toggleResponse.isLiked()).isTrue(),
                () -> assertThat(result.getLikeCount()).isEqualTo(1)
        );
    }

    @Test
    void 선수_리뷰의_좋아요를_취소한다() {
        // given
        User user = userRepository.save(new User("유저", "user @gmail.com", "password", homeTeam.getId()));
        PlayerReview playerReview = playerReviewRepository.save(
                new PlayerReview(match.getId(), homeTeamPlayer.getId(), user.getId(), 5, "쩐다 ㄷㄷ"));

        playerReviewLikeRepository.save(new PlayerReviewLike(playerReview.getId(), user.getId()));

        // when
        LikeToggleResponse toggleResponse = playerReviewFacadeService.toggleLike(playerReview.getId(), user.getId());

        // then
        PlayerReview result = playerReviewRepository.findById(playerReview.getId()).orElseThrow();
        assertAll(
                () -> assertThat(toggleResponse.isLiked()).isFalse(),
                () -> assertThat(result.getLikeCount()).isEqualTo(0)
        );
    }

    @Test
    void 선수_리뷰를_성공적으로_수정한다() {
        // given
        User user = userRepository.save(new User("유저", "user @gmail.com", "password", homeTeam.getId()));
        PlayerReview playerReview = playerReviewRepository.save(
                new PlayerReview(match.getId(), homeTeamPlayer.getId(), user.getId(), 3, "원래 리뷰 내용"));

        PlayerReviewUpdateRequest request = new PlayerReviewUpdateRequest("수정된 리뷰 내용", 5);

        // when
        PlayerReviewUpdateResponse response = playerReviewFacadeService.updateReview(request, playerReview.getId(),
                user.getId());

        // then
        PlayerReview result = playerReviewRepository.findById(playerReview.getId()).orElseThrow();
        assertAll(
                () -> assertThat(response.id()).isEqualTo(playerReview.getId()),
                () -> assertThat(result.getContent()).isEqualTo("수정된 리뷰 내용"),
                () -> assertThat(result.getPoint()).isEqualTo(5)
        );
    }

    @Test
    void 선수_리뷰_수정_시_작성자가_아니면_예외가_발생한다() {
        // given
        User owner = userRepository.save(new User("작성자", "owner @gmail.com", "password", homeTeam.getId()));
        User other = userRepository.save(new User("다른유저", "other @gmail.com", "password", homeTeam.getId()));

        PlayerReview playerReview = playerReviewRepository.save(
                new PlayerReview(match.getId(), homeTeamPlayer.getId(), owner.getId(), 4, "작성자의 리뷰"));

        PlayerReviewUpdateRequest request = new PlayerReviewUpdateRequest("수정 시도", 5);

        // when & then
        assertThatThrownBy(() -> playerReviewFacadeService.updateReview(request, playerReview.getId(), other.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.ACCESS_DENIED.getMessage());
    }

    @Test
    void 리뷰_점수를_수정하면_선수_통계의_평점_합계가_변경된다() {
        // given
        User user = userRepository.save(new User("유저", "user @gmail.com", "password", homeTeam.getId()));
        PlayerReviewCreateResponse response = playerReviewFacadeService.submitReview(
                new PlayerReviewCreateRequest(homeTeamPlayer.getId(), "3점 리뷰", 3), match.getId(), user.getId());
        Long reviewId = response.id();

        // 3점에서 5점으로 수정 (차이 +2)
        PlayerReviewUpdateRequest request = new PlayerReviewUpdateRequest("5점 리뷰", 5);

        // when
        playerReviewFacadeService.updateReview(request, reviewId, user.getId());

        // then
        PlayerMatchStatistics statistics = playerMatchStatisticsRepository.findByMatchIdAndPlayerId(match.getId(),
                homeTeamPlayer.getId()).orElseThrow();

        assertAll(
                () -> assertThat(statistics.getReviewCount()).isEqualTo(1),
                () -> assertThat(statistics.getTotalScore()).isEqualTo(5)
        );
    }

    @Test
    void 경기가_종료되지_않은_상태에서_리뷰를_남기면_예외가_발생한다() {
        // given
        Match ongoingMatch = matchRepository.save(
                new Match(1L, "2", homeTeam.getId(), awayTeam.getId(), LocalDateTime.now().plusHours(1),
                        MatchStatus.SCHEDULED, "상암 월드컵 경기장"));
        User user = userRepository.save(new User("유저", "user @gmail.com", "password", homeTeam.getId()));
        PlayerReviewCreateRequest request = new PlayerReviewCreateRequest(homeTeamPlayer.getId(), "최고의 활약이었습니다.", 5);

        // when & then
        assertThatThrownBy(() -> playerReviewFacadeService.submitReview(request, ongoingMatch.getId(), user.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.MATCH_REVIEW_TIME_LIMIT_PASSED.getMessage());
    }

    @Test
    void 선수_리뷰를_성공적으로_삭제한다() {
        // given
        User user = userRepository.save(new User("유저", "user @gmail.com", "password", homeTeam.getId()));
        PlayerReview playerReview = playerReviewRepository.save(
                new PlayerReview(match.getId(), homeTeamPlayer.getId(), user.getId(), 5, "최고의 활약!"));

        PlayerMatchStatistics statistics = playerMatchStatisticsRepository.findByMatchIdAndPlayerId(match.getId(),
                homeTeamPlayer.getId()).orElseThrow();
        statistics.addNewReview(5);
        playerMatchStatisticsRepository.save(statistics);

        // when
        playerReviewFacadeService.deleteReview(playerReview.getId(), user.getId());

        // then
        PlayerMatchStatistics resultStatistics = playerMatchStatisticsRepository.findByMatchIdAndPlayerId(match.getId(),
                homeTeamPlayer.getId()).orElseThrow();

        assertAll(
                () -> assertThat(playerReviewRepository.existsById(playerReview.getId())).isFalse(),
                () -> assertThat(resultStatistics.getReviewCount()).isEqualTo(0),
                () -> assertThat(resultStatistics.getTotalScore()).isEqualTo(0)
        );
    }

    @Test
    void 선수_리뷰_삭제_시_작성자가_아니면_예외가_발생한다() {
        // given
        User owner = userRepository.save(new User("작성자", "owner @gmail.com", "password", homeTeam.getId()));
        User other = userRepository.save(new User("다른유저", "other @gmail.com", "password", homeTeam.getId()));
        PlayerReview playerReview = playerReviewRepository.save(
                new PlayerReview(match.getId(), homeTeamPlayer.getId(), owner.getId(), 5, "작성자의 리뷰"));

        // when & then
        assertThatThrownBy(() -> playerReviewFacadeService.deleteReview(playerReview.getId(), other.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.ACCESS_DENIED.getMessage());
    }
}
