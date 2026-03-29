package com.example.pitchboxd.matchLineup.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.matchLineup.domain.MatchLineup;
import com.example.pitchboxd.matchLineup.domain.ParticipationStatus;
import com.example.pitchboxd.matchLineup.infrastructure.MatchLineupRepository;
import com.example.pitchboxd.support.DatabaseCleaner;
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
class MatchLineupServiceTest {

    @Autowired
    private MatchLineupService matchLineupService;

    @Autowired
    private MatchLineupRepository matchLineupRepository;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();
    }

    @AfterEach
    void tearDown() {
        databaseCleaner.clean();
    }

    @Test
    void 경기_아이디와_선수_아이디로_경기_라인업을_조회한다() {
        // given
        Long matchId = 1L;
        Long playerId = 1L;
        MatchLineup matchLineup = new MatchLineup(matchId, playerId, 10, ParticipationStatus.STARTER);
        matchLineupRepository.save(matchLineup);

        // when
        MatchLineup result = matchLineupService.findMatchLineup(matchId, playerId);

        // then
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getMatchId()).isEqualTo(matchId),
                () -> assertThat(result.getPlayerId()).isEqualTo(playerId),
                () -> assertThat(result.getStatus()).isEqualTo(ParticipationStatus.STARTER)
        );
    }

    @Test
    void 존재하지_않는_경기_라인업_조회_시_예외가_발생한다() {
        // given
        Long matchId = 999L;
        Long playerId = 999L;

        // when & then
        assertThatThrownBy(() -> matchLineupService.findMatchLineup(matchId, playerId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.MATCH_LINEUP_NOT_FOUND.getMessage());
    }
}
