# Create Player Statistics with Lineup Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 경기 라인업이 생성(동기화)될 때 관련 선수들의 선수 통계([PlayerStatistics](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/match/playerStatistics/domain/PlayerStatistics.java)) 엔티티도 데이터베이스에 같이 생성되어 저장되도록 처리합니다.

**Architecture:** 라인업 동기화([MatchLineupSyncService](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/admin/service/sync/MatchLineupSyncService.java))가 완료된 후, 라인업에 포함된 선수들의 ID 목록을 추출하여 [PlayerStatisticsService](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/match/playerStatistics/service/PlayerStatisticsService.java)를 호출합니다. [PlayerStatisticsService](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/match/playerStatistics/service/PlayerStatisticsService.java)는 이미 해당 경기에 통계 정보가 생성되어 있는 선수를 제외하고, 신규 선수들에 대해서만 [PlayerStatistics](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/match/playerStatistics/domain/PlayerStatistics.java)) 엔티티를 생성하여 [PlayerStatisticsRepository](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/match/playerStatistics/infrastructure/PlayerStatisticsRepository.java)를 통해 저장하여 중복 생성을 막고 멱등성을 보장합니다.

**Tech Stack:** Java, Spring Boot, Spring Data JPA, JUnit 5

---

### Task 1: `PlayerStatisticsService`에 `createAllPlayerStatistics` 메소드 추가

