# Add Fan Distribution and Lineup Player Ratings to Match Stats Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 경기 상세 통계 조회 API(`GET /api/v1/matches/{matchId}/detail/stats`)에 홈/원정팀 라인업 선수들의 평균 평점 및 경기 리뷰에 참여한 사용자들의 팬 타입 분포(`homeCount`, `awayCount`, `neutralCount`) 데이터를 추가합니다.

**Architecture:**
* `MatchReviewRepository`에 팬 타입(`FanType`)별 경기 리뷰 개수를 그룹 연산하는 JPQL 쿼리를 추가합니다.
* `MatchDetailStatsResponse` 레코드(DTO)에 `homePlayerAverage`, `awayPlayerAverage`, `homeCount`, `awayCount`, `neutralCount` 필드를 추가합니다.
* `MatchDetailFacadeService`에서 경기 라인업 데이터를 조회해 홈/원정 선수들을 분류하고, 리뷰가 작성된 선수들의 평점 평균을 계산하여 DTO에 매핑합니다.
* `MatchReviewRepository`의 그룹 쿼리를 통해 홈/원정/중립 팬별 리뷰 참여 건수를 가져와 DTO에 매핑합니다.

**Tech Stack:** Java, Spring Boot, Spring Data JPA

---

### Task 1: `MatchReviewRepository`에 팬 타입 분포 집계 쿼리 추가

**Files:**
- Modify: [MatchReviewRepository.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/match/matchReview/infrastructure/MatchReviewRepository.java)

- [ ] **Step 1: Declare the repository query method**
[MatchReviewRepository.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/match/matchReview/infrastructure/MatchReviewRepository.java)에 아래 메소드를 추가합니다.
```java
    @Query("select r.fanType as fanType, count(r) as count from MatchReview r where r.matchId = :matchId group by r.fanType")
    List<Object[]> countFanTypeDistributionByMatchId(@Param("matchId") Long matchId);
```

- [ ] **Step 2: Verify Compilation**
Run: `./gradlew compileJava`
Expected: SUCCESS

- [ ] **Step 3: Commit (if auto_commit enabled)**
Check `.agent/config.yml` for `auto_commit` setting. If `false`, skip.

---

### Task 2: DTO 변경 및 컴파일 오류 수정

