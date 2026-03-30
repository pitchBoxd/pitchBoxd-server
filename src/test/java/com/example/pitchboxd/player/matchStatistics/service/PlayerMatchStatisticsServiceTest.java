package com.example.pitchboxd.player.matchStatistics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.player.matchStatistics.domain.PlayerMatchStatistics;
import com.example.pitchboxd.player.matchStatistics.infrastructure.PlayerMatchStatisticsRepository;
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
class PlayerMatchStatisticsServiceTest {

    @Autowired
    private PlayerMatchStatisticsService playerMatchStatisticsService;

    @Autowired
    private PlayerMatchStatisticsRepository playerMatchStatisticsRepository;

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
    void 플레이어_매치_통계의_리뷰를_업데이트한다() {
        // given
        Long matchId = 1L;
        Long playerId = 1L;
        int point = 5;
        PlayerMatchStatistics playerMatchStatistics = new PlayerMatchStatistics(matchId, playerId);
        playerMatchStatisticsRepository.save(playerMatchStatistics);

        // when
        playerMatchStatisticsService.updateReview(matchId, playerId, point);

        // then
        PlayerMatchStatistics result = playerMatchStatisticsRepository.findByMatchIdAndPlayerId(matchId, playerId)
                .orElseThrow();
        assertThat(result).isNotNull();
    }

    @Test
    void 플레이어_매치_통계_리뷰_업데이트_시_경계값인_0점을_허용한다() {
        // given
        Long matchId = 1L;
        Long playerId = 1L;
        int point = 0;
        PlayerMatchStatistics playerMatchStatistics = new PlayerMatchStatistics(matchId, playerId);
        playerMatchStatisticsRepository.save(playerMatchStatistics);

        // when
        playerMatchStatisticsService.updateReview(matchId, playerId, point);

        // then
        PlayerMatchStatistics result = playerMatchStatisticsRepository.findByMatchIdAndPlayerId(matchId, playerId)
                .orElseThrow();
        assertThat(result).isNotNull();
    }

    @Test
    void 존재하지_않는_플레이어_매치_통계인_경우_예외가_발생한다() {
        // given
        Long matchId = 1L;
        Long playerId = 1L;
        int point = 5;

        // when & then
        assertThatThrownBy(() -> playerMatchStatisticsService.updateReview(matchId, playerId, point))
                .isInstanceOf(BusinessException.class);
    }
}
