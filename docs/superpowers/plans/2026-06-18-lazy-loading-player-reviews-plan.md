# Lazy Loading Player Reviews Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 경기 상세 화면에서 특정 선수를 클릭했을 때, 해당 선수의 상세 텍스트 리뷰 목록을 QueryDSL 기반 무한 스크롤로 지연 로딩(Lazy Loading)하는 조회 기능을 API 및 쿼리 최적화(N+1 방지)를 포함하여 구현합니다.

**Architecture:**
* `PlayerReviewLikeRepository`에 리뷰 ID 목록에 대한 좋아요 여부를 일괄 조회하는 `@Query` 메소드를 추가합니다.
* QueryDSL을 사용하여 `PlayerReviewQueryRepository`를 신규 작성해 커서 기반 페이징 조회를 처리합니다.
* `PlayerReviewFacadeService`에 `getPlayerReviews` 메소드를 추가하고, 쿼리 조회 결과 데이터와 좋아요 유무 상태 및 작성자의 응원팀 명(favoriteTeamName)을 벌크로 한 번에 매핑하여 반환하는 가공 로직을 설계합니다.
* `MatchDetailController`에 `GET /api/v1/matches/{matchId}/players/{playerId}/player-reviews` 엔드포인트를 노출합니다.

**Tech Stack:** Java, Spring Boot, Spring Data JPA, QueryDSL, JUnit 5

---

### Task 1: `PlayerReviewLikeRepository` 벌크 조회 메소드 추가

**Files:**
- Modify: [PlayerReviewLikeRepository.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/match/playerReview/infrastructure/PlayerReviewLikeRepository.java)

- [ ] **Step 1: Declare query method**
[PlayerReviewLikeRepository.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/match/playerReview/infrastructure/PlayerReviewLikeRepository.java)에 아래 메소드를 추가합니다.
```java
    @Query("SELECT prl.playerReviewId FROM PlayerReviewLike prl WHERE prl.playerReviewId IN :reviewIds AND prl.userId = :userId")
    java.util.List<Long> findLikedReviewIdsIn(
            @Param("reviewIds") java.util.Collection<Long> reviewIds,
            @Param("userId") Long userId
    );
```

- [ ] **Step 2: Verify Compilation**
Run: `./gradlew compileJava`
Expected: SUCCESS

- [ ] **Step 3: Commit**
Stage and commit changes if `auto_commit` is enabled in `.agent/config.yml`.

---

### Task 2: `PlayerReviewQueryRepository` (QueryDSL) 구현 및 테스트

