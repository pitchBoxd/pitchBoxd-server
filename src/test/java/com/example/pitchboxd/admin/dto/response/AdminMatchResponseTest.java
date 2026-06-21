package com.example.pitchboxd.admin.dto.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchResult;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AdminMatchResponseTest {

    @Test
    void 매치_엔티티를_AdminMatchResponse로_정상_변환한다() {
        // given
        LocalDateTime startTime = LocalDateTime.of(2026, 6, 21, 14, 0);
        Match match = new Match(1L, "1라운드", 1L, 2L, startTime, MatchStatus.SCHEDULED, "울산 문수", "naver123");
        ReflectionTestUtils.setField(match, "id", 100L);
        
        // when
        AdminMatchResponse response = AdminMatchResponse.of(match, "울산", "전북");

        // then
        assertAll(
                () -> assertThat(response.id()).isEqualTo(100L),
                () -> assertThat(response.naverId()).isEqualTo("naver123"),
                () -> assertThat(response.homeTeamId()).isEqualTo(1L),
                () -> assertThat(response.homeTeamName()).isEqualTo("울산"),
                () -> assertThat(response.awayTeamId()).isEqualTo(2L),
                () -> assertThat(response.awayTeamName()).isEqualTo("전북"),
                () -> assertThat(response.round()).isEqualTo("1라운드"),
                () -> assertThat(response.startTime()).isEqualTo(startTime),
                () -> assertThat(response.status()).isEqualTo(MatchStatus.SCHEDULED),
                () -> assertThat(response.homeScore()).isNull(),
                () -> assertThat(response.awayScore()).isNull(),
                () -> assertThat(response.location()).isEqualTo("울산 문수")
        );
    }

    @Test
    void 종료된_경기는_점수를_포함하여_정상_변환한다() {
        // given
        LocalDateTime startTime = LocalDateTime.of(2026, 6, 21, 14, 0);
        Match match = new Match(1L, "1라운드", 1L, 2L, startTime, MatchStatus.FINISHED, "울산 문수", "naver123");
        ReflectionTestUtils.setField(match, "id", 100L);
        match.finish(startTime.plusHours(2));
        match.decideMatchResult(new MatchResult(2, 1, List.of(), List.of()));

        // when
        AdminMatchResponse response = AdminMatchResponse.of(match, "울산", "전북");

        // then
        assertAll(
                () -> assertThat(response.status()).isEqualTo(MatchStatus.FINISHED),
                () -> assertThat(response.homeScore()).isEqualTo(2),
                () -> assertThat(response.awayScore()).isEqualTo(1),
                () -> assertThat(response.finishedAt()).isEqualTo(startTime.plusHours(2))
        );
    }
}
