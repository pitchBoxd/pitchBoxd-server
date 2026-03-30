package com.example.pitchboxd.match.service.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.match.domain.Match;
import com.example.pitchboxd.match.domain.MatchStatus;
import com.example.pitchboxd.match.infrastructure.MatchRepository;
import com.example.pitchboxd.support.DatabaseCleaner;
import java.time.LocalDateTime;
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
class MatchServiceTest {

    @Autowired
    private MatchService matchService;

    @Autowired
    private MatchRepository matchRepository;

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
    void 매치를_정상적으로_조회한다() {
        // given
        Match match = matchRepository.save(new Match(
                1L, "1", 1L, 1L,
                LocalDateTime.now(), MatchStatus.FINISHED, "지구"));
        Long matchId = match.getId();

        // when
        Match result = matchService.findMatch(matchId);

        // then
        assertThat(result.getId()).isEqualTo(matchId);
    }

    @Test
    void 존재하지_않는_매치를_조회할_경우_예외가_발생한다() {
        // given
        Long nonExistMatchId = 1L;

        // when & then
        assertThatThrownBy(() -> matchService.findMatch(nonExistMatchId))
                .isInstanceOf(BusinessException.class);
    }
}