**Files:**
- Create: [PlayerReviewQueryRepository.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/match/playerReview/infrastructure/PlayerReviewQueryRepository.java)
- Create: [PlayerReviewQueryRepositoryTest.java](file:///Users/yuseokhyeon/pitchboxd/src/test/java/com/example/pitchboxd/match/playerReview/infrastructure/PlayerReviewQueryRepositoryTest.java)

- [ ] **Step 1: Write the failing test**
[PlayerReviewQueryRepositoryTest.java](file:///Users/yuseokhyeon/pitchboxd/src/test/java/com/example/pitchboxd/match/playerReview/infrastructure/PlayerReviewQueryRepositoryTest.java)를 생성하여 최신순(LATEST) 및 추천순(LIKE) 정렬에 의한 커서 페이징 조회 동작을 검증하는 테스트 코드를 작성합니다.

```java
package com.example.pitchboxd.match.playerReview.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.pitchboxd.match.matchReview.domain.ReviewSortType;
import com.example.pitchboxd.match.playerReview.domain.PlayerReview;
import com.example.pitchboxd.support.DatabaseCleaner;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PlayerReviewQueryRepositoryTest {

    @Autowired
    private PlayerReviewQueryRepository playerReviewQueryRepository;

    @Autowired
    private PlayerReviewRepository playerReviewRepository;

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

    @DisplayName("최신순(LATEST) 정렬로 플레이어 리뷰를 커서 기반 페이징 조회한다.")
    @Test
    void findReviewsByCursor_latest() {
        // given
        Long matchId = 1L;
        Long playerId = 2L;
        PlayerReview review1 = playerReviewRepository.save(new PlayerReview(matchId, playerId, 10L, 5, "리뷰1"));
        PlayerReview review2 = playerReviewRepository.save(new PlayerReview(matchId, playerId, 11L, 7, "리뷰2"));
        PlayerReview review3 = playerReviewRepository.save(new PlayerReview(matchId, playerId, 12L, 9, "리뷰3"));

        // when (size=2)
        List<PlayerReview> result = playerReviewQueryRepository.findReviewsByCursor(matchId, playerId, null, null, ReviewSortType.LATEST, 2);

        // then (size+1인 3개 조회 확인)
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getId()).isEqualTo(review3.getId()); // 최신순 역순
        assertThat(result.get(1).getId()).isEqualTo(review2.getId());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**
Run: `./gradlew test --tests "*PlayerReviewQueryRepositoryTest*"`
Expected: 컴파일 에러 (클래스가 존재하지 않음)

- [ ] **Step 3: Write implementation**
[PlayerReviewQueryRepository.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/match/playerReview/infrastructure/PlayerReviewQueryRepository.java)를 생성합니다.

```java
package com.example.pitchboxd.match.playerReview.infrastructure;

import com.example.pitchboxd.match.matchReview.domain.ReviewSortType;
import com.example.pitchboxd.match.playerReview.domain.PlayerReview;
import com.example.pitchboxd.match.playerReview.domain.QPlayerReview;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PlayerReviewQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<PlayerReview> findReviewsByCursor(Long matchId, Long playerId, Long cursorId, Long cursorLikeCount, ReviewSortType sort, int size) {
        QPlayerReview playerReview = QPlayerReview.playerReview;
        
        var query = queryFactory
                .selectFrom(playerReview)
                .where(
                        playerReview.matchId.eq(matchId),
                        playerReview.playerId.eq(playerId),
                        buildCursorCondition(cursorId, cursorLikeCount, sort)
                );

        if (ReviewSortType.LIKE == sort) {
            query = query.orderBy(playerReview.likeCount.desc(), playerReview.id.desc());
        } else {
            query = query.orderBy(playerReview.id.desc());
        }

        return query.limit(size + 1).fetch();
    }

    private BooleanExpression buildCursorCondition(Long cursorId, Long cursorLikeCount, ReviewSortType sort) {
        if (ReviewSortType.LIKE == sort) {
            return lessThanCursorLike(cursorLikeCount, cursorId);
        }
        return lessThanCursorId(cursorId);
    }

    private BooleanExpression lessThanCursorId(Long cursorId) {
        QPlayerReview playerReview = QPlayerReview.playerReview;
        return cursorId != null ? playerReview.id.lt(cursorId) : null;
    }

    private BooleanExpression lessThanCursorLike(Long cursorLikeCount, Long cursorId) {
        QPlayerReview playerReview = QPlayerReview.playerReview;
        if (cursorLikeCount == null || cursorId == null) {
            return null;
        }
        return playerReview.likeCount.lt(cursorLikeCount)
                .or(playerReview.likeCount.eq(cursorLikeCount).and(playerReview.id.lt(cursorId)));
    }
}
```

- [ ] **Step 4: Run test to verify it passes**
Run: `./gradlew test --tests "*PlayerReviewQueryRepositoryTest*"`
Expected: PASS

- [ ] **Step 5: Commit**
Stage and commit changes if `auto_commit` is enabled.

---

### Task 3: `PlayerReviewFacadeService` 비즈니스 로직 및 DTO 벌크 매핑 구현

**Files:**
- Modify: [PlayerReviewFacadeService.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/match/playerReview/service/facade/PlayerReviewFacadeService.java)
- Create: [PlayerReviewSliceResponse.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/matchDetail/dto/response/PlayerReviewSliceResponse.java)
- Create: [PlayerReviewDetailResponse.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/matchDetail/dto/response/PlayerReviewDetailResponse.java)
- Modify: [PlayerReviewFacadeServiceTest.java](file:///Users/yuseokhyeon/pitchboxd/src/test/java/com/example/pitchboxd/match/playerReview/service/facade/PlayerReviewFacadeServiceTest.java)

- [ ] **Step 1: Create Response DTO files**
두 개의 Response DTO 레코드인 [PlayerReviewSliceResponse.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/matchDetail/dto/response/PlayerReviewSliceResponse.java)와 [PlayerReviewDetailResponse.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/matchDetail/dto/response/PlayerReviewDetailResponse.java)를 생성합니다.

```java
// PlayerReviewSliceResponse.java
package com.example.pitchboxd.matchDetail.dto.response;

import java.util.List;

public record PlayerReviewSliceResponse(
        List<PlayerReviewDetailResponse> reviews,
        Long nextCursorId,
        Long nextCursorLikeCount,
        boolean hasNext
) {}
```

```java
// PlayerReviewDetailResponse.java
package com.example.pitchboxd.matchDetail.dto.response;

import java.time.LocalDateTime;

public record PlayerReviewDetailResponse(
        Long id,
        String nickname,
        String favoriteTeamName,
        int point,
        String content,
        String fanType,
        long likeCount,
        boolean isLiked,
        LocalDateTime createdAt
) {}
```

- [ ] **Step 2: Write the failing test**
[PlayerReviewFacadeServiceTest.java](file:///Users/yuseokhyeon/pitchboxd/src/test/java/com/example/pitchboxd/match/playerReview/service/facade/PlayerReviewFacadeServiceTest.java)에 플레이어 리뷰 페이징 목록 가공 및 N+1 최적화(좋아요 여부 벌크 매핑) 검증을 담당하는 테스트 케이스를 작성합니다.

- [ ] **Step 3: Modify implementation**
[PlayerReviewFacadeService.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/match/playerReview/service/facade/PlayerReviewFacadeService.java)를 수정합니다.
* `PlayerReviewQueryRepository` 및 `PlayerReviewLikeRepository`, `UserRepository`, `TeamRepository` 를 주입받습니다.
* `getPlayerReviews(Long matchId, Long playerId, Long cursorId, Long cursorLikeCount, ReviewSortType sort, int size, Long loginUserId)` 메소드를 구현합니다.
  * 리포지토리를 통해 `size + 1` 개의 엔티티를 조회합니다.
  * `hasNext` 및 Cursor 변수들을 셋팅하고 초과 데이터 1개를 절단합니다.
  * 작성자 유저 ID 목록을 취합해 `UserRepository.findAllById(userIds)`로 조회하여 닉네임과 선호 팀 ID를 맵으로 구성합니다.
  * 선호 팀 ID 목록을 수집하여 `TeamRepository.findAllById(teamIds)`로 팀명을 맵으로 구성합니다.
  * 로그인 유저 정보가 있는 경우 `playerReviewLikeRepository.findLikedReviewIdsIn(reviewIds, loginUserId)`로 좋아요 여부를 일괄 조회하여 세팅합니다.
  * 가공 완료된 `PlayerReviewSliceResponse` 객체를 구성하여 반환합니다.

- [ ] **Step 4: Run test to verify it passes**
Run: `./gradlew test --tests "*PlayerReviewFacadeServiceTest*"`
Expected: PASS

- [ ] **Step 5: Commit**
Stage and commit changes if `auto_commit` is enabled.

---

### Task 4: `MatchDetailController` API 연동 및 전체 리그 통과 확인

**Files:**
- Modify: [MatchDetailController.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/matchDetail/presentation/MatchDetailController.java)
- Create: [MatchDetailControllerTest.java](file:///Users/yuseokhyeon/pitchboxd/src/test/java/com/example/pitchboxd/matchDetail/presentation/MatchDetailControllerTest.java) (만약 `MatchDetailControllerTest.java`가 없다면 생성)

- [ ] **Step 1: Write the failing test**
[MatchDetailControllerTest.java](file:///Users/yuseokhyeon/pitchboxd/src/test/java/com/example/pitchboxd/matchDetail/presentation/MatchDetailControllerTest.java)를 작성하여 `GET /api/v1/matches/{matchId}/players/{playerId}/player-reviews` 경로 호출 시 선수 리뷰가 JSON 형태로 정상 페이징 반환되는지 확인하는 통합 테스트를 생성합니다.

- [ ] **Step 2: Run test to verify it fails**
Run: `./gradlew test --tests "*MatchDetailControllerTest*"`
Expected: Fails with 404 Not Found (엔드포인트가 없음)

- [ ] **Step 3: Modify implementation**
[MatchDetailController.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/matchDetail/presentation/MatchDetailController.java)를 수정합니다.
* 아래 엔드포인트를 노출시킵니다.
```java
    @Operation(summary = "경기 페이지 특정 선수 리뷰 조회 (무한 스크롤 페이징)", description = "선수 카드 클릭 시 최신순(LATEST) 및 추천순(LIKE)으로 플레이어 리뷰를 페이징 조회합니다.")
    @GetMapping("{matchId}/players/{playerId}/player-reviews")
    public ResponseEntity<SuccessResponse<PlayerReviewSliceResponse>> getPlayerReviews(
            @PathVariable Long matchId,
            @PathVariable Long playerId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(required = false) Long cursorLikeCount,
            @RequestParam(defaultValue = "LATEST") ReviewSortType sort,
            @RequestParam(defaultValue = "10") int size,
            @LoginUserId(required = false) Long userId
    ) {
        PlayerReviewSliceResponse response = matchDetailFacadeService.getPlayerReviews(matchId, playerId, cursorId,
                cursorLikeCount, sort, size, userId);
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }
```
* [MatchDetailFacadeService.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/matchDetail/service/MatchDetailFacadeService.java)를 수정하여 `playerReviewFacadeService`를 위임받아 `getPlayerReviews`를 호출해 그대로 반환하도록 구현을 위임합니다.

- [ ] **Step 4: Run test to verify it passes**
Run: `./gradlew test --tests "*MatchDetailControllerTest*"`
Expected: PASS

- [ ] **Step 5: Run all test suite**
Run: `./gradlew test`
Expected: 모든 테스트 PASS (0 failures)

- [ ] **Step 6: Commit**
Stage and commit changes if `auto_commit` is enabled.
