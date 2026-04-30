package com.example.pitchboxd.admin.service.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.admin.dto.request.UpdatePlayerRequest;
import com.example.pitchboxd.player.domain.Player;
import com.example.pitchboxd.player.infrastructure.PlayerRepository;
import com.example.pitchboxd.support.DatabaseCleaner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class AdminFacadeServiceTest {

    @Autowired
    private AdminFacadeService adminFacadeService;

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

    @DisplayName("선수 정보를 수정한다.")
    @Test
    void updatePlayer_success() {
        // given
        Player player = new Player(1L, "기존 이름", "oldNaverId");
        Player savedPlayer = playerRepository.save(player);
        Long playerId = savedPlayer.getId();
        UpdatePlayerRequest request = new UpdatePlayerRequest(2L, "새 이름", "newNaverId");

        // when
        adminFacadeService.updatePlayer(playerId, request);

        // then
        Player result = playerRepository.findById(playerId).orElseThrow();
        
        assertAll(
                () -> assertThat(result.getTeamId()).isEqualTo(2L),
                () -> assertThat(result.getName()).isEqualTo("새 이름"),
                () -> assertThat(result.getNaverId()).isEqualTo("newNaverId")
        );
    }
}
