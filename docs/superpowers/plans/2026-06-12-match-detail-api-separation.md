# 경기 상세 정보 API 분리 구현 계획서 (Result & Stats)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 경기 상세 조회 시, 절대 변하지 않는 경기 기본 정보/라인업(정적 데이터)과 자주 변하는 실시간 평점/별점 통계(동적 데이터)가 한 API에 묶여있던 것을 `/detail/result`와 `/detail/stats` API로 분리하여 캐싱 도입이 용이한 구조를 만듭니다.

**Architecture:** 기존 `MatchDetailResponse` DTO를 삭제하고, 정적 정보용 `MatchDetailResultResponse`와 통계용 `MatchDetailStatsResponse`를 새로 정의합니다. `MatchDetailFacadeService` 내의 하나의 데이터 조회 메서드를 2개로 분할하여 독립적인 책임을 맡게 하고, `MatchDetailController`에서도 기존 엔드포인트를 폐기하고 2개의 신규 엔드포인트로 노출합니다.

**Tech Stack:** Java 21, Spring Boot 3.5, Spring Data JPA, QueryDSL, JUnit 5

---

### Task 1: DTO 신설 및 기존 DTO 제거

**Files:**
- Delete: `src/main/java/com/example/pitchboxd/matchDetail/dto/response/MatchDetailResponse.java`
- Create: `src/main/java/com/example/pitchboxd/matchDetail/dto/response/MatchDetailResultResponse.java`
- Create: `src/main/java/com/example/pitchboxd/matchDetail/dto/response/MatchDetailStatsResponse.java`

- [ ] **Step 1: 기존 MatchDetailResponse.java 파일 제거**
  (기존 사용처들인 Controller, Service, Test 코드는 이후 작업에서 순차적으로 대체 수정할 예정입니다.)

- [ ] **Step 2: MatchDetailResultResponse.java DTO 생성**
  코드 내용:
  ```java
  package com.example.pitchboxd.matchDetail.dto.response;

  import java.time.LocalDateTime;

  public record MatchDetailResultResponse(
          String seasonName,
          String round,
          LocalDateTime startTime,
          String location,
          String homeTeamName,
          String awayTeamName,
          Integer homeScore,
          Integer awayScore,
          LineupResponses homeLineups,
          LineupResponses awayLineups
  ) {}
  ```

- [ ] **Step 3: MatchDetailStatsResponse.java DTO 생성**
  코드 내용:
  ```java
  package com.example.pitchboxd.matchDetail.dto.response;

  import java.util.List;
  import java.util.Map;

  public record MatchDetailStatsResponse(
          Double totalAverage,
          Double homeAverage,
          Double awayAverage,
          Map<Integer, Long> distributionMap,
          MatchHighlightsResponse highlights
  ) {
      public record HighlightPlayerResponse(
              Long playerId,
              String name,
              Double averageRating
      ) {}

      public record MatchHighlightsResponse(
              HighlightPlayerResponse mom,
              List<HighlightPlayerResponse> top3
      ) {}
  }
  ```

- [ ] **Step 4: Commit (설정에 따라 적용)**
  Check `.agent/config.yml` for `auto_commit` setting.
  If `auto_commit: false`: skip commit and staging. Print: "Skipping commit (auto_commit: false)."

---

### Task 2: MatchDetailFacadeService 메서드 분리 구현

**Files:**
- Modify: `src/main/java/com/example/pitchboxd/matchDetail/service/MatchDetailFacadeService.java`

