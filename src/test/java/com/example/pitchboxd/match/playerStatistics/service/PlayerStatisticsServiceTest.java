package com.example.pitchboxd.match.playerStatistics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.match.playerStatistics.domain.PlayerStatistics;
import com.example.pitchboxd.match.playerStatistics.infrastructure.PlayerStatisticsRepository;
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
class PlayerStatisticsServiceTest {

    @Autowired
    private PlayerStatisticsService playerStatisticsService;

    @Autowired
    private PlayerStatisticsRepository playerStatisticsRepository;

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
        PlayerStatistics playerStatistics = new PlayerStatistics(matchId, playerId);
        playerStatisticsRepository.save(playerStatistics);

        // when
        playerStatisticsService.updateReview(matchId, playerId, point);

        // then
        PlayerStatistics result = playerStatisticsRepository.findByMatchIdAndPlayerId(matchId, playerId)
                .orElseThrow();
        assertThat(result).isNotNull();
    }

    @Test
    void 플레이어_매치_통계_리뷰_업데이트_시_경계값인_0점을_허용한다() {
        // given
        Long matchId = 1L;
        Long playerId = 1L;
        int point = 0;
        PlayerStatistics playerStatistics = new PlayerStatistics(matchId, playerId);
        playerStatisticsRepository.save(playerStatistics);

        // when
        playerStatisticsService.updateReview(matchId, playerId, point);

        // then
        PlayerStatistics result = playerStatisticsRepository.findByMatchIdAndPlayerId(matchId, playerId)
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
        assertThatThrownBy(() -> playerStatisticsService.updateReview(matchId, playerId, point))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 플레이어_매치_통계의_리뷰_점수를_조정한다() {
        // given
        Long matchId = 1L;
        Long playerId = 1L;
        int differenceOfPoint = 2;
        PlayerStatistics playerStatistics = new PlayerStatistics(matchId, playerId);
        playerStatisticsRepository.save(playerStatistics);

        // when
        playerStatisticsService.adjustReviewStatistics(matchId, playerId, differenceOfPoint);

        // then
        PlayerStatistics result = playerStatisticsRepository.findByMatchIdAndPlayerId(matchId, playerId)
                .orElseThrow();
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getTotalScore()).isEqualTo(differenceOfPoint),
                () -> assertThat(result.getReviewCount()).isZero()
        );
    }

    @Test
    void 존재하지_않는_플레이어_매치_통계의_점수_조정_시_예외가_발생한다() {
        // given
        Long matchId = 1L;
        Long playerId = 1L;
        int differenceOfPoint = 2;

        // when & then
        assertThatThrownBy(
                () -> playerStatisticsService.adjustReviewStatistics(matchId, playerId, differenceOfPoint))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.PLAYER_STATISTICS_NOT_FOUND.getMessage());
    }

    @Test
    void 플레이어_매치_통계의_리뷰를_삭제한다() {
        // given
        Long matchId = 1L;
        Long playerId = 1L;
        int point = 5;
        PlayerStatistics playerStatistics = new PlayerStatistics(matchId, playerId);
        playerStatistics.addNewReview(point);
        playerStatisticsRepository.save(playerStatistics);

        // when
        playerStatisticsService.removeReview(matchId, playerId, point);

        // then
        PlayerStatistics result = playerStatisticsRepository.findByMatchIdAndPlayerId(matchId, playerId)
                .orElseThrow();
        assertAll(
                () -> assertThat(result.getTotalScore()).isZero(),
                () -> assertThat(result.getReviewCount()).isZero()
        );
    }

    @Test
    void 플레이어_매치_통계_리뷰_삭제_시_경계값인_0점을_허용한다() {
        // given
        Long matchId = 1L;
        Long playerId = 1L;
        int point = 0;
        PlayerStatistics playerStatistics = new PlayerStatistics(matchId, playerId);
        playerStatistics.addNewReview(point);
        playerStatisticsRepository.save(playerStatistics);

        // when
        playerStatisticsService.removeReview(matchId, playerId, point);

        // then
        PlayerStatistics result = playerStatisticsRepository.findByMatchIdAndPlayerId(matchId, playerId)
                .orElseThrow();
        assertThat(result).isNotNull();
    }

    @Test
    void 존재하지_않는_플레이어_매치_통계의_리뷰_삭제_시_예외가_발생한다() {
        // given
        Long matchId = 1L;
        Long playerId = 1L;
        int point = 5;

        // when & then
        assertThatThrownBy(() -> playerStatisticsService.removeReview(matchId, playerId, point))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.PLAYER_STATISTICS_NOT_FOUND.getMessage());
    }
    @Test
    void 여러_선수들의_통계_엔티티를_생성하고_저장한다() {
        // given
        Long matchId = 1L;
        java.util.List<Long> playerIds = java.util.List.of(10L, 20L, 30L);

        // when
        playerStatisticsService.createAllPlayerStatistics(matchId, playerIds);

        // then
        java.util.List<PlayerStatistics> stats = playerStatisticsRepository.findAllByMatchId(matchId);
        assertThat(stats).hasSize(3);
        assertThat(stats).extracting(PlayerStatistics::getPlayerId)
                .containsExactlyInAnyOrder(10L, 20L, 30L);
    }

    @Test
    void 이미_생성된_선수_통계는_덮어쓰지_않고_없는_선수들의_통계만_추가_생성한다() {
        // given
        Long matchId = 1L;
        playerStatisticsRepository.save(new PlayerStatistics(10L, matchId)); // 이미 10번 선수 통계 존재
        
        java.util.List<Long> playerIds = java.util.List.of(10L, 20L, 30L);

        // when
        playerStatisticsService.createAllPlayerStatistics(matchId, playerIds);

        // then
        java.util.List<PlayerStatistics> stats = playerStatisticsRepository.findAllByMatchId(matchId);
        assertThat(stats).hasSize(3); // 추가로 20, 30만 생성되어 총 3개여야 함
        assertThat(stats).extracting(PlayerStatistics::getPlayerId)
                .containsExactlyInAnyOrder(10L, 20L, 30L);
    }
}

