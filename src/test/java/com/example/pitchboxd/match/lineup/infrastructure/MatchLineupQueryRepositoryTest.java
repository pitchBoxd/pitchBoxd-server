package com.example.pitchboxd.match.lineup.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.global.config.QueryDslConfig;
import com.example.pitchboxd.match.lineup.domain.MatchLineup;
import com.example.pitchboxd.match.lineup.domain.ParticipationStatus;
import com.example.pitchboxd.match.lineup.infrastructure.dto.LineupPlayerModel;
import com.example.pitchboxd.player.domain.Player;
import com.example.pitchboxd.player.infrastructure.PlayerRepository;
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
@Import({MatchLineupQueryRepository.class, QueryDslConfig.class})
@DisplayNameGeneration(ReplaceUnderscores.class)
class MatchLineupQueryRepositoryTest {

    @Autowired
    private MatchLineupQueryRepository matchLineupQueryRepository;

    @Autowired
    private MatchLineupRepository matchLineupRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Test
    void 경기에_참여한_선수들의_라인업을_조회한다_벤치_멤버는_제외한다() {
        // given
        Long matchId = 1L;
        Long teamId = 1L;

        Player starterPlayer = playerRepository.save(new Player(teamId, "선발선수", "n1"));
        Player subInPlayer = playerRepository.save(new Player(teamId, "교체선수", "n2"));
        Player benchPlayer = playerRepository.save(new Player(teamId, "벤치선수", "n3"));
        Player otherMatchPlayer = playerRepository.save(new Player(teamId, "다른경기선수", "n4"));

        matchLineupRepository.save(new MatchLineup(matchId, starterPlayer.getId(), 7, ParticipationStatus.STARTER));
        matchLineupRepository.save(new MatchLineup(matchId, subInPlayer.getId(), 10, ParticipationStatus.SUBSTITUTED_IN));
        matchLineupRepository.save(new MatchLineup(matchId, benchPlayer.getId(), 20, ParticipationStatus.BENCH));
        matchLineupRepository.save(new MatchLineup(999L, otherMatchPlayer.getId(), 1, ParticipationStatus.STARTER));

        // when
        List<LineupPlayerModel> results = matchLineupQueryRepository.findLineupPlayersByMatchId(matchId);

        // then
        assertAll(
                () -> assertThat(results).hasSize(2),
                () -> assertThat(results).extracting(LineupPlayerModel::playerName)
                        .containsExactlyInAnyOrder("선발선수", "교체선수"),
                () -> assertThat(results).extracting(LineupPlayerModel::status)
                        .containsExactlyInAnyOrder(ParticipationStatus.STARTER, ParticipationStatus.SUBSTITUTED_IN),
                () -> {
                    LineupPlayerModel starter = results.stream()
                            .filter(r -> r.playerName().equals("선발선수"))
                            .findFirst().get();
                    assertThat(starter.backNumber()).isEqualTo(7);
                    assertThat(starter.teamId()).isEqualTo(teamId);
                }
        );
    }
}
