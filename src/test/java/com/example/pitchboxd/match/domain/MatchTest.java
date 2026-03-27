package com.example.pitchboxd.match.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.matchStatistics.domain.FanType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MatchTest {

    @Test
    void 경기를_생성한다() {
        // given
        Long seasonId = 1L;
        Integer round = 10;
        Long homeTeamId = 1L;
        Long awayTeamId = 2L;
        LocalDateTime dateTime = LocalDateTime.of(2026, 3, 28, 15, 0);
        MatchStatus status = MatchStatus.FINISHED;
        String location = "Seoul Stadium";

        // when
        Match match = new Match(seasonId, round, homeTeamId, awayTeamId, dateTime, status, location, null);

        // then
        assertAll(
                () -> assertThat(match.getSeasonId()).isEqualTo(seasonId),
                () -> assertThat(match.getRound()).isEqualTo(round),
                () -> assertThat(match.getHomeTeamId()).isEqualTo(homeTeamId),
                () -> assertThat(match.getAwayTeamId()).isEqualTo(awayTeamId),
                () -> assertThat(match.getDateTime()).isEqualTo(dateTime),
                () -> assertThat(match.getStatus()).isEqualTo(status),
                () -> assertThat(match.getLocation()).isEqualTo(location)
        );
    }

    @Test
    void 팀_ID가_홈_팀_ID와_같으면_HOME_타입을_반환한다() {
        // given
        Long homeTeamId = 1L;
        Long awayTeamId = 2L;
        Match match = new Match(
                1L, 1, homeTeamId, awayTeamId,
                LocalDateTime.of(2026, 3, 28, 15, 0),
                MatchStatus.FINISHED, "Stadium", null
        );

        // when
        FanType fanType = match.determineFanType(homeTeamId);

        // then
        assertThat(fanType).isEqualTo(FanType.HOME);
    }

    @Test
    void 팀_ID가_어웨이_팀_ID와_같으면_AWAY_타입을_반환한다() {
        // given
        Long homeTeamId = 1L;
        Long awayTeamId = 2L;
        Match match = new Match(
                1L, 1, homeTeamId, awayTeamId,
                LocalDateTime.of(2026, 3, 28, 15, 0),
                MatchStatus.FINISHED, "Stadium", null
        );

        // when
        FanType fanType = match.determineFanType(awayTeamId);

        // then
        assertThat(fanType).isEqualTo(FanType.AWAY);
    }

    @Test
    void 팀_ID가_홈_또는_어웨이_팀_ID와_다르면_NEUTRAL_타입을_반환한다() {
        // given
        Long homeTeamId = 1L;
        Long awayTeamId = 2L;
        Long neutralTeamId = 3L;
        Match match = new Match(
                1L, 1, homeTeamId, awayTeamId,
                LocalDateTime.of(2026, 3, 28, 15, 0),
                MatchStatus.FINISHED, "Stadium", null
        );

        // when
        FanType fanType = match.determineFanType(neutralTeamId);

        // then
        assertThat(fanType).isEqualTo(FanType.NEUTRAL);
    }

    @Test
    void 경기가_종료된_상태이면_true를_반환한다() {
        // given
        Match match = new Match(
                1L, 1, 1L, 2L,
                LocalDateTime.of(2026, 3, 28, 15, 0),
                MatchStatus.FINISHED, "Stadium", null
        );

        // when
        boolean result = match.isEnd();

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 경기가_종료된_상태가_아니면_false를_반환한다() {
        // given
        Match match = new Match(
                1L, 1, 1L, 2L,
                LocalDateTime.of(2026, 3, 28, 15, 0),
                null, "Stadium", null
        );

        // when
        boolean result = match.isEnd();

        // then
        assertThat(result).isFalse();
    }
}
