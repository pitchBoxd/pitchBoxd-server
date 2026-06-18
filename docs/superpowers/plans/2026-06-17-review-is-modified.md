# Review Modification Status Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Include an `isModified` boolean status in MatchReview and PlayerReview response DTOs indicating whether they have been updated.

**Architecture:** Add `isUpdated()` helper to `PlayerReview` entity. Modify response DTOs to include `isModified`. Update Querydsl projections to select `updatedAt.isNotNull()`. Update service mapper and rewrite tests to assert proper mapping.

**Tech Stack:** Java, Spring Boot, Querydsl, JUnit 5

---

### Task 1: PlayerReview.isUpdated() 추가

**Files:**
- Modify: `src/main/java/com/example/pitchboxd/match/playerReview/domain/PlayerReview.java`
- Test: `src/test/java/com/example/pitchboxd/match/playerReview/domain/PlayerReviewTest.java`

- [ ] **Step 1: Write the failing test**

  Open `src/test/java/com/example/pitchboxd/match/playerReview/domain/PlayerReviewTest.java` and add a test method `isUpdated_ShouldReturnTrue_WhenUpdatedAtIsNotNull` and `isUpdated_ShouldReturnFalse_WhenUpdatedAtIsNull`.

  ```java
  @Test
  void isUpdated_ShouldReturnTrue_WhenUpdatedAtIsNotNull() {
      PlayerReview playerReview = new PlayerReview(1L, 1L, 1L, 8, "Great match!", FanType.HOME);
      playerReview.update("Awesome match!", 9);
      
      assertThat(playerReview.isUpdated()).isTrue();
  }

  @Test
  void isUpdated_ShouldReturnFalse_WhenUpdatedAtIsNull() {
      PlayerReview playerReview = new PlayerReview(1L, 1L, 1L, 8, "Great match!", FanType.HOME);
      
      assertThat(playerReview.isUpdated()).isFalse();
  }
  ```

- [ ] **Step 2: Run test to verify it fails**

  Run: `./gradlew test --tests "com.example.pitchboxd.match.playerReview.domain.PlayerReviewTest"`
  Expected: FAIL (compilation error: cannot find symbol `isUpdated()`)

- [ ] **Step 3: Write minimal implementation**

  Open `src/main/java/com/example/pitchboxd/match/playerReview/domain/PlayerReview.java` and implement `isUpdated()`.

  ```java
  public boolean isUpdated() {
      return updatedAt != null;
  }
  ```

- [ ] **Step 4: Run test to verify it passes**

  Run: `./gradlew test --tests "com.example.pitchboxd.match.playerReview.domain.PlayerReviewTest"`
  Expected: PASS

- [ ] **Step 5: Commit (if auto_commit enabled)**

  Check `.agent/config.yml` for `auto_commit` setting.
  Since `auto_commit` is `false` in `.agent/config.yml`, skip commit and staging. Print: "Skipping commit (auto_commit: false)."

---

### Task 2: DTO 클래스 및 HotReviewSummary 수정

**Files:**
- Modify: `src/main/java/com/example/pitchboxd/matchDetail/dto/response/MatchReviewDetailResponse.java`
- Modify: `src/main/java/com/example/pitchboxd/matchDetail/dto/response/MyMatchReviewResponse.java`
- Modify: `src/main/java/com/example/pitchboxd/matchDetail/dto/response/MatchDetailMatchReviewResponse.java`
- Modify: `src/main/java/com/example/pitchboxd/matchDetail/dto/response/MyPlayerReviewResponse.java`
- Modify: `src/main/java/com/example/pitchboxd/match/matchReview/infrastructure/dto/HotReviewSummary.java`

- [ ] **Step 1: Modify DTOs to include `isModified`**

  Modify records to add `boolean isModified` (or update constructor call inside mapping static methods).

  - In `MatchReviewDetailResponse.java`:
    ```java
    public record MatchReviewDetailResponse(
            Long reviewId,
            Long userId,
            String nickname,
            String profileImage,
            FanType fanType,
            Integer point,
            String content,
            Long likeCount,
            boolean isLiked,
            boolean isOwner,
            LocalDateTime createdAt,
            boolean isModified
    ) {}
    ```

  - In `MyMatchReviewResponse.java`:
    ```java
    public record MyMatchReviewResponse(
            Long reviewId,
            Integer rating,
            String comment,
            boolean isModified
    ) {}
    ```

  - In `MyPlayerReviewResponse.java`:
    ```java
    public record MyPlayerReviewResponse(
            Long playerReviewId,
            Long playerId,
            Integer rating,
            String comment,
            boolean isModified
    ) {}
    ```

  - In `HotReviewSummary.java`:
    ```java
    public record HotReviewSummary(
            Long reviewId,
            Long matchId,
            String authorNickname,
            Long authorId,
            FanType fanType,
            Integer point,
            String content,
            Long likeCount,
            boolean isModified
    ) {}
    ```

  - In `MatchDetailMatchReviewResponse.java`:
    ```java
    public record MatchDetailMatchReviewResponse(
            Long reviewId,
            String authorNickname,
            Long authorId,
            FanType fanType,
            Integer point,
            String content,
            Long likeCount,
            boolean isLiked,
            boolean isModified
    ) {
        public static MatchDetailMatchReviewResponse of(HotReviewSummary hotReview, Boolean isLiked) {
            return new MatchDetailMatchReviewResponse(
                    hotReview.reviewId(),
                    hotReview.authorNickname(),
                    hotReview.authorId(),
                    hotReview.fanType(),
                    hotReview.point(),
                    hotReview.content(),
                    hotReview.likeCount(),
                    isLiked,
                    hotReview.isModified()
            );
        }
    }
    ```

