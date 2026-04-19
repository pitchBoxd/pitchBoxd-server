package com.example.pitchboxd.match.core.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.global.config.QueryDslConfig;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import com.example.pitchboxd.match.core.infrastructure.dto.MatchSummary;
import com.example.pitchboxd.match.matchStatistics.domain.FanType;
import com.example.pitchboxd.match.matchStatistics.domain.MatchStatistics;
import com.example.pitchboxd.match.matchStatistics.infrastructure.MatchStatisticsRepository;
import com.example.pitchboxd.team.domain.Team;
import com.example.pitchboxd.team.infrastructure.TeamRepository;
import java.time.LocalDateTime;
import java.util.List;
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
    private MatchStatisticsRepository matchStatisticsRepository;

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
}