**Files:**
- Modify: [MatchDetailStatsResponse.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/matchDetail/dto/response/MatchDetailStatsResponse.java)
- Modify: [MatchDetailFacadeServiceTest.java](file:///Users/yuseokhyeon/pitchboxd/src/test/java/com/example/pitchboxd/matchDetail/service/MatchDetailFacadeServiceTest.java)
- Modify: [MatchDetailControllerTest.java](file:///Users/yuseokhyeon/pitchboxd/src/test/java/com/example/pitchboxd/matchDetail/presentation/MatchDetailControllerTest.java)

- [ ] **Step 1: Modify `MatchDetailStatsResponse` Record**
[MatchDetailStatsResponse.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/matchDetail/dto/response/MatchDetailStatsResponse.java) 파일을 아래 내용으로 전체 덮어쓰기하거나 수정합니다.
```java
package com.example.pitchboxd.matchDetail.dto.response;

import java.util.List;
import java.util.Map;

public record MatchDetailStatsResponse(
        Double totalAverage,
        Double homeAverage,
        Double awayAverage,
        Map<Integer, Long> distributionMap,
        MatchHighlightsResponse highlights,
        Double homePlayerAverage,
        Double awayPlayerAverage,
        Long homeCount,
        Long awayCount,
        Long neutralCount
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

- [ ] **Step 2: Fix compilation in existing tests**
기존의 `MatchDetailFacadeServiceTest` 및 `MatchDetailControllerTest`에서 `MatchDetailStatsResponse` 생성이나 단언문 등 수정에 따른 오류가 발생할 수 있습니다. 
우선 `./gradlew compileTestJava`를 돌려 발생하는 컴파일 에러를 확인합니다.

- [ ] **Step 3: Resolve compilation errors**
`MatchDetailFacadeServiceTest.java` 및 `MatchDetailControllerTest.java` 내의 기존 통계 관련 단언문이나 목업 데이터를 생성자 파라미터 변화에 맞춰 임시 더미 값으로 보정해 줍니다.

---

### Task 3: `MatchDetailFacadeService` 통계 가공 로직 구현 및 단위 테스트 추가

**Files:**
- Modify: [MatchDetailFacadeService.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/matchDetail/service/MatchDetailFacadeService.java)
- Modify: [MatchDetailFacadeServiceTest.java](file:///Users/yuseokhyeon/pitchboxd/src/test/java/com/example/pitchboxd/matchDetail/service/MatchDetailFacadeServiceTest.java)

- [ ] **Step 1: Write a failing unit test**
[MatchDetailFacadeServiceTest.java](file:///Users/yuseokhyeon/pitchboxd/src/test/java/com/example/pitchboxd/matchDetail/service/MatchDetailFacadeServiceTest.java)에 라인업 선수 평점과 팬 리뷰 분포 조회를 검증하는 테스트 케이스 `경기의_상세_통계_조회_시_라인업_선수_평점_평균과_팬_분포가_정확히_반환된다`를 작성합니다.
```java
    @Test
    void 경기의_상세_통계_조회_시_라인업_선수_평점_평균과_팬_분포가_정확히_반환된다() {
        // given
        Player homePlayer = playerRepository.save(new Player(homeTeam.getId(), "홈선수", "p1"));
        Player awayPlayer = playerRepository.save(new Player(awayTeam.getId(), "원정선수", "p2"));

        matchLineupRepository.save(new MatchLineup(match.getId(), homePlayer.getId(), 7, ParticipationStatus.STARTER));
        matchLineupRepository.save(new MatchLineup(match.getId(), awayPlayer.getId(), 9, ParticipationStatus.STARTER));

        // 선수 통점 정보 셋팅
        PlayerStatistics homeStat = new PlayerStatistics(homePlayer.getId(), match.getId());
        homeStat.addNewReview(8); // 4.0
        playerStatisticsRepository.save(homeStat);

        PlayerStatistics awayStat = new PlayerStatistics(awayPlayer.getId(), match.getId());
        awayStat.addNewReview(10); // 5.0
        playerStatisticsRepository.save(awayStat);

        // 경기 리뷰 생성 (팬 분포 확인용)
        User user1 = userRepository.save(new User("유저1", "u1@test.com", "pass"));
        User user2 = userRepository.save(new User("유저2", "u2@test.com", "pass"));
        User user3 = userRepository.save(new User("유저3", "u3@test.com", "pass"));

        matchReviewRepository.save(new MatchReview(match.getId(), user1.getId(), 8, "좋음", FanType.HOME));
        matchReviewRepository.save(new MatchReview(match.getId(), user2.getId(), 7, "보통", FanType.AWAY));
        matchReviewRepository.save(new MatchReview(match.getId(), user3.getId(), 9, "훌륭", FanType.NEUTRAL));

        // when
        MatchDetailStatsResponse result = matchDetailFacadeService.getMatchStatsData(match.getId());

        // then
        assertAll(
                () -> assertThat(result.homePlayerAverage()).isEqualTo(4.0),
                () -> assertThat(result.awayPlayerAverage()).isEqualTo(5.0),
                () -> assertThat(result.homeCount()).isEqualTo(1L),
                () -> assertThat(result.awayCount()).isEqualTo(1L),
                () -> assertThat(result.neutralCount()).isEqualTo(1L)
        );
    }
```

- [ ] **Step 2: Run test to verify it fails**
Run: `./gradlew test --tests "*MatchDetailFacadeServiceTest.경기의_상세_통계_조회_시_라인업_선수_평점_평균과_팬_분포가_정확히_반환된다*"`
Expected: FAIL

- [ ] **Step 3: Modify `MatchDetailFacadeService.java` implementation**
`getMatchStatsData` 메소드 구현에 `matchQueryService` 주입 및 홈/원정 선수 평점 평균 계산, 그리고 팬 분포 쿼리를 연동합니다.
```java
    public MatchDetailStatsResponse getMatchStatsData(Long matchId) {
        MatchDetailStaticModel matchDetail = matchQueryService.findMatchStaticDetailById(matchId);
        List<LineupPlayerModel> lineups = matchLineupQueryService.findLineupAndPlayedPlayers(matchId);
        List<PlayerStatistics> playerStats = playerStatisticsRepository.findAllByMatchId(matchId);

        MatchStatistics matchStats = matchStatisticsRepository.findByMatchId(matchId)
                .orElse(new MatchStatistics(matchId));

        // 1. 경기 별점 분포도
        List<Object[]> rawDistribution = matchReviewRepository.countPointDistributionByMatchId(matchId);
        Map<Integer, Long> distributionMap = new HashMap<>();
        for (int i = 0; i <= 10; i++) {
            distributionMap.put(i, 0L);
        }
        for (Object[] row : rawDistribution) {
            Integer point = (Integer) row[0];
            Long count = (Long) row[1];
            if (point != null && count != null && point >= 0 && point <= 10) {
                distributionMap.put(point, count);
            }
        }

        // 2. MOM 및 TOP3 선수 선정
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
                        ps.playerId(),
                        playerNamesMap.getOrDefault(ps.playerId(), "Unknown Player"),
                        ps.getAverageRating()
                ))
                .toList();

        // 3. 라인업에 들어간 홈/원정 선수 평점 평균 계산
        Map<Long, Double> playerRatingsMap = playerStats.stream()
                .filter(ps -> ps.getReviewCount() > 0)
                .collect(Collectors.toMap(
                        PlayerStatistics::getPlayerId,
                        PlayerStatistics::getAverageRating,
                        (existing, replacement) -> existing
                ));

        double homePlayerAverage = lineups.stream()
                .filter(l -> l.teamId().equals(matchDetail.homeTeamId()))
                .mapToDouble(l -> playerRatingsMap.getOrDefault(l.playerId(), 0.0))
                .filter(rating -> rating > 0.0)
                .average()
                .orElse(0.0);

        double awayPlayerAverage = lineups.stream()
                .filter(l -> l.teamId().equals(matchDetail.awayTeamId()))
                .mapToDouble(l -> playerRatingsMap.getOrDefault(l.playerId(), 0.0))
                .filter(rating -> rating > 0.0)
                .average()
                .orElse(0.0);

        // 4. 팬 타입 분포 집계
        List<Object[]> rawFanTypeDistribution = matchReviewRepository.countFanTypeDistributionByMatchId(matchId);
        long homeCount = 0L;
        long awayCount = 0L;
        long neutralCount = 0L;
        for (Object[] row : rawFanTypeDistribution) {
            FanType fanType = (FanType) row[0];
            Long count = (Long) row[1];
            if (fanType != null && count != null) {
                if (fanType == FanType.HOME) homeCount = count;
                else if (fanType == FanType.AWAY) awayCount = count;
                else if (fanType == FanType.NEUTRAL) neutralCount = count;
            }
        }

        return new MatchDetailStatsResponse(
                matchStats.getTotalAverage(),
                matchStats.getHomeAverage(),
                matchStats.getAwayAverage(),
                distributionMap,
                new MatchDetailStatsResponse.MatchHighlightsResponse(mom, top3),
                homePlayerAverage,
                awayPlayerAverage,
                homeCount,
                awayCount,
                neutralCount
        );
    }
