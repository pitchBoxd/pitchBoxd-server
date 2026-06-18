# Refactor Sort Parameter to Enum Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor the review sort query parameter in `getMatchReviews` API from `String` to a type-safe `ReviewSortType` Enum.

**Architecture:** Create `ReviewSortType` enum in the `matchReview` domain, update repository QueryDSL query methods, update the facade service, update the controller request parameter binding, and fix test suite references.

**Tech Stack:** Java, Spring Boot, Spring Web, QueryDSL, JUnit 5

---

### Task 1: Create ReviewSortType Enum

**Files:**
* Create: `/Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/match/matchReview/domain/ReviewSortType.java`

- [ ] **Step 1: Create the ReviewSortType enum**

Create `ReviewSortType.java` with values `LATEST` and `LIKE`:
```java
package com.example.pitchboxd.match.matchReview.domain;

public enum ReviewSortType {
    LATEST,
    LIKE
}
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew compileJava`
Expected: SUCCESS

- [ ] **Step 3: Commit (if auto_commit enabled)**

Check `.agent/config.yml` for `auto_commit` setting.
If `auto_commit: true` (default when absent):
```bash
git add src/main/java/com/example/pitchboxd/match/matchReview/domain/ReviewSortType.java
git commit -m "feat: add ReviewSortType enum in matchReview domain"
```
If `auto_commit: false`: skip commit and staging. Print: "Skipping commit (auto_commit: false)."

---

### Task 2: Modify Repository Query DSL

**Files:**
* Modify: `/Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/match/matchReview/infrastructure/MatchReviewQueryRepository.java`

- [ ] **Step 1: Update repository query method to accept ReviewSortType**

Update the method signature of `findReviewsByCursor` and the helper methods to use `ReviewSortType` instead of `String`:
```java
// Import the Enum:
import com.example.pitchboxd.match.matchReview.domain.ReviewSortType;
```

Update implementation:
```java
    public List<MatchReview> findReviewsByCursor(Long matchId, Long cursorId, Long cursorLikeCount, ReviewSortType sort, int size) {
        QMatchReview matchReview = QMatchReview.matchReview;

        var query = queryFactory
                .selectFrom(matchReview)
                .where(
                        matchReview.matchId.eq(matchId),
                        buildCursorCondition(cursorId, cursorLikeCount, sort)
                )
                .limit(size + 1);

        if (ReviewSortType.LIKE == sort) {
            query.orderBy(matchReview.likeCount.desc(), matchReview.id.desc());
        } else {
            query.orderBy(matchReview.id.desc());
        }

        return query.fetch();
    }

    private BooleanExpression buildCursorCondition(Long cursorId, Long cursorLikeCount, ReviewSortType sort) {
        if (ReviewSortType.LIKE == sort) {
            return lessThanCursorLike(cursorLikeCount, cursorId);
        }
        return lessThanCursorId(cursorId);
    }
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew compileJava` (Note: service will fail to compile until next task).

- [ ] **Step 3: Commit (if auto_commit enabled)**

Check `.agent/config.yml` for `auto_commit` setting.
If `auto_commit: true` (default when absent):
```bash
git add src/main/java/com/example/pitchboxd/match/matchReview/infrastructure/MatchReviewQueryRepository.java
git commit -m "refactor: update MatchReviewQueryRepository to use ReviewSortType enum"
```
If `auto_commit: false`: skip commit.

---

### Task 3: Modify Facade Service

**Files:**
* Modify: `/Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/matchDetail/service/MatchDetailFacadeService.java`

- [ ] **Step 1: Update getMatchReviews signature and validation**

Import the enum:
```java
import com.example.pitchboxd.match.matchReview.domain.ReviewSortType;
```

