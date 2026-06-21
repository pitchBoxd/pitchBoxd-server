package com.example.pitchboxd.admin.service.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;

import com.example.pitchboxd.admin.dto.request.UpdatePlayerRequest;
import com.example.pitchboxd.admin.dto.request.CreateMatchRequest;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import com.example.pitchboxd.match.core.infrastructure.MatchRepository;
import com.example.pitchboxd.match.lineup.domain.MatchLineup;
import com.example.pitchboxd.match.lineup.infrastructure.MatchLineupRepository;
import com.example.pitchboxd.player.domain.Player;
import com.example.pitchboxd.player.infrastructure.PlayerRepository;
import com.example.pitchboxd.global.infrastructure.naver.NaverSportsClient;
import com.example.pitchboxd.global.infrastructure.naver.dto.NaverMatchDetailResponse;
import com.example.pitchboxd.global.infrastructure.naver.dto.NaverLineupResponse;
import com.example.pitchboxd.global.infrastructure.naver.dto.NaverLineupResponse.NaverPlayerNode;
import com.example.pitchboxd.support.DatabaseCleaner;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
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
import com.example.pitchboxd.user.domain.User;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class AdminFacadeServiceTest {

    @Autowired
    private AdminFacadeService adminFacadeService;

    @Autowired
    private EntityManager em;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private MatchLineupRepository matchLineupRepository;

    @MockitoBean
    private NaverSportsClient naverSportsClient;

    @Autowired
    private com.example.pitchboxd.user.infrastructure.UserRepository userRepository;

    @Autowired
    private com.example.pitchboxd.team.infrastructure.TeamRepository teamRepository;

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

    @DisplayName("기간 내 종료된 경기가 있으면 자동으로 경기 결과를 종료 처리하고 라인업을 동기화한다.")
    @Test
    void autoFinishMatchesAndUpdateLineup_success() {
        // given
        LocalDate from = LocalDate.now().minusDays(1);
        LocalDate to = LocalDate.now().plusDays(1);
        CreateMatchRequest request = new CreateMatchRequest(from, to);

        Match match = matchRepository.save(new Match(1L, "1", 1L, 2L, LocalDateTime.now(), MatchStatus.SCHEDULED, "Stadium", "naver-game-id-auto"));
        Player player = playerRepository.save(new Player(1L, "선수1", "naver-p1"));

        // NaverMatchDetailResponse Mocking
        NaverMatchDetailResponse.ScorersNode scorers = new NaverMatchDetailResponse.ScorersNode(List.of(), List.of());
        NaverMatchDetailResponse.GameNode game = new NaverMatchDetailResponse.GameNode("RESULT", 2, 1, scorers);
        NaverMatchDetailResponse.ResultNode resultNode = new NaverMatchDetailResponse.ResultNode(game);
        NaverMatchDetailResponse detailResponse = new NaverMatchDetailResponse(resultNode);
        given(naverSportsClient.getMatchDetail("naver-game-id-auto")).willReturn(detailResponse);

        // NaverLineupResponse Mocking
        NaverPlayerNode node = new NaverPlayerNode("naver-p1", "7", "선수1", false);
        NaverLineupResponse lineupResponse = new NaverLineupResponse(
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
        given(naverSportsClient.getMatchLineup("naver-game-id-auto")).willReturn(lineupResponse);

        // when
        adminFacadeService.autoFinishMatchesAndUpdateLineup(request);
        em.clear();

        // then
        Match updatedMatch = matchRepository.findById(match.getId()).orElseThrow();
        assertThat(updatedMatch.getStatus()).isEqualTo(MatchStatus.FINISHED);
        
        List<MatchLineup> lineups = matchLineupRepository.findAll();
        assertThat(lineups).hasSize(1);
    }

    @DisplayName("모든 유저 정보를 조회하여 DTO 리스트로 반환한다.")
    @Test
    void getAllUsers_success() {
        // given
        userRepository.save(new User("테스트1", "test1@example.com", "password123!"));
        userRepository.save(new User("테스트2", "test2@example.com", "password123!"));

        // when
        List<com.example.pitchboxd.admin.dto.response.AdminUserResponse> users = adminFacadeService.getAllUsers();

        // then
        assertThat(users).hasSize(2);
    }

    @DisplayName("특정 시즌의 모든 경기를 DTO 목록으로 변환하여 조회한다.")
    @Test
    void getMatchesBySeason_success() {
        // given
        com.example.pitchboxd.team.domain.Team home = teamRepository.save(new com.example.pitchboxd.team.domain.Team("울산", "n1"));
        com.example.pitchboxd.team.domain.Team away = teamRepository.save(new com.example.pitchboxd.team.domain.Team("전북", "n2"));
        
        matchRepository.save(new Match(10L, "1", home.getId(), away.getId(), LocalDateTime.now(), MatchStatus.SCHEDULED, "울산 문수", "naver-m1"));
        matchRepository.save(new Match(10L, "2", home.getId(), away.getId(), LocalDateTime.now().plusHours(1), MatchStatus.SCHEDULED, "울산 문수", "naver-m2"));
        matchRepository.save(new Match(20L, "1", home.getId(), away.getId(), LocalDateTime.now(), MatchStatus.SCHEDULED, "울산 문수", "naver-m3"));

        // when
        List<com.example.pitchboxd.admin.dto.response.AdminMatchResponse> results = adminFacadeService.getMatchesBySeason(10L);

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).homeTeamName()).isEqualTo("울산");
        assertThat(results.get(0).awayTeamName()).isEqualTo("전북");
    }
}
