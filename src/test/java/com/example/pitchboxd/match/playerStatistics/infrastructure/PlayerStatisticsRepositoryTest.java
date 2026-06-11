package com.example.pitchboxd.match.playerStatistics.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.pitchboxd.match.playerStatistics.domain.PlayerStatistics;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PlayerStatisticsRepositoryTest {

    @Autowired
    private PlayerStatisticsRepository playerStatisticsRepository;

    @Test
    void 매치_ID로_모든_선수_통계를_조회한다() {
        // given
        PlayerStatistics stats1 = playerStatisticsRepository.save(new PlayerStatistics(100L, 1L));
        PlayerStatistics stats2 = playerStatisticsRepository.save(new PlayerStatistics(101L, 1L));
        playerStatisticsRepository.save(new PlayerStatistics(100L, 2L)); // 다른 매치

        // when
        List<PlayerStatistics> matchStats = playerStatisticsRepository.findAllByMatchId(1L);

        // then
        assertThat(matchStats).hasSize(2)
                .extracting(PlayerStatistics::getId)
                .containsExactlyInAnyOrder(stats1.getId(), stats2.getId());
    }
}