```

- [ ] **Step 4: Run test to verify it passes**
Run: `./gradlew test --tests "*MatchDetailFacadeServiceTest.경기의_상세_통계_조회_시_라인업_선수_평점_평균과_팬_분포가_정확히_반환된다*"`
Expected: PASS

---

### Task 4: 통합 테스트 작성 및 전체 통과 검증

**Files:**
- Modify: [MatchDetailControllerTest.java](file:///Users/yuseokhyeon/pitchboxd/src/test/java/com/example/pitchboxd/matchDetail/presentation/MatchDetailControllerTest.java)

- [ ] **Step 1: Write integration test case**
[MatchDetailControllerTest.java](file:///Users/yuseokhyeon/pitchboxd/src/test/java/com/example/pitchboxd/matchDetail/presentation/MatchDetailControllerTest.java)에 선수 평점 평균 및 팬 분포가 포함된 상태로 API 응답이 오는지 검증하는 `경기_상세_통계_데이터를_조회할_때_선수평점과_팬분포를_포함한다` 테스트 케이스를 추가합니다.
```java
    @Test
    void 경기_상세_통계_데이터를_조회할_때_선수평점과_팬분포를_포함한다() {
        // given
        Player homePlayer = playerRepository.findAll().get(0);
        PlayerStatistics homeStat = new PlayerStatistics(homePlayer.getId(), match.getId());
        homeStat.addNewReview(8); // 4.0
        playerStatisticsRepository.save(homeStat);

        User author = userRepository.save(new User("팬작성자", "fan@test.com", "pass"));
        matchReviewRepository.save(new MatchReview(match.getId(), author.getId(), 9, "경기 후기", FanType.HOME));

        // when
        MatchDetailStatsResponse statsResponse = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/matches/{matchId}/detail/stats", match.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MatchDetailStatsResponse.class);

        // then
        assertAll(
                () -> assertThat(statsResponse.homePlayerAverage()).isEqualTo(4.0),
                () -> assertThat(statsResponse.awayPlayerAverage()).isEqualTo(0.0),
                () -> assertThat(statsResponse.homeCount()).isEqualTo(1L),
                () -> assertThat(statsResponse.awayCount()).isEqualTo(0L),
                () -> assertThat(statsResponse.neutralCount()).isEqualTo(0L)
        );
    }
```

- [ ] **Step 2: Run test to verify it passes**
Run: `./gradlew test --tests "*MatchDetailControllerTest*"`
Expected: PASS

- [ ] **Step 3: Run all test suite**
Run: `./gradlew test`
Expected: 모든 테스트 PASS (0 failures)
