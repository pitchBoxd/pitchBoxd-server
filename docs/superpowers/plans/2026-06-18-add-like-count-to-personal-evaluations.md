# Add Like Count to Personal Evaluations Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 로그인 유저의 경기 및 선수 평점 조회 API(`GET /api/v1/matches/{matchId}/detail/personal`)의 응답 DTO인 `MyMatchReviewResponse`와 `MyPlayerReviewResponse`에 평점의 좋아요 개수(`likeCount`) 필드를 추가합니다.

**Architecture:**
* `MyMatchReviewResponse` record DTO에 `Long likeCount` 필드를 추가합니다.
* `MyPlayerReviewResponse` record DTO에 `Long likeCount` 필드를 추가합니다.
* `MatchDetailFacadeService`에서 개인 평가 데이터를 빌드하여 반환할 때, `MatchReview`와 `PlayerReview` 엔티티의 `likeCount` 값을 DTO에 각각 매핑합니다.
* 기존 테스트 및 신규 통합 테스트를 통해 좋아요 개수가 정상 반환되는지 검증합니다.

**Tech Stack:** Java, Spring Boot

---

### Task 1: DTO 변경 및 컴파일 오류 수정

**Files:**
- Modify: [MyMatchReviewResponse.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/matchDetail/dto/response/MyMatchReviewResponse.java)
- Modify: [MyPlayerReviewResponse.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/matchDetail/dto/response/MyPlayerReviewResponse.java)
- Modify: [MatchDetailFacadeService.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/matchDetail/service/MatchDetailFacadeService.java)
- Modify: [MatchDetailFacadeServiceTest.java](file:///Users/yuseokhyeon/pitchboxd/src/test/java/com/example/pitchboxd/matchDetail/service/MatchDetailFacadeServiceTest.java)
- Modify: [MatchDetailControllerTest.java](file:///Users/yuseokhyeon/pitchboxd/src/test/java/com/example/pitchboxd/matchDetail/presentation/MatchDetailControllerTest.java)

- [ ] **Step 1: Modify `MyMatchReviewResponse` DTO**
[MyMatchReviewResponse.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/matchDetail/dto/response/MyMatchReviewResponse.java) 파일을 아래처럼 변경합니다.
```java
package com.example.pitchboxd.matchDetail.dto.response;

public record MyMatchReviewResponse(
        Long reviewId,
        Integer rating,
        String comment,
        boolean isModified,
        Long likeCount
) {}
```

- [ ] **Step 2: Modify `MyPlayerReviewResponse` DTO**
[MyPlayerReviewResponse.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/matchDetail/dto/response/MyPlayerReviewResponse.java) 파일을 아래처럼 변경합니다.
```java
package com.example.pitchboxd.matchDetail.dto.response;

public record MyPlayerReviewResponse(
        Long playerReviewId,
        Long playerId,
        Integer rating,
        String comment,
        boolean isModified,
        Long likeCount
) {}
```

- [ ] **Step 3: Modify `MatchDetailFacadeService.java` Mapping**
[MatchDetailFacadeService.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/matchDetail/service/MatchDetailFacadeService.java) 의 `getMatchPersonalData` 메소드 내 매핑 부분을 수정합니다.
```java
    public MatchDetailPersonalResponse getMatchPersonalData(Long matchId, Long userId) {
        if (userId == null) {
            return new MatchDetailPersonalResponse(false, null, List.of());
        }

        MyMatchReviewResponse myMatchReview = matchReviewRepository.findByMatchIdAndUserId(matchId, userId)
                .map(matchReview -> new MyMatchReviewResponse(
                        matchReview.getId(),
                        matchReview.getPoint(),
                        matchReview.getContent(),
                        matchReview.isUpdated(),
                        matchReview.getLikeCount()
                ))
                .orElse(null);

        List<MyPlayerReviewResponse> myPlayerReviews = playerReviewRepository.findAllByMatchIdAndUserId(matchId, userId)
                .stream()
                .map(playerReview -> new MyPlayerReviewResponse(
                        playerReview.getId(),
                        playerReview.getPlayerId(),
                        playerReview.getPoint(),
                        playerReview.getContent(),
                        playerReview.isUpdated(),
                        playerReview.getLikeCount()
                ))
                .toList();

        boolean isEvaluated = (myMatchReview != null || !myPlayerReviews.isEmpty());
        return new MatchDetailPersonalResponse(isEvaluated, myMatchReview, myPlayerReviews);
    }
```

- [ ] **Step 4: Fix compile errors in tests**
`./gradlew compileTestJava` 명령을 실행해 컴파일 에러를 확인합니다. DTO의 기존 생성자 빌드 및 Assertion에 `likeCount` 더미 인자(0L 등)를 삽입해 컴파일 에러를 모두 보정합니다.

---

### Task 2: 비즈니스 로직 단위 테스트 보완 및 전체 통과 검증

**Files:**
- Modify: [MatchDetailFacadeServiceTest.java](file:///Users/yuseokhyeon/pitchboxd/src/test/java/com/example/pitchboxd/matchDetail/service/MatchDetailFacadeServiceTest.java)
- Modify: [MatchDetailControllerTest.java](file:///Users/yuseokhyeon/pitchboxd/src/test/java/com/example/pitchboxd/matchDetail/presentation/MatchDetailControllerTest.java)

- [ ] **Step 1: Modify unit tests in `MatchDetailFacadeServiceTest`**
`MatchDetailFacadeServiceTest.java` 의 `로그인_유저가_평가한_내용이_있으면_평가함_데이터를_반환한다` (또는 개인 평가 조회 관련 테스트들)에 좋아요 개수(`likeCount`) 검증 코드를 추가합니다.
(평가 시 `likeCount`가 0개 혹은 1개 등 저장된 값이 잘 조회되는지 테스트 내 단언문 보강)

- [ ] **Step 2: Run service test to verify it passes**
Run: `./gradlew test --tests "*MatchDetailFacadeServiceTest*"`
Expected: PASS

- [ ] **Step 3: Modify integration tests in `MatchDetailControllerTest`**
`MatchDetailControllerTest.java` 의 `로그인_유저가_경기와_선수를_모두_평가한_경우_개인_평가_데이터를_조회한다` 등의 개인 평가 통합 테스트에 `likeCount`에 대한 단언문을 보강하고 정상 수신되는지 확인합니다.

- [ ] **Step 4: Run controller test to verify it passes**
Run: `./gradlew test --tests "*MatchDetailControllerTest*"`
Expected: PASS

- [ ] **Step 5: Run all test suite**
Run: `./gradlew test`
Expected: 모든 테스트 PASS (0 failures)