- [ ] **Step 1: MatchDetailFacadeService.java 수정**
  * `import com.example.pitchboxd.matchDetail.dto.response.MatchDetailResponse;` 삭제
  * `import com.example.pitchboxd.matchDetail.dto.response.MatchDetailResultResponse;` 추가
  * `import com.example.pitchboxd.matchDetail.dto.response.MatchDetailStatsResponse;` 추가
  * 기존 `public MatchDetailResponse getMatchStaticData(Long matchId)` 메서드 삭제
  * 신규 메서드 `getMatchResultData(Long matchId)`와 `getMatchStatsData(Long matchId)` 정의

  **교체될 코드 구현체:**
  ```java
      public MatchDetailResultResponse getMatchResultData(Long matchId) {
          MatchDetailStaticModel matchDetail = matchQueryService.findMatchStaticDetailById(matchId);
          List<LineupPlayerModel> lineups = matchLineupQueryService.findLineupAndPlayedPlayers(matchId);

          List<PlayerStatistics> playerStats = playerStatisticsRepository.findAllByMatchId(matchId);
          Map<Long, Double> playerRatingsMap = playerStats.stream()
                  .collect(Collectors.toMap(
                          PlayerStatistics::getPlayerId,
                          PlayerStatistics::getAverageRating,
                          (existing, replacement) -> existing
                  ));

          List<LineupResponse> homeLineupResponses = lineups.stream()
                  .filter(l -> l.teamId().equals(matchDetail.homeTeamId()))
                  .map(l -> LineupResponse.of(l, playerRatingsMap.getOrDefault(l.playerId(), 0.0)))
                  .toList();

          List<LineupResponse> awayLineupResponses = lineups.stream()
                  .filter(l -> l.teamId().equals(matchDetail.awayTeamId()))
                  .map(l -> LineupResponse.of(l, playerRatingsMap.getOrDefault(l.playerId(), 0.0)))
                  .toList();

          return new MatchDetailResultResponse(
                  matchDetail.seasonName(),
                  matchDetail.round(),
                  matchDetail.startTime(),
                  matchDetail.location(),
                  matchDetail.homeTeamName(),
                  matchDetail.awayTeamName(),
                  matchDetail.homeScore(),
                  matchDetail.awayScore(),
                  new LineupResponses(homeLineupResponses),
                  new LineupResponses(awayLineupResponses)
          );
      }

      public MatchDetailStatsResponse getMatchStatsData(Long matchId) {
          List<LineupPlayerModel> lineups = matchLineupQueryService.findLineupAndPlayedPlayers(matchId);
          List<PlayerStatistics> playerStats = playerStatisticsRepository.findAllByMatchId(matchId);

          MatchStatistics matchStats = matchStatisticsRepository.findByMatchId(matchId)
                  .orElse(new MatchStatistics(matchId));

          List<Object[]> rawDistribution = matchReviewRepository.countPointDistributionByMatchId(matchId);
          Map<Integer, Long> distributionMap = new HashMap<>();
          for (int i = 0; i <= 10; i++) {
              distributionMap.put(i, 0L);
          }
          for (Object[] row : rawDistribution) {
              Integer point = (Integer) row[0];
              Long count = (Long) row[1];
              if (point >= 0 && point <= 10) {
                  distributionMap.put(point, count);
              }
          }

          List<PlayerStatistics> sortedStats = playerStats.stream()
                  .filter(ps -> ps.getReviewCount() > 0)
                  .sorted(Comparator.comparingDouble(PlayerStatistics::getAverageRating).reversed()
                          .thenComparing(PlayerStatistics::getReviewCount, Comparator.reverseOrder())
                          .thenComparing(PlayerStatistics::getPlayerId, Comparator.reverseOrder()))
                  .toList();

          Map<Long, String> playerNamesMap = lineups.stream()
                  .collect(Collectors.toMap(
                          LineupPlayerModel::playerId,
                          LineupPlayerModel::playerName,
                          (existing, replacement) -> existing
                  ));

          MatchDetailStatsResponse.HighlightPlayerResponse mom = null;
          if (!sortedStats.isEmpty()) {
              PlayerStatistics momStat = sortedStats.get(0);
              String momName = playerNamesMap.getOrDefault(momStat.getPlayerId(), "Unknown Player");
              mom = new MatchDetailStatsResponse.HighlightPlayerResponse(momStat.getPlayerId(), momName, momStat.getAverageRating());
          }

          List<MatchDetailStatsResponse.HighlightPlayerResponse> top3 = sortedStats.stream()
                  .limit(3)
                  .map(ps -> new MatchDetailStatsResponse.HighlightPlayerResponse(
                          ps.getPlayerId(),
                          playerNamesMap.getOrDefault(ps.getPlayerId(), "Unknown Player"),
                          ps.getAverageRating()
                  ))
                  .toList();

          return new MatchDetailStatsResponse(
                  matchStats.getTotalAverage(),
                  matchStats.getHomeAverage(),
                  matchStats.getAwayAverage(),
                  distributionMap,
                  new MatchDetailStatsResponse.MatchHighlightsResponse(mom, top3)
          );
      }
  ```

- [ ] **Step 2: 컴파일 실행을 통해 서비스 클래스 에러 검증**
  (Controller와 Test가 아직 수정되지 않아 전역 컴파일은 에러가 나므로 FacadeService의 구문 자체 위주로 임포트 문제를 검증합니다.)

- [ ] **Step 3: Commit (설정에 따라 적용)**
  Check `.agent/config.yml` for `auto_commit` setting.
  If `auto_commit: false`: skip commit and staging. Print: "Skipping commit (auto_commit: false)."

---

### Task 3: MatchDetailController 엔드포인트 수정 및 신설

**Files:**
- Modify: `src/main/java/com/example/pitchboxd/matchDetail/presentation/MatchDetailController.java`

