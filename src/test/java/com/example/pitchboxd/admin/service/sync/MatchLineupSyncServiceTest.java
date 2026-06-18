package com.example.pitchboxd.admin.service.sync;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.example.pitchboxd.global.infrastructure.naver.NaverSportsClient;
import com.example.pitchboxd.global.infrastructure.naver.dto.NaverLineupResponse;
import com.example.pitchboxd.global.infrastructure.naver.dto.NaverLineupResponse.NaverPlayerNode;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import com.example.pitchboxd.match.core.infrastructure.MatchRepository;
import com.example.pitchboxd.match.playerStatistics.domain.PlayerStatistics;
import com.example.pitchboxd.match.playerStatistics.infrastructure.PlayerStatisticsRepository;
import com.example.pitchboxd.player.domain.Player;
import com.example.pitchboxd.player.infrastructure.PlayerRepository;
import com.example.pitchboxd.support.DatabaseCleaner;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class MatchLineupSyncServiceTest {

    @Autowired
    private MatchLineupSyncService matchLineupSyncService;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PlayerStatisticsRepository playerStatisticsRepository;

    @MockitoBean
    private NaverSportsClient naverSportsClient;

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

    @DisplayName("라인업을 동기화할 때, 출전한 선수들의 통계 엔티티도 같이 생성된다.")
    @Test
    void syncLineup_createsPlayerStatistics() {
        // given
        Match match = matchRepository.save(new Match(1L, "1", 1L, 2L, LocalDateTime.now(), MatchStatus.SCHEDULED, "Stadium", "naver-game-id"));
        
        Player player1 = playerRepository.save(new Player(1L, "선수1", "naver-p1"));
        Player player2 = playerRepository.save(new Player(2L, "선수2", "naver-p2"));

        NaverPlayerNode node1 = new NaverPlayerNode("naver-p1", "7", "선수1", false);
        NaverPlayerNode node2 = new NaverPlayerNode("naver-p2", "9", "선수2", false);

        NaverLineupResponse mockResponse = new NaverLineupResponse(
                new NaverLineupResponse.ResultNode(
                        new NaverLineupResponse.LineUpDataNode(
                                new NaverLineupResponse.SubstitutionNode(List.of(), List.of()),
                                new NaverLineupResponse.LineupNode(
                                        new NaverLineupResponse.TeamLineupNode(List.of(List.of(node1))),
                                        new NaverLineupResponse.TeamLineupNode(List.of(List.of(node2)))
                                )
                        )
                )
        );

        given(naverSportsClient.getMatchLineup("naver-game-id")).willReturn(mockResponse);

        // when
        matchLineupSyncService.syncLineup("naver-game-id");

        // then
        List<PlayerStatistics> stats = playerStatisticsRepository.findAllByMatchId(match.getId());
        assertThat(stats).hasSize(2);
        assertThat(stats).extracting(PlayerStatistics::getPlayerId)
                .containsExactlyInAnyOrder(player1.getId(), player2.getId());
    }

    @DisplayName("라인업을 동기화할 때, naver_id가 일치하지 않더라도 소속팀과 이름이 일치하면 대체 매칭되어 라인업이 등록된다.")
    @Test
    void syncLineup_fallbackToName_whenNaverIdMismatched() {
        // given
        Long homeTeamId = 1L;
        Match match = matchRepository.save(new Match(1L, "1", homeTeamId, 2L, LocalDateTime.now(), MatchStatus.SCHEDULED, "Stadium", "naver-game-id-fallback"));
        
        // DB에 저장된 선수의 naverId는 'naver-p1-old' 이지만, 네이버 API에서 주는 ID는 'naver-p1-new'인 경우
        Player player = playerRepository.save(new Player(homeTeamId, "김선수", "naver-p1-old"));

        // API 결과 데이터 정의
        NaverPlayerNode node = new NaverPlayerNode("naver-p1-new", "7", "김선수", false);

        NaverLineupResponse mockResponse = new NaverLineupResponse(
                new NaverLineupResponse.ResultNode(
                        new NaverLineupResponse.LineUpDataNode(
                                new NaverLineupResponse.SubstitutionNode(List.of(), List.of()),
                                new NaverLineupResponse.LineupNode(
                                        new NaverLineupResponse.TeamLineupNode(List.of(List.of(node))),
                                        new NaverLineupResponse.TeamLineupNode(List.of())
                                )
                        )
                )
        );

        given(naverSportsClient.getMatchLineup("naver-game-id-fallback")).willReturn(mockResponse);

        // when
        matchLineupSyncService.syncLineup("naver-game-id-fallback");

        // then
        List<PlayerStatistics> stats = playerStatisticsRepository.findAllByMatchId(match.getId());
        assertThat(stats).hasSize(1);
        assertThat(stats.get(0).getPlayerId()).isEqualTo(player.getId());
    }
}

