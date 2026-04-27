package com.example.pitchboxd.team.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.support.DatabaseCleaner;
import com.example.pitchboxd.team.domain.Team;
import com.example.pitchboxd.team.infrastructure.TeamRepository;
import java.util.List;
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
class TeamQueryServiceTest {

    @Autowired
    private TeamQueryService teamQueryService;

    @Autowired
    private TeamRepository teamRepository;

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
    void 네이버_코드로_팀을_조회한다() {
        // given
        String naverCode = "naver-123";
        Team team = new Team("팀A", naverCode);
        teamRepository.save(team);

        // when
        Team result = teamQueryService.findByNaverCode(naverCode);

        // then
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getName()).isEqualTo("팀A")
        );
    }

    @Test
    void 존재하지_않는_네이버_코드로_조회시_예외가_발생한다() {
        // given
        String wrongCode = "wrong-code";

        // when & then
        assertThatThrownBy(() -> teamQueryService.findByNaverCode(wrongCode))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TEAM_NOT_FOUND);
    }

    @Test
    void 모든_팀을_조회한다() {
        // given
        teamRepository.save(new Team("팀A", "code1"));
        teamRepository.save(new Team("팀B", "code2"));

        // when
        List<Team> allTeams = teamQueryService.findAllTeam();

        // then
        assertThat(allTeams).hasSize(2);
    }
}