- [ ] **Step 1: MatchDetailController.java 수정**
  * `import com.example.pitchboxd.matchDetail.dto.response.MatchDetailResponse;` 삭제
  * `import com.example.pitchboxd.matchDetail.dto.response.MatchDetailResultResponse;` 추가
  * `import com.example.pitchboxd.matchDetail.dto.response.MatchDetailStatsResponse;` 추가
  * 기존 `public ResponseEntity<SuccessResponse<MatchDetailResponse>> getMatchStaticData(@PathVariable Long matchId)` 엔드포인트 제거
  * `/detail/result` 및 `/detail/stats` 엔드포인트를 노출하는 2개의 메서드 신설

  **교체될 코드 구현체:**
  ```java
      @Operation(summary = "경기 결과 및 라인업 데이터", description = "경기 상세 페이지의 결과 점수 및 양팀 라인업 데이터를 가져옵니다.")
      @GetMapping("{matchId}/detail/result")
      public ResponseEntity<SuccessResponse<MatchDetailResultResponse>> getMatchResultData(@PathVariable Long matchId) {
          MatchDetailResultResponse response = matchDetailFacadeService.getMatchResultData(matchId);
          HttpStatus status = HttpStatus.OK;

          return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
      }

      @Operation(summary = "경기 평점 통계 및 하이라이트 데이터", description = "경기 상세 페이지의 실시간 평점 평균, 별점 분포도, MOM 및 Top3 플레이어 정보를 가져옵니다.")
      @GetMapping("{matchId}/detail/stats")
      public ResponseEntity<SuccessResponse<MatchDetailStatsResponse>> getMatchStatsData(@PathVariable Long matchId) {
          MatchDetailStatsResponse response = matchDetailFacadeService.getMatchStatsData(matchId);
          HttpStatus status = HttpStatus.OK;

          return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
      }
  ```

- [ ] **Step 2: Commit (설정에 따라 적용)**
  Check `.agent/config.yml` for `auto_commit` setting.
  If `auto_commit: false`: skip commit and staging. Print: "Skipping commit (auto_commit: false)."

---

### Task 4: 통합 테스트 코드 수정 및 실행 검증

**Files:**
- Modify: `src/test/java/com/example/pitchboxd/matchDetail/service/MatchDetailFacadeServiceTest.java`