- [ ] **Step 2: Verify compilation fails**

  Run: `./gradlew compileJava`
  Expected: FAIL with compilation errors in `MatchReviewQueryRepository`, `MatchDetailFacadeService`, and tests since they do not match the new constructors.

- [ ] **Step 3: Commit (if auto_commit enabled)**

  Check `.agent/config.yml` for `auto_commit` setting.
  Since `auto_commit` is `false` in `.agent/config.yml`, skip commit and staging. Print: "Skipping commit (auto_commit: false)."

---

### Task 3: Querydsl Repository 수정 (`MatchReviewQueryRepository.java`)

**Files:**
- Modify: `src/main/java/com/example/pitchboxd/match/matchReview/infrastructure/MatchReviewQueryRepository.java`

- [ ] **Step 1: Update projections in `MatchReviewQueryRepository`**

  In `findHotReviewsByMatchId` and `findHotReviewsByMatchIds`, add `matchReview.updatedAt.isNotNull()` to map to `isModified` parameter in `HotReviewSummary`.

  ```java
  // In findHotReviewsByMatchId
  return queryFactory
          .select(Projections.constructor(HotReviewSummary.class,
                  matchReview.id,
                  matchReview.matchId,
                  user.nickname,
                  user.id,
                  matchReview.fanType,
                  matchReview.point,
                  matchReview.content,
                  matchReview.likeCount,
                  matchReview.updatedAt.isNotNull()
          ))
  ```

  ```java
  // In findHotReviewsByMatchIds
  return queryFactory
          .select(Projections.constructor(HotReviewSummary.class,
                  matchReview.id,
                  matchReview.matchId,
                  user.nickname,
                  user.id,
                  matchReview.fanType,
                  matchReview.point,
                  matchReview.content,
                  matchReview.likeCount,
                  matchReview.updatedAt.isNotNull()
          ))
  ```

- [ ] **Step 2: Verify compilation still fails but without errors in `MatchReviewQueryRepository`**

  Run: `./gradlew compileJava`
  Expected: FAIL but errors in `MatchReviewQueryRepository` should be resolved.

- [ ] **Step 3: Commit (if auto_commit enabled)**

  Check `.agent/config.yml` for `auto_commit` setting.
  Since `auto_commit` is `false` in `.agent/config.yml`, skip commit and staging. Print: "Skipping commit (auto_commit: false)."

---

### Task 4: Facade Service 매핑 수정 (`MatchDetailFacadeService.java`)

**Files:**
- Modify: `src/main/java/com/example/pitchboxd/matchDetail/service/MatchDetailFacadeService.java`

- [ ] **Step 1: Update mappings in Facade Service**

  In `getMatchPersonalData`:
  ```java
  MyMatchReviewResponse myMatchReview = matchReviewRepository.findByMatchIdAndUserId(matchId, userId)
          .map(matchReview -> new MyMatchReviewResponse(
                  matchReview.getId(),
                  matchReview.getPoint(),
                  matchReview.getContent(),
                  matchReview.isUpdated()
          ))
          .orElse(null);

  List<MyPlayerReviewResponse> myPlayerReviews = playerReviewRepository.findAllByMatchIdAndUserId(matchId, userId)
          .stream()
          .map(playerReview -> new MyPlayerReviewResponse(
                  playerReview.getId(),
                  playerReview.getPlayerId(),
                  playerReview.getPoint(),
                  playerReview.getContent(),
                  playerReview.isUpdated()
          ))
          .toList();
  ```

  In `getMatchReviews`:
  ```java
  List<MatchReviewDetailResponse> reviewResponses = content.stream()
          .map(r -> {
              User author = authorMap.get(r.getUserId());
              String nickname = author != null ? author.getNickname() : "Unknown";
              String profile = "";
              boolean isLiked = likedStatus.getOrDefault(r.getId(), false);
              boolean isOwner = userId != null && r.isOwner(userId);
              return new MatchReviewDetailResponse(
                      r.getId(),
                      r.getUserId(),
                      nickname,
                      profile,
                      r.getFanType(),
                      r.getPoint(),
                      r.getContent(),
                      r.getLikeCount(),
                      isLiked,
                      isOwner,
                      r.getCreatedAt(),
                      r.isUpdated()
              );
          })
          .toList();
  ```

- [ ] **Step 2: Verify production code compiles successfully**

  Run: `./gradlew compileJava`
  Expected: BUILD SUCCESSFUL (Note: Test compilation may still fail, which is addressed in the next task).

- [ ] **Step 3: Commit (if auto_commit enabled)**

  Check `.agent/config.yml` for `auto_commit` setting.
  Since `auto_commit` is `false` in `.agent/config.yml`, skip commit and staging. Print: "Skipping commit (auto_commit: false)."

---

### Task 5: 테스트 코드 수정 및 최종 검증

**Files:**
- Modify: `src/test/java/com/example/pitchboxd/matchDetail/presentation/MatchDetailControllerTest.java`
- Modify: `src/test/java/com/example/pitchboxd/matchDetail/service/MatchDetailFacadeServiceTest.java`

- [ ] **Step 1: Fix compilation errors in test code**

  Update DTO constructors in test fixtures to include `isModified` (typically `false` or `true` depending on test logic).

- [ ] **Step 2: Add assertions to verify `isModified` mapping**

  In `MatchDetailFacadeServiceTest.java`, add tests to assert that `isModified` is mapped correctly based on review updates.

- [ ] **Step 3: Run all tests to verify passing status**

  Run: `./gradlew test`
  Expected: BUILD SUCCESSFUL (All tests pass)

- [ ] **Step 4: Commit (if auto_commit enabled)**

  Check `.agent/config.yml` for `auto_commit` setting.
  Since `auto_commit` is `false` in `.agent/config.yml`, skip commit and staging. Print: "Skipping commit (auto_commit: false)."
