package com.example.pitchboxd.player.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.player.domain.Player;
import com.example.pitchboxd.player.infrastructure.PlayerRepository;
import com.example.pitchboxd.support.DatabaseCleaner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DisplayNameGeneration(ReplaceUnderscores.class)
class PlayerServiceTest {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private PlayerRepository playerRepository;

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
    void 플레이어를_성공적으로_조회한다() {
        // given
        Player player = new Player(1L, "손흥민", "1");
        Player savedPlayer = playerRepository.save(player);

        // when
        Player result = playerService.findPlayer(savedPlayer.getId());

        // then
        assertAll(
                () -> assertThat(result.getId()).isEqualTo(savedPlayer.getId()),
                () -> assertThat(result.getName()).isEqualTo("손흥민")
        );
    }

    @Test
    void 존재하지_않는_플레이어를_조회하면_예외가_발생한다() {
        // given
        Long notExistId = 999L;

        // when & then
        assertThatThrownBy(() -> playerService.findPlayer(notExistId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PLAYER_NOT_FOUND);
    }
}
