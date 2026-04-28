package com.example.pitchboxd.match.lineup.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.match.lineup.domain.MatchLineup;
import com.example.pitchboxd.match.lineup.domain.ParticipationStatus;
import com.example.pitchboxd.match.lineup.infrastructure.MatchLineupRepository;
import com.example.pitchboxd.match.lineup.infrastructure.dto.LineupPlayerModel;
import com.example.pitchboxd.player.domain.Player;
import com.example.pitchboxd.player.infrastructure.PlayerRepository;
import com.example.pitchboxd.support.DatabaseCleaner;
import java.util.List;
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
class MatchLineupQueryServiceTest {

    @Autowired
    private MatchLineupQueryService matchLineupQueryService;

    @Autowired
    private MatchLineupRepository matchLineupRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();
    }

    @Test
    void 경기에_참여한_선수들의_라인업을_조회한다() {
        // given
        Long matchId = 1L;
        Player player = playerRepository.save(new Player(1L, "선수1", "n1"));
        Player otherPlayer = playerRepository.save(new Player(2L, "선수2", "n2"));
        matchLineupRepository.save(new MatchLineup(matchId, player.getId(), 7, ParticipationStatus.STARTER));
        matchLineupRepository.save(new MatchLineup(999L, otherPlayer.getId(), 7, ParticipationStatus.STARTER));

        // when
        List<LineupPlayerModel> results = matchLineupQueryService.findLineupAndPlayedPlayers(matchId);

        // then
        assertAll(
                () -> assertThat(results).hasSize(1),
                () -> assertThat(results.get(0).playerName()).isEqualTo("선수1")
        );
    }

    @Test
    void 해당되는_라인업이_없으면_빈_리스트를_반환한다() {
        // when
        List<LineupPlayerModel> results = matchLineupQueryService.findLineupAndPlayedPlayers(999L);

        // then
        assertThat(results).isEmpty();
    }
}
