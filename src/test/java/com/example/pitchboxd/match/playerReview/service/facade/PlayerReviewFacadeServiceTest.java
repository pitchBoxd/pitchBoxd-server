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
import com.example.pitchboxd.match.playerReview.dto.request.PlayerReviewCreateRequest;
import com.example.pitchboxd.match.playerReview.dto.response.PlayerReviewCreateResponse;
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
        User user = userRepository.save(new User("유저", "user@gmail.com", "password", homeTeam.getId()));
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
        User user = userRepository.save(new User("유저", "user@gmail.com", "password", homeTeam.getId()));

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

        User user = userRepository.save(new User("유저", "user@gmail.com", "password", homeTeam.getId()));
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
                new User("유저", "user@gmail.com", "password", awayTeam.getId())); // 다른 팀 팬

        PlayerReviewCreateRequest request = new PlayerReviewCreateRequest(homeTeamPlayer.getId(), "최고의 활약이었습니다.", 5);

        // when & then
        assertThatThrownBy(() -> playerReviewFacadeService.submitReview(request, match.getId(), awayTeamFan.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.PLAYER_REVIEW_NOT_FAN.getMessage());
    }

    @Test
    void 경기_리뷰_가능_시간이_지나면_리뷰를_할_수_없다() {
        // given
        User user = userRepository.save(new User("유저", "user@gmail.com", "password", homeTeam.getId()));
        PlayerReviewCreateRequest request = new PlayerReviewCreateRequest(homeTeamPlayer.getId(), "최고의 활약이었습니다.", 5);

        // 24시간이 지난 후로 설정
        clockHolder.plusHours(25);

        // when & then
        assertThatThrownBy(() -> playerReviewFacadeService.submitReview(request, match.getId(), user.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.MATCH_REVIEW_TIME_LIMIT_PASSED.getMessage());
    }
}