Modify `getMatchReviews` to accept `ReviewSortType` and adjust validations:
```java
    public MatchReviewSliceResponse getMatchReviews(Long matchId, Long cursorId, Long cursorLikeCount, ReviewSortType sort, int size, Long userId) {
        if (size <= 0) {
            throw new IllegalArgumentException("페이지 크기는 1 이상이어야 합니다.");
        }
        if (ReviewSortType.LIKE == sort) {
            if ((cursorId == null && cursorLikeCount != null) || (cursorId != null && cursorLikeCount == null)) {
                throw new IllegalArgumentException("추천순 정렬 페이징 시 cursorId와 cursorLikeCount는 모두 null이거나 모두 null이 아니어야 합니다.");
            }
        }

        // 1. QueryDSL로 size+1개 데이터 조회
        List<MatchReview> reviews = matchReviewQueryRepository.findReviewsByCursor(matchId, cursorId, cursorLikeCount, sort, size);
        // ... (rest remains unchanged)
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew compileJava` (Note: Controller will fail compiling until next task).

- [ ] **Step 3: Commit (if auto_commit enabled)**

If `auto_commit: true` (default when absent):
```bash
git add src/main/java/com/example/pitchboxd/matchDetail/service/MatchDetailFacadeService.java
git commit -m "refactor: update MatchDetailFacadeService to use ReviewSortType enum"
```
If `auto_commit: false`: skip commit.

---

### Task 4: Modify Controller request mapping

**Files:**
* Modify: `/Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/matchDetail/presentation/MatchDetailController.java`

- [ ] **Step 1: Update getMatchReviews endpoint parameter type**

Import the enum:
```java
import com.example.pitchboxd.match.matchReview.domain.ReviewSortType;
```

Modify the `@RequestParam` in `getMatchReviews` endpoint:
```java
    @Operation(summary = "경기 페이지 전체 경기 리뷰 조회 (무한 스크롤 페이징)", description = "최신순(LATEST) 및 추천순(LIKE)으로 리뷰를 페이징 조회합니다.")
    @GetMapping("{matchId}/match-reviews")
    public ResponseEntity<SuccessResponse<MatchReviewSliceResponse>> getMatchReviews(
            @PathVariable Long matchId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(required = false) Long cursorLikeCount,
            @RequestParam(defaultValue = "LATEST") ReviewSortType sort, // String에서 ReviewSortType으로 변경
            @RequestParam(defaultValue = "10") int size,
            @LoginUserId(required = false) Long userId
    ) {
        MatchReviewSliceResponse response = matchDetailFacadeService.getMatchReviews(matchId, cursorId, cursorLikeCount,
                sort, size, userId);
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew compileJava`
Expected: SUCCESS

- [ ] **Step 3: Commit (if auto_commit enabled)**

If `auto_commit: true` (default when absent):
```bash
git add src/main/java/com/example/pitchboxd/matchDetail/presentation/MatchDetailController.java
git commit -m "refactor: update MatchDetailController to bind ReviewSortType enum"
```
If `auto_commit: false`: skip commit.

---

### Task 5: Modify Test Code References

**Files:**
* Modify: `/Users/yuseokhyeon/pitchboxd/src/test/java/com/example/pitchboxd/matchDetail/presentation/MatchDetailControllerTest.java`

- [ ] **Step 1: Update controller test request parameters**

Ensure the test methods mapping to Enum compile correctly. In `MatchDetailControllerTest.java`, the tests use REST Assured query parameters:
```java
                .queryParam("sort", "LATEST")
```
and
```java
                .queryParam("sort", "LIKE")
```
Since they pass String parameters to the HTTP client request, the binding on the controller will automatically parse `"LATEST"` and `"LIKE"` into the `ReviewSortType` enum values.
Check if any Java mock/service test calls `matchDetailFacadeService.getMatchReviews(...)` directly with a String argument instead of the enum.
If there are any direct service calls in the controller test or other unit tests, import `com.example.pitchboxd.match.matchReview.domain.ReviewSortType` and update the argument to `ReviewSortType.LATEST` or `ReviewSortType.LIKE`.

- [ ] **Step 2: Run all tests to verify**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL (All tests pass)

- [ ] **Step 3: Commit (if auto_commit enabled)**

If `auto_commit: true` (default when absent):
```bash
git add src/test/java/com/example/pitchboxd/matchDetail/presentation/MatchDetailControllerTest.java
git commit -m "test: verify ReviewSortType binding in MatchDetailControllerTest"
```
If `auto_commit: false`: skip commit.
