# Remove Neutral Fan Stats Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove neutral fan average rating statistic calculation and DTO fields from the Match Detail Static API.

**Architecture:** Modify the response record `MatchDetailResponse` to exclude `neutralFanAverageRating`, update `MatchDetailFacadeService` to remove neutral fan aggregation, and clean up test assertions in `MatchDetailFacadeServiceTest`.

**Tech Stack:** Java, Spring Boot, JUnit 5, AssertJ

---

### Task 1: DTO Modification

**Files:**
* Modify: `/Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/matchDetail/dto/response/MatchDetailResponse.java`

- [ ] **Step 1: Remove `neutralFanAverageRating` from MatchDetailResponse record**

Modify `MatchDetailResponse.java` as follows:
```java
package com.example.pitchboxd.matchDetail.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record MatchDetailResponse(
        String season,
        String round,
        LocalDateTime dateTime,
        String location,
        String homeTeam,
        String awayTeam,
        Integer homeScore,
        Integer awayScore,
        LineupResponses homeLineups,
        LineupResponses awayLineups,
        Double matchAverageRating,
        Double homeFanAverageRating,
        Double awayFanAverageRating,
        Map<Integer, Long> ratingDistribution,
        MatchHighlightsResponse highlights
) {
    public record MatchHighlightsResponse(
            HighlightPlayerResponse mom,
            List<HighlightPlayerResponse> top3
    ) {}
    
    public record HighlightPlayerResponse(
            Long playerId,
            String name,
            Double averageRating
    ) {}
}
```

- [ ] **Step 2: Verify code compilation**

Run: `./gradlew compileJava`
Expected: SUCCESS (though note that `MatchDetailFacadeService` will fail compiling until Task 2 is complete).

- [ ] **Step 3: Commit (if auto_commit enabled)**

Check `.agent/config.yml` for `auto_commit` setting.

If `auto_commit: true` (default when absent):
```bash
git add src/main/java/com/example/pitchboxd/matchDetail/dto/response/MatchDetailResponse.java
git commit -m "refactor: remove neutralFanAverageRating from MatchDetailResponse DTO"
```
If `auto_commit: false`: skip commit and staging. Print: "Skipping commit (auto_commit: false)."

---

### Task 2: Service Modification

**Files:**
* Modify: `/Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/matchDetail/service/MatchDetailFacadeService.java`

- [ ] **Step 1: Remove neutral stats calculation from getMatchStaticData**

Modify the calculation logic in `MatchDetailFacadeService.java`:
```java
        // Remove these lines:
        // int neutralCount = totalCount - homeCount - awayCount;
        // long neutralSum = totalSum - homeSum - awaySum;
        // double neutralAverage = neutralCount <= 0 ? 0.0 : Math.max(0.0, (neutralSum / (double) neutralCount) / 2.0);
```

Also, modify the `new MatchDetailResponse(...)` instantiation at the end of `getMatchStaticData` to omit `neutralAverage`:
```java
        return new MatchDetailResponse(
                matchDetail.seasonName(),
                matchDetail.round(),
                matchDetail.startTime(),
                matchDetail.location(),
                matchDetail.homeTeamName(),
                matchDetail.awayTeamName(),
                matchDetail.homeScore(),
                matchDetail.awayScore(),
                new LineupResponses(homeLineupResponses),
                new LineupResponses(awayLineupResponses),
                matchStats.getTotalAverage(),
                matchStats.getHomeAverage(),
                matchStats.getAwayAverage(),
                distributionMap,
                new MatchDetailResponse.MatchHighlightsResponse(mom, top3)
        );
```

- [ ] **Step 2: Verify code compilation**

Run: `./gradlew compileJava`
Expected: SUCCESS

- [ ] **Step 3: Commit (if auto_commit enabled)**

Check `.agent/config.yml` for `auto_commit` setting.

If `auto_commit: true` (default when absent):
```bash
git add src/main/java/com/example/pitchboxd/matchDetail/service/MatchDetailFacadeService.java
git commit -m "refactor: remove neutral fan average rating calculation from MatchDetailFacadeService"
```
If `auto_commit: false`: skip commit and staging. Print: "Skipping commit (auto_commit: false)."

---

### Task 3: Test Code Updates

**Files:**
* Modify: `/Users/yuseokhyeon/pitchboxd/src/test/java/com/example/pitchboxd/matchDetail/service/MatchDetailFacadeServiceTest.java`

- [ ] **Step 1: Modify test cases asserting neutral statistics**

Modify `MatchDetailFacadeServiceTest.java` around line 270:
```java
    @Test
    void 경기의_정적_데이터_조회_시_평균평점들과_평점분포도가_올바르게_계산된다() {
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
        MatchDetailResponse result = matchDetailFacadeService.getMatchStaticData(match.getId());

        // then
        // Remove: assertThat(result.neutralFanAverageRating()).isEqualTo(3.5);
        assertThat(result.matchAverageRating()).isEqualTo(4.0);
        assertThat(result.homeFanAverageRating()).isEqualTo(4.5);
        assertThat(result.awayFanAverageRating()).isEqualTo(4.0);

        Map<Integer, Long> distribution = result.ratingDistribution();
        assertThat(distribution.get(8)).isEqualTo(2L);
        assertThat(distribution.get(5)).isEqualTo(1L);
        assertThat(distribution.get(1)).isEqualTo(0L);
        assertThat(distribution.get(10)).isEqualTo(0L);
    }
```

- [ ] **Step 2: Run all tests to verify**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL (All tests pass)

- [ ] **Step 3: Commit (if auto_commit enabled)**

Check `.agent/config.yml` for `auto_commit` setting.

If `auto_commit: true` (default when absent):
```bash
git add src/test/java/com/example/pitchboxd/matchDetail/service/MatchDetailFacadeServiceTest.java
git commit -m "test: update MatchDetailFacadeServiceTest to remove neutral fan assertions"
```
If `auto_commit: false`: skip commit and staging. Print: "Skipping commit (auto_commit: false)."
