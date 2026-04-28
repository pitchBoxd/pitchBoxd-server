package com.example.pitchboxd.match.core.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.global.config.QueryDslConfig;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchFilter;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import com.example.pitchboxd.match.core.infrastructure.dto.MatchDetailStaticModel;
import com.example.pitchboxd.match.core.infrastructure.dto.MatchSummary;
import com.example.pitchboxd.match.matchStatistics.domain.FanType;
import com.example.pitchboxd.match.matchStatistics.domain.MatchStatistics;
import com.example.pitchboxd.match.matchStatistics.infrastructure.MatchStatisticsRepository;
import com.example.pitchboxd.season.domain.Season;
import com.example.pitchboxd.season.infrastructure.SeasonRepository;
import com.example.pitchboxd.team.domain.Team;
import com.example.pitchboxd.team.infrastructure.TeamRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@Import({MatchQueryRepository.class, QueryDslConfig.class})
@DisplayNameGeneration(ReplaceUnderscores.class)
class MatchQueryRepositoryTest {

    @Autowired
    private MatchQueryRepository matchQueryRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private SeasonRepository seasonRepository;

    @Autowired
    private MatchStatisticsRepository matchStatisticsRepository;

    @Test
    void 경기의_정적_데이터를_상세_조회한다() {
        // given
        Season season = seasonRepository.save(new Season("2026 K리그1"));
        Team homeTeam = teamRepository.save(new Team("FC서울", "n1"));
        Team awayTeam = teamRepository.save(new Team("울산현대HD", "n2"));

        LocalDateTime startTime = LocalDateTime.of(2026, 4, 28, 19, 0);
        Match match = matchRepository.save(
                new Match(season.getId(), "5", homeTeam.getId(), awayTeam.getId(), startTime, MatchStatus.FINISHED,
                        "상암", "match-1"));

        // when
        Optional<MatchDetailStaticModel> result = matchQueryRepository.findMatchStaticDetailById(match.getId());

        // then
        assertThat(result).isPresent();
        MatchDetailStaticModel model = result.get();
        assertAll(
                () -> assertThat(model.matchId()).isEqualTo(match.getId()),
                () -> assertThat(model.seasonName()).isEqualTo("2026 K리그1"),
                () -> assertThat(model.round()).isEqualTo("5"),
                () -> assertThat(model.startTime()).isEqualTo(startTime),
                () -> assertThat(model.location()).isEqualTo("상암"),
                () -> assertThat(model.homeTeamName()).isEqualTo("FC서울"),
                () -> assertThat(model.homeTeamId()).isEqualTo(homeTeam.getId()),
                () -> assertThat(model.awayTeamName()).isEqualTo("울산현대HD"),
                () -> assertThat(model.awayTeamId()).isEqualTo(awayTeam.getId())
        );
    }

