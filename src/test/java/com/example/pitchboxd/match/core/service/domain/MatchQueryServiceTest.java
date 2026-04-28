package com.example.pitchboxd.match.core.service.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.global.domain.ClockHolder;
import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchFilter;
import com.example.pitchboxd.match.core.domain.MatchResult;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import com.example.pitchboxd.match.core.infrastructure.MatchRepository;
import com.example.pitchboxd.match.core.infrastructure.dto.MatchDetailStaticModel;
import com.example.pitchboxd.match.core.infrastructure.dto.MatchSummary;
import com.example.pitchboxd.season.domain.Season;
import com.example.pitchboxd.season.infrastructure.SeasonRepository;
import com.example.pitchboxd.support.DatabaseCleaner;
import com.example.pitchboxd.team.domain.Team;
import com.example.pitchboxd.team.infrastructure.TeamRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MatchQueryServiceTest {

    @Autowired
    private MatchQueryService matchQueryService;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private SeasonRepository seasonRepository;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private ClockHolder clockHolder;

    private Team homeTeam;
    private Team awayTeam;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();

        homeTeam = teamRepository.save(new Team("Home Team", "1"));
        awayTeam = teamRepository.save(new Team("Away Team", "2"));
    }

    @AfterEach
    void tearDown() {
        databaseCleaner.clean();
    }

    @Test
    void 경기의_정적_데이터를_상세_조회한다() {
        // given
        Season season = seasonRepository.save(new Season("2026 K리그1"));
        Match match = matchRepository.save(
                new Match(season.getId(), "5", homeTeam.getId(), awayTeam.getId(), LocalDateTime.now(),
                        MatchStatus.FINISHED, "상암", "match-1"));

        // when
        MatchDetailStaticModel result = matchQueryService.findMatchStaticDetailById(match.getId());

        // then
        assertAll(
                () -> assertThat(result.matchId()).isEqualTo(match.getId()),
                () -> assertThat(result.seasonName()).isEqualTo("2026 K리그1"),
                () -> assertThat(result.homeTeamName()).isEqualTo("Home Team"),
                () -> assertThat(result.awayTeamName()).isEqualTo("Away Team")
        );
    }

    @Test
    void 존재하지_않는_경기를_조회하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> matchQueryService.findMatchStaticDetailById(999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MATCH_NOT_FOUND);
    }

    @Test
    void 특정_시점_이후에_종료된_경기_목록을_조회한다() {
        // given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minusHours(48);

        // 종료된 경기 (기준 시점 이후)
        Match finishedMatch = new Match(1L, "3", homeTeam.getId(), awayTeam.getId(), threshold.minusHours(1),
                MatchStatus.FINISHED, "상암", "1");
        finishedMatch.finish(threshold.plusHours(1));
        MatchResult matchResult = new MatchResult(3, 1, null, null);
        finishedMatch.decideMatchResult(matchResult);

        matchRepository.save(finishedMatch);

        // 종료된 경기 (기준 시점 이전) - 조회되지 않아야 함
        Match oldFinishedMatch = new Match(1L, "2", homeTeam.getId(), awayTeam.getId(), threshold.minusHours(4),
                MatchStatus.FINISHED, "상암", "2");
        oldFinishedMatch.finish(threshold.minusHours(2));
        matchRepository.save(oldFinishedMatch);

        // 진행 예정인 경기 - 조회되지 않아야 함
        Match upcomingMatch = new Match(1L, "7", homeTeam.getId(), awayTeam.getId(), now.plusHours(1),
                MatchStatus.SCHEDULED, "상암", "3");

        matchRepository.save(upcomingMatch);

        // when
        List<MatchSummary> results = matchQueryService.findRecentlyFinishedMatches(threshold);

        // then
        assertAll(
                () -> assertThat(results).hasSize(1),
                () -> assertThat(results.get(0).homeTeam()).isEqualTo(homeTeam.getName()),
                () -> assertThat(results.get(0).awayTeam()).isEqualTo(awayTeam.getName()),
                () -> assertThat(results.get(0).homeTeamScore()).isEqualTo(3),
                () -> assertThat(results.get(0).awayTeamScore()).isEqualTo(1)
        );
    }

    @Test
    void 해당되는_경기가_없으면_빈_리스트를_반환한다() {
        // given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minusHours(48);

        Match upcomingMatch = new Match(1L, "7", homeTeam.getId(), awayTeam.getId(), now.plusHours(1),
                MatchStatus.SCHEDULED, "상암", "1");
        matchRepository.save(upcomingMatch);

        // when
        List<MatchSummary> results = matchQueryService.findRecentlyFinishedMatches(threshold);

        // then
        assertThat(results).isEmpty();
    }

    @Test
    void 특정_팀의_특정_시점_이후에_종료된_경기_목록을_조회한다() {
        // given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minusHours(48);

        // 팀이 홈인 종료된 경기 (기준 시점 이후)
        Match match1 = new Match(1L, "1", homeTeam.getId(), awayTeam.getId(), threshold.minusHours(1),
                MatchStatus.FINISHED, "상암", "1");
        match1.finish(threshold.plusHours(1));
        match1.decideMatchResult(new MatchResult(2, 0, null, null));
        matchRepository.save(match1);

        // 팀이 어웨이인 종료된 경기 (기준 시점 이후)
        Match match2 = new Match(1L, "2", awayTeam.getId(), homeTeam.getId(), threshold.minusHours(2),
                MatchStatus.FINISHED, "상암", "2");
        match2.finish(threshold.plusHours(2));
        match2.decideMatchResult(new MatchResult(1, 3, null, null));
        matchRepository.save(match2);

        // 다른 팀들간의 종료된 경기 (기준 시점 이후)
        Team otherTeam1 = teamRepository.save(new Team("Other 1", "3"));
        Team otherTeam2 = teamRepository.save(new Team("Other 2", "4"));
        Match match3 = new Match(1L, "3", otherTeam1.getId(), otherTeam2.getId(), threshold.minusHours(1),
                MatchStatus.FINISHED, "상암", "3");
        match3.finish(threshold.plusHours(1));
        match3.decideMatchResult(new MatchResult(0, 0, null, null));
        matchRepository.save(match3);

        // when
        List<MatchSummary> results = matchQueryService.findRecentlyFinishedMatchesByTeam(threshold, homeTeam.getId());

        // then
        assertAll(
                () -> assertThat(results).hasSize(2),
                () -> assertThat(results).extracting(MatchSummary::homeTeam)
                        .containsExactlyInAnyOrder(homeTeam.getName(), awayTeam.getName())
        );
    }

    @Test
    void 특정_팀의_종료된_경기가_없으면_빈_리스트를_반환한다() {
        // given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minusHours(48);

        // 다른 팀의 종료된 경기만 존재
        Team otherTeam1 = teamRepository.save(new Team("Other 1", "3"));
        Team otherTeam2 = teamRepository.save(new Team("Other 2", "4"));
        Match match = new Match(1L, "3", otherTeam1.getId(), otherTeam2.getId(), threshold.minusHours(1),
                MatchStatus.FINISHED, "상암", "1");
        match.finish(threshold.plusHours(1));
        match.decideMatchResult(new MatchResult(0, 0, null, null));
        matchRepository.save(match);

        // when
        List<MatchSummary> results = matchQueryService.findRecentlyFinishedMatchesByTeam(threshold, homeTeam.getId());

        // then
        assertThat(results).isEmpty();
    }

    @Test
    void 시즌_및_상태_필터를_사용해_경기를_조회한다() {
        // given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minusHours(48);

        // 시즌 1, 종료됨 (리뷰 가능)
        Match match1 = new Match(1L, "1", homeTeam.getId(), awayTeam.getId(), now.minusHours(3),
                MatchStatus.FINISHED, "상암", "match-1");
        match1.finish(now.minusHours(1));
        matchRepository.save(match1);

        // 시즌 1, 진행 예정 (리뷰 불가능)
        Match match2 = new Match(1L, "2", homeTeam.getId(), awayTeam.getId(), now.plusHours(1),
                MatchStatus.SCHEDULED, "상암", "match-2");
        matchRepository.save(match2);

        // 시즌 2, 종료됨 (리뷰 가능)
        Match match3 = new Match(2L, "1", homeTeam.getId(), awayTeam.getId(), now.minusHours(3),
                MatchStatus.FINISHED, "상암", "match-3");
        match3.finish(now.minusHours(1));
        matchRepository.save(match3);

        // when & then
        assertAll(
                () -> {
                    // 시즌 1의 모든 경기 조회
                    List<MatchSummary> results = matchQueryService.findMatches(1L, null, threshold);
                    assertThat(results).hasSize(2);
                },
                () -> {
                    // 모든 시즌의 리뷰 가능한 경기 조회
                    List<MatchSummary> results = matchQueryService.findMatches(null, MatchFilter.REVIEWABLE, threshold);
                    assertThat(results).hasSize(2);
                },
                () -> {
                    // 시즌 1의 리뷰 가능한 경기 조회
                    List<MatchSummary> results = matchQueryService.findMatches(1L, MatchFilter.REVIEWABLE, threshold);
                    assertThat(results).hasSize(1);
                    assertThat(results.get(0).id()).isEqualTo(match1.getId());
                }
        );
    }
}
