# Resolve Naver ID Mismatch by Name Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 라인업 동기화 시 선수의 `naver_id`가 매칭되지 않을 경우, 동명이인 방지를 위해 선수의 소속 팀 ID와 이름을 활용하여 대체 매칭(Fallback)하는 로직을 추가합니다.

**Architecture:** 
1. `PlayerRepository`에 `Optional<Player> findByTeamIdAndName(Long teamId, String name)` 쿼리 메소드를 추가합니다.
2. `MatchLineupSyncService`에서 선발/교체 라인업을 등록할 때 기존의 `match.getHomeTeamId()` 및 `match.getAwayTeamId()`를 `addStaterLineup` 및 `addSubstitutionLineup` 메소드의 파라미터로 넘깁니다.
3. `playerRepository.findByNaverId(node.playerId())` 결과가 비어있을 경우 `playerRepository.findByTeamIdAndName(teamId, node.name())`을 이용해 선수를 2차 조회하도록 Fallback 로직을 적용합니다.

**Tech Stack:** Java, Spring Boot, Spring Data JPA, JUnit 5

---

### Task 1: `PlayerRepository`에 쿼리 메소드 추가

**Files:**
- Modify: [PlayerRepository.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/player/infrastructure/PlayerRepository.java)

- [ ] **Step 1: Write the failing test**
[PlayerRepositoryTest.java]가 존재한다면 거기에 테스트를 작성합니다. (우선 `PlayerRepositoryTest.java`가 있는지 확인한 후 없다면 신규 작성하거나 직접 `PlayerRepository`에 메소드만 작성한 후 Task 2 테스트에서 함께 검증합니다. 여기서는 `PlayerRepository`에 메소드를 추가하므로 바로 메소드를 선언합니다.)

- [ ] **Step 2: Add method signature**
[PlayerRepository.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/player/infrastructure/PlayerRepository.java)에 아래 쿼리 메소드를 추가합니다.
```java
Optional<Player> findByTeamIdAndName(Long teamId, String name);
```

- [ ] **Step 3: Verify Compilation**
Run: `./gradlew compileJava`
Expected: SUCCESS

- [ ] **Step 4: Commit**
Stage and commit changes if `auto_commit` is enabled.

---

### Task 2: `MatchLineupSyncService` 수정 및 테스트 검증

**Files:**
- Modify: [MatchLineupSyncService.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/admin/service/sync/MatchLineupSyncService.java)
- Modify: [MatchLineupSyncServiceTest.java](file:///Users/yuseokhyeon/pitchboxd/src/test/java/com/example/pitchboxd/admin/service/sync/MatchLineupSyncServiceTest.java)

- [ ] **Step 1: Write the failing test**
[MatchLineupSyncServiceTest.java](file:///Users/yuseokhyeon/pitchboxd/src/test/java/com/example/pitchboxd/admin/service/sync/MatchLineupSyncServiceTest.java)에 선수의 `naver_id`가 일치하지 않더라도 소속 팀 ID와 이름이 일치하면 라인업에 정상 등록되는지 확인하는 테스트 `syncLineup_fallbackToName_whenNaverIdMismatched`를 추가합니다.

```java
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
```

- [ ] **Step 2: Run test to verify it fails**
Run: `./gradlew test --tests "*MatchLineupSyncServiceTest.syncLineup_fallbackToName_whenNaverIdMismatched*"`
Expected: Fail (선수를 찾지 못해 라인업 및 통계 데이터 크기가 0이 됨)

- [ ] **Step 3: Write minimal implementation**
[MatchLineupSyncService.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/admin/service/sync/MatchLineupSyncService.java)를 다음과 같이 수정합니다.

* `NaverScheduleWrapperExternalResponseHelper` 내에서 `addStaterLineup` 및 `addSubstitutionLineup` 호출 시 `teamId`를 넘겨주도록 변경합니다.
  ```java
        addStaterLineup(lineups, response.getHomeStarters(), match, match.getHomeTeamId());
        addSubstitutionLineup(lineups, response.getHomeSubstitutions(), match, match.getHomeTeamId());

        addStaterLineup(lineups, response.getAwayStarters(), match, match.getAwayTeamId());
        addSubstitutionLineup(lineups, response.getAwaySubstitutions(), match, match.getAwayTeamId());
  ```
* `addStaterLineup` 및 `addSubstitutionLineup` 메소드의 시그니처를 수정하고, `naverId` 조회 후 `teamId`와 `name`으로 대체 조회(Fallback)하는 로직을 반영합니다.
  ```java
      private void addStaterLineup(List<MatchLineup> lineups, List<NaverPlayerNode> nodes, Match match, Long teamId) {
          for (NaverPlayerNode node : nodes) {
              playerRepository.findByNaverId(node.playerId())
                  .or(() -> playerRepository.findByTeamIdAndName(teamId, node.name()))
                  .ifPresentOrElse(
                      player -> lineups.add(createLineup(match.getId(), player.getId(), node, ParticipationStatus.STARTER)),
                      () -> log.warn("선발 라인업 동기화 제외 - DB에 존재하지 않는 선수입니다. (선수 ID: {}, 이름: {})", node.playerId(), node.name())
                  );
          }
      }

      private void addSubstitutionLineup(List<MatchLineup> lineups, List<NaverPlayerNode> nodes, Match match, Long teamId) {
          for (NaverPlayerNode node : nodes) {
              playerRepository.findByNaverId(node.playerId())
                  .or(() -> playerRepository.findByTeamIdAndName(teamId, node.name()))
                  .ifPresentOrElse(
                      player -> {
                          ParticipationStatus status =
                                  node.changed() ? ParticipationStatus.SUBSTITUTED_IN : ParticipationStatus.BENCH;

                          MatchLineup matchLineup = createLineup(match.getId(), player.getId(), node, status);
                          lineups.add(matchLineup);
                      },
                      () -> log.warn("교체 라인업 동기화 제외 - DB에 존재하지 않는 선수입니다. (선수 ID: {}, 이름: {})", node.playerId(), node.name())
                  );
          }
      }
  ```

- [ ] **Step 4: Run test to verify it passes**
Run: `./gradlew test --tests "*MatchLineupSyncServiceTest*"`
Expected: PASS

- [ ] **Step 5: Commit**
Stage and commit changes if `auto_commit` is enabled.