- [ ] **Step 1: 테스트 파일의 임포트 및 기존 통합 테스트 메서드 전면 교체**
  * `import com.example.pitchboxd.matchDetail.dto.response.MatchDetailResponse;` 삭제
  * `import com.example.pitchboxd.matchDetail.dto.response.MatchDetailResultResponse;` 추가
  * `import com.example.pitchboxd.matchDetail.dto.response.MatchDetailStatsResponse;` 추가
  * 기존 `경기의_정적_데이터_조회_시_MOM과_Top3_선수가_정렬_조건에_맞게_반환된다` 삭제
  * 기존 `경기의_정적_데이터_조회_시_평균평점들과_평점분포도가_올바르게_계산된다` 삭제
  * 신규 테스트 3개 구현:
    1. `경기의_결과와_라인업_데이터를_정확히_조회한다`
    2. `경기의_통계_데이터_조회_시_MOM과_Top3_선수가_정렬_조건에_맞게_반환된다`
    3. `경기의_통계_데이터_조회_시_평균평점들과_평점분포도가_올바르게_계산된다`

  **구현할 테스트 코드 블록:**
  ```java
      @Test
      void 경기의_결과와_라인업_데이터를_정확히_조회한다() {
          // given
          Player player = playerRepository.save(new Player(homeTeam.getId(), "테스트선수", "pT"));
          matchLineupRepository.save(new MatchLineup(match.getId(), player.getId(), 1, ParticipationStatus.STARTER));

          // when
          MatchDetailResultResponse result = matchDetailFacadeService.getMatchResultData(match.getId());

          // then
          assertAll(
                  () -> assertThat(result.seasonName()).isEqualTo("2026"),
                  () -> assertThat(result.round()).isEqualTo("1"),
                  () -> assertThat(result.location()).isEqualTo("서울"),
                  () -> assertThat(result.homeTeamName()).isEqualTo("FC서울"),
                  () -> assertThat(result.awayTeamName()).isEqualTo("울산"),
                  () -> assertThat(result.homeLineups().lineups()).hasSize(1),
                  () -> assertThat(result.homeLineups().lineups().get(0).playerName()).isEqualTo("테스트선수")
          );
      }

      @Test
      void 경기의_통계_데이터_조회_시_MOM과_Top3_선수가_정렬_조건에_맞게_반환된다() {
          // given
          Player player1 = playerRepository.save(new Player(homeTeam.getId(), "선수1", "p1"));
          Player player2 = playerRepository.save(new Player(homeTeam.getId(), "선수2", "p2"));
          Player player3 = playerRepository.save(new Player(homeTeam.getId(), "선수3", "p3"));
          Player player4 = playerRepository.save(new Player(homeTeam.getId(), "선수4", "p4"));

          matchLineupRepository.save(new MatchLineup(match.getId(), player1.getId(), 1, ParticipationStatus.STARTER));
          matchLineupRepository.save(new MatchLineup(match.getId(), player2.getId(), 2, ParticipationStatus.STARTER));
          matchLineupRepository.save(new MatchLineup(match.getId(), player3.getId(), 3, ParticipationStatus.STARTER));
          matchLineupRepository.save(new MatchLineup(match.getId(), player4.getId(), 4, ParticipationStatus.STARTER));

          PlayerStatistics stat1 = new PlayerStatistics(player1.getId(), match.getId());
          stat1.addNewReview(9); // 평점 = 4.5
          playerStatisticsRepository.save(stat1);

          PlayerStatistics stat2 = new PlayerStatistics(player2.getId(), match.getId());
          stat2.addNewReview(9);
          stat2.addNewReview(9); // 평점 = 4.5, 투표 = 2
          playerStatisticsRepository.save(stat2);

          PlayerStatistics stat3 = new PlayerStatistics(player3.getId(), match.getId());
          stat3.addNewReview(8); // 평점 = 4.0
          playerStatisticsRepository.save(stat3);

          PlayerStatistics stat4 = new PlayerStatistics(player4.getId(), match.getId());
          stat4.addNewReview(9);
          stat4.addNewReview(9); // 평점 = 4.5, 투표 = 2 (player4.id > player2.id)
          playerStatisticsRepository.save(stat4);

          // when
          MatchDetailStatsResponse result = matchDetailFacadeService.getMatchStatsData(match.getId());

          // then
          assertThat(result.highlights().mom().playerId()).isEqualTo(player4.getId());
          assertThat(result.highlights().mom().name()).isEqualTo("선수4");
          assertThat(result.highlights().mom().averageRating()).isEqualTo(4.5);

          assertThat(result.highlights().top3()).hasSize(3);
          assertThat(result.highlights().top3().get(0).playerId()).isEqualTo(player4.getId());
          assertThat(result.highlights().top3().get(1).playerId()).isEqualTo(player2.getId());
          assertThat(result.highlights().top3().get(2).playerId()).isEqualTo(player1.getId());
      }

      @Test
      void 경기의_통계_데이터_조회_시_평균평점들과_평점분포도가_올바르게_계산된다() {
          // given
          MatchStatistics matchStats = new MatchStatistics(match.getId());
          matchStats.addNewReview(9, FanType.HOME);
          matchStats.addNewReview(9, FanType.HOME);
          matchStats.addNewReview(8, FanType.AWAY);
          matchStats.addNewReview(6, FanType.NEUTRAL);
          matchStats.addNewReview(8, FanType.NEUTRAL);
          matchStatisticsRepository.save(matchStats);

          User user = userRepository.save(new User("테스터", "test@test.com", "pass"));
          matchReviewRepository.save(new MatchReview(match.getId(), user.getId(), 8, "좋은경기", FanType.NEUTRAL));
          matchReviewRepository.save(new MatchReview(match.getId(), user.getId(), 8, "재밌네요", FanType.HOME));
          matchReviewRepository.save(new MatchReview(match.getId(), user.getId(), 5, "그저그럼", FanType.AWAY));

          // when
          MatchDetailStatsResponse result = matchDetailFacadeService.getMatchStatsData(match.getId());

          // then
          assertThat(result.totalAverage()).isEqualTo(4.0);
          assertThat(result.homeAverage()).isEqualTo(4.5);
          assertThat(result.awayAverage()).isEqualTo(4.0);

          Map<Integer, Long> distribution = result.distributionMap();
          assertThat(distribution.get(8)).isEqualTo(2L);
          assertThat(distribution.get(5)).isEqualTo(1L);
      }
  ```

- [ ] **Step 2: 테스트 전체 컴파일 및 테스트 구동 수행**
  Run: `./gradlew test --tests com.example.pitchboxd.matchDetail.service.MatchDetailFacadeServiceTest`
  Expected: BUILD SUCCESSFUL (수정 및 신설된 모든 테스트 통과)

- [ ] **Step 3: Commit (설정에 따라 적용)**
  Check `.agent/config.yml` for `auto_commit` setting.
  If `auto_commit: false`: skip commit and staging. Print: "Skipping commit (auto_commit: false)."
