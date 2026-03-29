package com.example.pitchboxd.matchLineup.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class MatchLineupTest {

    @Test
    void 매치_라인업을_생성한다() {
        // given
        Long matchId = 1L;
        Long playerId = 2L;
        Integer backNumber = 10;
        ParticipationStatus status = ParticipationStatus.STARTER;

        // when
        MatchLineup matchLineup = new MatchLineup(matchId, playerId, backNumber, status);

        // then
        assertAll(
                () -> assertThat(matchLineup.getMatchId()).isEqualTo(matchId),
                () -> assertThat(matchLineup.getPlayerId()).isEqualTo(playerId),
                () -> assertThat(matchLineup.getBackNumber()).isEqualTo(backNumber),
                () -> assertThat(matchLineup.getStatus()).isEqualTo(status)
        );
    }

    @Test
    void 선발_상태인_경우_경기_참여로_판단한다() {
        // given
        MatchLineup matchLineup = new MatchLineup(1L, 1L, 7, ParticipationStatus.STARTER);

        // when
        boolean result = matchLineup.isParticipated();

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 교체_투입_상태인_경우_경기_참여로_판단한다() {
        // given
        MatchLineup matchLineup = new MatchLineup(1L, 1L, 10, ParticipationStatus.SUBSTITUTED_IN);

        // when
        boolean result = matchLineup.isParticipated();

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 벤치_상태로_경기_종료인_경우_경기_참여가_아닌_것으로_판단한다() {
        // given
        MatchLineup matchLineup = new MatchLineup(1L, 1L, 11, ParticipationStatus.BENCH);

        // when
        boolean result = matchLineup.isParticipated();

        // then
        assertThat(result).isFalse();
    }
}