    @Test
    void 종료된_경기_중_특정_시점_이후에_종료된_경기를_조회한다() {
        // given
        Team homeTeam = teamRepository.save(new Team("FC서울", "1"));
        Team awayTeam = teamRepository.save(new Team("울산현대HD", "1"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minusHours(48); // 48시간 이전에 종료되지 않은 경기를 찾을 수 있음.

        // 1. 조건 만족: FINISHED 상태이며 threshold 이후에 종료됨
        Match satisfiedMatch = matchRepository.save(
                new Match(1L, "3", homeTeam.getId(), awayTeam.getId(), threshold.minusHours(2), MatchStatus.FINISHED,
                        "상암", "1"));
        satisfiedMatch.finish(threshold.plusHours(1));

        // 2. 조건 미달: FINISHED 상태이나 threshold 이전에 종료됨
        Match unsatisfiedMatch1 = matchRepository.save(
                new Match(1L, "2", homeTeam.getId(), awayTeam.getId(), threshold.minusHours(4), MatchStatus.FINISHED,
                        "상암", "2"));
        unsatisfiedMatch1.finish(threshold.minusHours(1));

        // 3. 조건 미달: 종료되지 않음
        matchRepository.save(
                new Match(1L, "4", homeTeam.getId(), awayTeam.getId(), now, MatchStatus.SCHEDULED,
                        "상암", "3"));

        // 통계 데이터 저장 (평점 계산 확인용)
        MatchStatistics matchStatistics = matchStatisticsRepository.save(new MatchStatistics(satisfiedMatch.getId()));

        matchStatistics.addNewReview(9, FanType.NEUTRAL);
        matchStatistics.addNewReview(5, FanType.AWAY);
        matchStatistics.addNewReview(7, FanType.HOME);
        // 평점 (9 + 5 + 7) / (3 * 2) = 3.5

        // when
        List<MatchSummary> results = matchQueryRepository.findFinishedMatchesSince(threshold);

        // then
        assertAll(
                () -> assertThat(results).hasSize(1),
                () -> assertThat(results.get(0).id()).isEqualTo(satisfiedMatch.getId()),
                () -> assertThat(results.get(0).homeTeam()).isEqualTo("FC서울"),
                () -> assertThat(results.get(0).awayTeam()).isEqualTo("울산현대HD"),
                () -> assertThat(results.get(0).homeTeamScore()).isEqualTo(0),
                () -> assertThat(results.get(0).awayTeamScore()).isEqualTo(0),
                () -> assertThat(results.get(0).matchRating()).isEqualTo(3.5)
        );
    }

    @Test
    void 종료된_경기가_없으면_빈_리스트를_반환한다() {
        // given
        LocalDateTime threshold = LocalDateTime.now().minusDays(1);

        // when
        List<MatchSummary> results = matchQueryRepository.findFinishedMatchesSince(threshold);

        // then
        assertThat(results).isEmpty();
    }

    @Test
    void 종료된_경기_중_특정_시점_이후에_종료된_특정_팀_경기를_조회한다() {
        // given
        Team teamA = teamRepository.save(new Team("팀A", "1"));
        Team teamB = teamRepository.save(new Team("팀B", "1"));
        Team teamC = teamRepository.save(new Team("팀C", "1"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minusHours(48);

        // Match 1: Team A vs Team B (StartTime: threshold - 2h)
        Match match1 = matchRepository.save(
                new Match(1L, "1", teamA.getId(), teamB.getId(), threshold.minusHours(2), MatchStatus.FINISHED,
                        "경기장1", "n1"));
        match1.finish(threshold.plusHours(1));

        // Match 2: Team B vs Team C (StartTime: threshold - 1h)
        Match match2 = matchRepository.save(
                new Match(1L, "2", teamB.getId(), teamC.getId(), threshold.minusHours(1), MatchStatus.FINISHED,
                        "경기장2", "n2"));
        match2.finish(threshold.plusHours(2));

        // when
        List<MatchSummary> teamAResults = matchQueryRepository.findFinishedMatchesSince(threshold, teamA.getId());
        List<MatchSummary> teamBResults = matchQueryRepository.findFinishedMatchesSince(threshold, teamB.getId());
        List<MatchSummary> teamCResults = matchQueryRepository.findFinishedMatchesSince(threshold, teamC.getId());

        // then
        assertAll(
                () -> assertThat(teamAResults).hasSize(1),
                () -> assertThat(teamAResults.get(0).id()).isEqualTo(match1.getId()),

                () -> assertThat(teamBResults).hasSize(2),
                () -> assertThat(teamBResults).extracting(MatchSummary::id)
                        .containsExactly(match1.getId(), match2.getId()),

                () -> assertThat(teamCResults).hasSize(1),
                () -> assertThat(teamCResults.get(0).id()).isEqualTo(match2.getId())
        );
    }

    @Test
    void 시즌_및_상태_필터를_사용해_경기를_조회한다() {
        // given
        Team teamA = teamRepository.save(new Team("팀A", "n1"));
        Team teamB = teamRepository.save(new Team("팀B", "n2"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minusHours(48);

        // 1. 시즌 1, 종료됨 (리뷰 가능)
        Match match1 = matchRepository.save(
                new Match(1L, "1", teamA.getId(), teamB.getId(), now.minusHours(3), MatchStatus.FINISHED,
                        "경기장", "match-1"));
        match1.finish(now.minusHours(1));

        // 2. 시즌 1, 진행 예정 (리뷰 불가능)
        matchRepository.save(
                new Match(1L, "2", teamA.getId(), teamB.getId(), now.plusHours(1), MatchStatus.SCHEDULED,
                        "경기장", "match-2"));

        // 3. 시즌 2, 종료됨 (리뷰 가능)
        Match match3 = matchRepository.save(
                new Match(2L, "1", teamA.getId(), teamB.getId(), now.minusHours(3), MatchStatus.FINISHED,
                        "경기장", "match-3"));
        match3.finish(now.minusHours(1));

        // when & then
        assertAll(
                () -> {
                    // 시즌 1의 모든 경기 조회
                    List<MatchSummary> results = matchQueryRepository.findMatches(1L, null, threshold);
                    assertThat(results).hasSize(2);
                },
                () -> {
                    // 모든 시즌의 리뷰 가능한 경기 조회
                    List<MatchSummary> results = matchQueryRepository.findMatches(null, MatchFilter.REVIEWABLE,
                            threshold);
                    assertThat(results).hasSize(2);
                    assertThat(results).extracting(MatchSummary::id)
                            .containsExactlyInAnyOrder(match1.getId(), match3.getId());
                },
                () -> {
                    // 시즌 1의 리뷰 가능한 경기 조회
                    List<MatchSummary> results = matchQueryRepository.findMatches(1L, MatchFilter.REVIEWABLE,
                            threshold);
                    assertThat(results).hasSize(1);
                    assertThat(results.get(0).id()).isEqualTo(match1.getId());
                },
                () -> {
                    // 필터 없이 모든 경기 조회
                    List<MatchSummary> results = matchQueryRepository.findMatches(null, null, threshold);
                    assertThat(results).hasSize(3);
                }
        );
    }
}
