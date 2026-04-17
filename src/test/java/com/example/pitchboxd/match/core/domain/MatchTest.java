package com.example.pitchboxd.match.core.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.match.matchStatistics.domain.FanType;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MatchTest {

    private static Stream<Arguments> provideDurationConditions() {
        LocalDateTime now = LocalDateTime.of(2026, 3, 28, 20, 0);
        Duration duration = Duration.ofHours(2);

        return Stream.of(
                // finishedAt(17:00) + duration(2h) = 19:00 < now(20:00) -> true
                Arguments.of(duration, now.minusHours(3), now, true),
                // finishedAt(19:00) + duration(2h) = 21:00 > now(20:00) -> false
                Arguments.of(duration, now.minusHours(1), now, false)
        );
    }

    private static Stream<Arguments> provideInvalidMatchConditions() {
        LocalDateTime now = LocalDateTime.now();
        return Stream.of(
                Arguments.of(null, now.minusHours(1)),      // MatchStatus가 null인 경우
                Arguments.of(MatchStatus.FINISHED, null),   // finishedAt이 null인 경우
                Arguments.of(MatchStatus.SCHEDULED, now.minusHours(1)), // FINISHED가 아닌 상태
                Arguments.of(MatchStatus.FINISHED, now.plusHours(1))    // finishedAt이 현재 이후인 경우
        );
    }

    @Test
    void 경기를_생성한다() {
        // given
        Long seasonId = 1L;
        String round = "10";
        Long homeTeamId = 1L;
        Long awayTeamId = 2L;
        LocalDateTime dateTime = LocalDateTime.of(2026, 3, 28, 15, 0);
        MatchStatus status = MatchStatus.FINISHED;
        String location = "Seoul Stadium";

        // when
        Match match = new Match(seasonId, round, homeTeamId, awayTeamId, dateTime, status, location, "1");

        // then
        assertAll(
                () -> assertThat(match.getSeasonId()).isEqualTo(seasonId),
                () -> assertThat(match.getRound()).isEqualTo(round),
                () -> assertThat(match.getHomeTeamId()).isEqualTo(homeTeamId),
                () -> assertThat(match.getAwayTeamId()).isEqualTo(awayTeamId),
                () -> assertThat(match.getStartTime()).isEqualTo(dateTime),
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
                1L, "1", homeTeamId, awayTeamId,
                LocalDateTime.of(2026, 3, 28, 15, 0),
                MatchStatus.FINISHED, "Stadium", "1"
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
                1L, "1", homeTeamId, awayTeamId,
                LocalDateTime.of(2026, 3, 28, 15, 0),
                MatchStatus.FINISHED, "Stadium", "1"
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
                1L, "1", homeTeamId, awayTeamId,
                LocalDateTime.of(2026, 3, 28, 15, 0),
                MatchStatus.FINISHED, "Stadium", "1"
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
                1L, "1", 1L, 2L,
                LocalDateTime.of(2026, 3, 28, 15, 0),
                MatchStatus.FINISHED, "Stadium", "1"
        );

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime oneHourBefore = now.minusHours(1);

        match.finish(oneHourBefore);

        // when
        boolean result = match.isEnd(now);

        // then
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("provideInvalidMatchConditions")
    void 경기가_종료된_상태가_아니면_false를_반환한다(MatchStatus status, LocalDateTime finishedAt) {
        // given
        Match match = new Match(
                1L, "1", 1L, 2L,
                LocalDateTime.of(2026, 3, 28, 15, 0),
                status, "Stadium", "1"
        );

        ReflectionTestUtils.setField(match, "finishedAt", finishedAt);

        LocalDateTime now = LocalDateTime.now();

        // when
        boolean result = match.isEnd(now);

        // then
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("provideDurationConditions")
    void 지정된_시간이_지났는지_확인한다(Duration duration, LocalDateTime finishedAt, LocalDateTime now, boolean expected) {
        // given
        Match match = new Match(
                1L, "1", 1L, 2L,
                LocalDateTime.of(2026, 3, 28, 15, 0),
                MatchStatus.FINISHED, "Stadium", "1"
        );

        ReflectionTestUtils.setField(match, "finishedAt", finishedAt);

        // when
        boolean result = match.isPassed(now, duration);

        // then
        assertThat(result).isEqualTo(expected);
    }
}