**Files:**
- Modify: [PlayerStatisticsService.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/match/playerStatistics/service/PlayerStatisticsService.java)
- Modify: [PlayerStatisticsServiceTest.java](file:///Users/yuseokhyeon/pitchboxd/src/test/java/com/example/pitchboxd/match/playerStatistics/service/PlayerStatisticsServiceTest.java)

- [ ] **Step 1: Write the failing test**
[PlayerStatisticsServiceTest.java](file:///Users/yuseokhyeon/pitchboxd/src/test/java/com/example/pitchboxd/match/playerStatistics/service/PlayerStatisticsServiceTest.java)에 선수 통계 다중 생성 및 중복 방지(멱등성) 테스트인 `createAllPlayerStatistics_success`와 `createAllPlayerStatistics_idempotency`를 추가합니다.

```java
    @DisplayName("여러 선수들의 통계 엔티티를 생성하고 저장한다.")
    @Test
    void createAllPlayerStatistics_success() {
        // given
        Long matchId = 1L;
        List<Long> playerIds = List.of(10L, 20L, 30L);

        // when
        playerStatisticsService.createAllPlayerStatistics(matchId, playerIds);

        // then
        List<PlayerStatistics> stats = playerStatisticsRepository.findAllByMatchId(matchId);
        assertThat(stats).hasSize(3);
        assertThat(stats).extracting(PlayerStatistics::getPlayerId)
                .containsExactlyInAnyOrder(10L, 20L, 30L);
    }

    @DisplayName("이미 생성된 선수 통계는 덮어쓰지 않고, 없는 선수들의 통계만 추가 생성한다.")
    @Test
    void createAllPlayerStatistics_idempotency() {
        // given
        Long matchId = 1L;
        playerStatisticsRepository.save(new PlayerStatistics(10L, matchId)); // 이미 10번 선수 통계 존재
        
        List<Long> playerIds = List.of(10L, 20L, 30L);

        // when
        playerStatisticsService.createAllPlayerStatistics(matchId, playerIds);

        // then
        List<PlayerStatistics> stats = playerStatisticsRepository.findAllByMatchId(matchId);
        assertThat(stats).hasSize(3); // 추가로 20, 30만 생성되어 총 3개여야 함
        assertThat(stats).extracting(PlayerStatistics::getPlayerId)
                .containsExactlyInAnyOrder(10L, 20L, 30L);
    }
```

- [ ] **Step 2: Run test to verify it fails**
Run: `./gradlew test --tests "*PlayerStatisticsServiceTest*"`
Expected: 컴파일 에러 (메소드가 존재하지 않음)

- [ ] **Step 3: Write minimal implementation**
[PlayerStatisticsService.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/match/playerStatistics/service/PlayerStatisticsService.java)에 `createAllPlayerStatistics` 메소드를 구현합니다.

```java
    @Transactional
    public void createAllPlayerStatistics(Long matchId, List<Long> playerIds) {
        List<PlayerStatistics> existingStats = playerStatisticsRepository.findAllByMatchId(matchId);
        java.util.Set<Long> existingPlayerIds = existingStats.stream()
                .map(PlayerStatistics::getPlayerId)
                .collect(java.util.stream.Collectors.toSet());

        List<PlayerStatistics> newStats = playerIds.stream()
                .filter(playerId -> !existingPlayerIds.contains(playerId))
                .map(playerId -> new PlayerStatistics(playerId, matchId))
                .toList();

        playerStatisticsRepository.saveAll(newStats);
    }
```

- [ ] **Step 4: Run test to verify it passes**
Run: `./gradlew test --tests "*PlayerStatisticsServiceTest*"`
Expected: PASS

- [ ] **Step 5: Commit**
Stage and commit changes if `auto_commit` is enabled.

---

### Task 2: `MatchLineupSyncService` 수정 및 선수 통계 생성 연동

**Files:**
- Modify: [MatchLineupSyncService.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/admin/service/sync/MatchLineupSyncService.java)
- Create: [MatchLineupSyncServiceTest.java](file:///Users/yuseokhyeon/pitchboxd/src/test/java/com/example/pitchboxd/admin/service/sync/MatchLineupSyncServiceTest.java)

- [ ] **Step 1: Write the failing test**
[MatchLineupSyncServiceTest.java](file:///Users/yuseokhyeon/pitchboxd/src/test/java/com/example/pitchboxd/admin/service/sync/MatchLineupSyncServiceTest.java)를 생성하여 라인업 동기화 시 `PlayerStatistics`가 정상적으로 같이 생성되는지 확인하는 테스트를 작성합니다.

```java
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
}
```

- [ ] **Step 2: Run test to verify it fails**
Run: `./gradlew test --tests "*MatchLineupSyncServiceTest*"`
Expected: Fail (라인업만 생성되고, `PlayerStatistics`는 생성되지 않아 `stats` 크기가 0이 됨)

- [ ] **Step 3: Write minimal implementation**
[MatchLineupSyncService.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/admin/service/sync/MatchLineupSyncService.java)를 수정합니다.

* `PlayerStatisticsService`를 주입받도록 필드와 생성자를 업데이트합니다.
  ```java
  private final PlayerStatisticsService playerStatisticsService;
  ```
* `NaverScheduleWrapperExternalResponseHelper` 내부에서 라인업 저장을 완료한 뒤, 선수 ID들을 추출해 `createAllPlayerStatistics`를 호출합니다.
  ```java
      private void NaverScheduleWrapperExternalResponseHelper(String naverGameId, Match match) {
          NaverLineupResponse response = naverSportsClient.getMatchLineup(naverGameId);

          List<MatchLineup> lineups = new ArrayList<>();

          addStaterLineup(lineups, response.getHomeStarters(), match);
          addSubstitutionLineup(lineups, response.getHomeSubstitutions(), match);

          addStaterLineup(lineups, response.getAwayStarters(), match);
          addSubstitutionLineup(lineups, response.getAwaySubstitutions(), match);

          matchLineupService.createAllMatchLineup(lineups);

          List<Long> playerIds = lineups.stream()
                  .map(MatchLineup::getPlayerId)
                  .toList();
          playerStatisticsService.createAllPlayerStatistics(match.getId(), playerIds);
      }
  ```

- [ ] **Step 4: Run test to verify it passes**
Run: `./gradlew test --tests "*MatchLineupSyncServiceTest*"`
Expected: PASS

- [ ] **Step 5: Commit**
Stage and commit changes if `auto_commit` is enabled.
