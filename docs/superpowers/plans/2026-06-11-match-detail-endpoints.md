# 경기 상세 화면 조회 API 구현 계획서 (Match Detail Query Endpoints Implementation Plan)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 경기 상세 화면에 필요한 공통 통계 데이터 API, 로그인 사용자 개인화 데이터 API, 그리고 전체 리뷰 목록을 조회하는 무한 스크롤(커서 기반 페이징) API를 구현합니다.

**Architecture:** 
1. `GET /api/v1/matches/{matchId}/detail/static`: [MatchDetailFacadeService](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/matchDetail/service/MatchDetailFacadeService.java)를 확장하여 `MatchStatistics`, `PlayerStatistics`, `MatchReview` 데이터로부터 평점 평균, 삼색 평점, 평점 분포 히스토그램, MOM 및 Top 3 수훈 선수를 계산하여 반환합니다.
2. `GET /api/v1/matches/{matchId}/detail/personal`: 로그인 유저 세션 정보(없을 경우 Null)를 주입받아 유저가 작성한 경기 및 선수 리뷰 상태를 반환합니다.
3. `GET /api/v1/matches/{matchId}/match-reviews`: 최신순 및 추천순(좋아요 순) 정렬 조건을 지원하는 QueryDSL 커서 기반 페이징을 구현합니다.

**Tech Stack:** Spring Boot 3.5, Spring Data JPA, QueryDSL, Java 21

---

## 파일 변경/생성 맵 (File Changes Map)

### 신규 생성 파일
1. DTO: `src/main/java/com/example/pitchboxd/matchDetail/dto/response/MatchDetailPersonalResponse.java`
2. DTO: `src/main/java/com/example/pitchboxd/matchDetail/dto/response/MyMatchReviewResponse.java`
3. DTO: `src/main/java/com/example/pitchboxd/matchDetail/dto/response/MyPlayerReviewResponse.java`
4. DTO: `src/main/java/com/example/pitchboxd/matchDetail/dto/response/MatchReviewSliceResponse.java`
5. DTO: `src/main/java/com/example/pitchboxd/matchDetail/dto/response/MatchReviewDetailResponse.java`
6. 테스트: `src/test/java/com/example/pitchboxd/matchDetail/presentation/MatchDetailControllerTest.java`

### 수정 파일
1. `src/main/java/com/example/pitchboxd/match/matchReview/infrastructure/MatchReviewRepository.java` (JPA 쿼리 메소드 추가)
2. `src/main/java/com/example/pitchboxd/match/playerReview/infrastructure/PlayerReviewRepository.java` (JPA 쿼리 메소드 추가)
3. `src/main/java/com/example/pitchboxd/match/playerStatistics/infrastructure/PlayerStatisticsRepository.java` (JPA 쿼리 메소드 추가)
4. `src/main/java/com/example/pitchboxd/match/matchReview/infrastructure/MatchReviewQueryRepository.java` (QueryDSL 커서 페이징 조회 쿼리 추가)
5. `src/main/java/com/example/pitchboxd/matchDetail/dto/response/MatchDetailResponse.java` (공통 통계 필드 추가 및 리팩토링)
6. `src/main/java/com/example/pitchboxd/matchDetail/dto/response/LineupResponse.java` (averageRating 필드 추가)
7. `src/main/java/com/example/pitchboxd/matchDetail/presentation/MatchDetailController.java` (Endpoints 신설 및 연동)
8. `src/main/java/com/example/pitchboxd/matchDetail/service/MatchDetailFacadeService.java` (비즈니스 로직 작성)

---

## 상세 태스크 (Detailed Tasks)

### Task 1: Repository 신규 조회 메서드 추가

**Files:**
* Modify: [MatchReviewRepository](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/match/matchReview/infrastructure/MatchReviewRepository.java)
* Modify: [PlayerReviewRepository](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/match/playerReview/infrastructure/PlayerReviewRepository.java)
* Modify: [PlayerStatisticsRepository](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/match/playerStatistics/infrastructure/PlayerStatisticsRepository.java)

- [ ] **Step 1: JpaRepository 인터페이스들에 신규 메서드 시그니처 추가**

`MatchReviewRepository.java`에 추가:
```java
java.util.Optional<MatchReview> findByMatchIdAndUserId(Long matchId, Long userId);

@Query("select r.point as point, count(r) as count from MatchReview r where r.matchId = :matchId group by r.point")
List<Object[]> countPointDistributionByMatchId(@Param("matchId") Long matchId);
```

`PlayerReviewRepository.java`에 추가:
```java
java.util.List<PlayerReview> findAllByMatchIdAndUserId(Long matchId, Long userId);
```

`PlayerStatisticsRepository.java`에 추가:
```java
java.util.List<PlayerStatistics> findAllByMatchId(Long matchId);
```

- [ ] **Step 2: JUnit 테스트 코드를 작성하여 쿼리 메서드 동작 확인**
`src/test/java/com/example/pitchboxd/match/matchReview/infrastructure/MatchReviewRepositoryTest.java` (없을 경우 신규 작성) 또는 스프링 부트 슬라이스 테스트를 통해 쿼리 검증.
```java
// 예시 검증 코드
@DataJpaTest
class MatchReviewRepositoryTest {
    @Autowired private MatchReviewRepository matchReviewRepository;
    
    @Test
    void pointDistributionTest() {
        matchReviewRepository.save(new MatchReview(1L, 1L, 8, "좋은 매치", FanType.HOME));
        matchReviewRepository.save(new MatchReview(1L, 2L, 8, "재밌네요", FanType.NEUTRAL));
        List<Object[]> distribution = matchReviewRepository.countPointDistributionByMatchId(1L);
        assertThat(distribution).isNotEmpty();
    }
}
```

- [ ] **Step 3: 테스트 실행 및 통과 확인**
Run: `./gradlew test --tests "*MatchReviewRepositoryTest*"`
Expected: PASS

- [ ] **Step 4: Commit**
`.agent/config.yml`의 `auto_commit` 설정을 확인합니다.
If `auto_commit: true` (default):
```bash
git add src/main/java/com/example/pitchboxd/match/matchReview/infrastructure/MatchReviewRepository.java src/main/java/com/example/pitchboxd/match/playerReview/infrastructure/PlayerReviewRepository.java src/main/java/com/example/pitchboxd/match/playerStatistics/infrastructure/PlayerStatisticsRepository.java
git commit -m "feat: add query methods in review and statistics repositories"
```

---

### Task 2: Match Detail Static API 고도화

**Files:**
* Modify: [LineupResponse](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/matchDetail/dto/response/LineupResponse.java)
* Modify: [MatchDetailResponse](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/matchDetail/dto/response/MatchDetailResponse.java)
* Modify: [MatchDetailFacadeService](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/matchDetail/service/MatchDetailFacadeService.java)

- [ ] **Step 1: DTO 레코드들 구조 개선**

`LineupResponse.java` 수정:
```java
public record LineupResponse(
        Long playerId,
        String playerName,
        Integer backNumber,
        ParticipationStatus status,
        Double averageRating
) {
    public static LineupResponse of(LineupPlayerModel lineup, Double averageRating) {
        return new LineupResponse(lineup.playerId(), lineup.playerName(), lineup.backNumber(), lineup.status(), averageRating);
    }
}
```

`MatchDetailResponse.java` 수정:
```java
import java.util.Map;
import java.util.List;

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
        Double neutralFanAverageRating,
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

- [ ] **Step 2: MatchDetailFacadeService 로직 추가**
`MatchDetailFacadeService.java` 내 `getMatchStaticData(Long matchId)` 구현을 수정합니다.
```java
public MatchDetailResponse getMatchStaticData(Long matchId) {
    MatchDetailStaticModel matchDetail = matchQueryService.findMatchStaticDetailById(matchId);
    List<LineupPlayerModel> lineups = matchLineupQueryService.findLineupAndPlayedPlayers(matchId);
    
    // 1. PlayerStatistics 조회 및 캐싱 매핑
    List<PlayerStatistics> playerStats = playerStatisticsRepository.findAllByMatchId(matchId);
    Map<Long, Double> playerRatingsMap = playerStats.stream()
            .collect(Collectors.toMap(PlayerStatistics::getPlayerId, PlayerStatistics::getAverageRating));
            
    // 2. 라인업 생성
    List<LineupResponse> homeLineupResponses = lineups.stream()
            .filter(l -> l.teamId().equals(matchDetail.homeTeamId()))
            .map(l -> LineupResponse.of(l, playerRatingsMap.getOrDefault(l.playerId(), 0.0)))
            .toList();
    List<LineupResponse> awayLineupResponses = lineups.stream()
            .filter(l -> l.teamId().equals(matchDetail.awayTeamId()))
            .map(l -> LineupResponse.of(l, playerRatingsMap.getOrDefault(l.playerId(), 0.0)))
            .toList();

    // 3. MatchStatistics 조회
    MatchStatistics matchStats = matchStatisticsRepository.findByMatchId(matchId)
            .orElse(new MatchStatistics(matchId));
            
    // 4. 중립 팬 평점 계산
    long totalSum = matchStats.getTotalRatingSum();
    long homeSum = matchStats.getHomeFanRatingSum();
    long awaySum = matchStats.getAwayFanRatingSum();
    int totalCount = matchStats.getTotalReviewCount();
    int homeCount = matchStats.getHomeFanReviewCount();
    int awayCount = matchStats.getAwayFanReviewCount();
    
    int neutralCount = totalCount - homeCount - awayCount;
    long neutralSum = totalSum - homeSum - awaySum;
    double neutralAverage = neutralCount == 0 ? 0.0 : (neutralSum / (double) neutralCount) / 2.0;

    // 5. 평점 분포 계산 (0~10점)
    List<Object[]> rawDistribution = matchReviewRepository.countPointDistributionByMatchId(matchId);
    Map<Integer, Long> distributionMap = new HashMap<>();
    for (int i = 1; i <= 10; i++) {
        distributionMap.put(i, 0L);
    }
    for (Object[] row : rawDistribution) {
        Integer point = (Integer) row[0];
        Long count = (Long) row[1];
        if (point >= 1 && point <= 10) {
            distributionMap.put(point, count);
        }
    }

    // 6. Highlights (MOM & Top 3) 계산
    // 평점 높은 순 -> 투표 많은 순 -> ID 역순
    List<PlayerStatistics> sortedStats = playerStats.stream()
            .filter(ps -> ps.getReviewCount() > 0)
            .sorted(Comparator.comparingDouble(PlayerStatistics::getAverageRating).reversed()
                    .thenComparing(PlayerStatistics::getReviewCount, Comparator.reverseOrder())
                    .thenComparing(PlayerStatistics::getPlayerId, Comparator.reverseOrder()))
            .toList();
            
    MatchDetailResponse.HighlightPlayerResponse mom = null;
    if (!sortedStats.isEmpty()) {
        PlayerStatistics momStat = sortedStats.get(0);
        String momName = findPlayerName(lineups, momStat.getPlayerId());
        mom = new MatchDetailResponse.HighlightPlayerResponse(momStat.getPlayerId(), momName, momStat.getAverageRating());
    }
    
    List<MatchDetailResponse.HighlightPlayerResponse> top3 = sortedStats.stream()
            .limit(3)
            .map(ps -> new MatchDetailResponse.HighlightPlayerResponse(
                    ps.getPlayerId(),
                    findPlayerName(lineups, ps.getPlayerId()),
                    ps.getAverageRating()
            ))
            .toList();

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
            neutralAverage,
            distributionMap,
            new MatchDetailResponse.MatchHighlightsResponse(mom, top3)
    );
}

private String findPlayerName(List<LineupPlayerModel> lineups, Long playerId) {
    return lineups.stream()
            .filter(l -> l.playerId().equals(playerId))
            .map(LineupPlayerModel::playerName)
            .findFirst()
            .orElse("Unknown Player");
}
```

- [ ] **Step 3: JUnit 통합 테스트 작성**
`src/test/java/com/example/pitchboxd/matchDetail/service/MatchDetailFacadeServiceTest.java` 작성 후 실행.
- [ ] **Step 4: Commit**
```bash
git add src/main/java/com/example/pitchboxd/matchDetail/dto/response/MatchDetailResponse.java src/main/java/com/example/pitchboxd/matchDetail/dto/response/LineupResponse.java src/main/java/com/example/pitchboxd/matchDetail/service/MatchDetailFacadeService.java
git commit -m "feat: implement static match detail aggregation including rating distribution and highlights"
```

---

### Task 3: Match Detail Personal API 구현

**Files:**
* Create: `src/main/java/com/example/pitchboxd/matchDetail/dto/response/MatchDetailPersonalResponse.java`
* Create: `src/main/java/com/example/pitchboxd/matchDetail/dto/response/MyMatchReviewResponse.java`
* Create: `src/main/java/com/example/pitchboxd/matchDetail/dto/response/MyPlayerReviewResponse.java`
* Modify: [MatchDetailController](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/matchDetail/presentation/MatchDetailController.java)
* Modify: [MatchDetailFacadeService](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/matchDetail/service/MatchDetailFacadeService.java)

- [ ] **Step 1: DTO 생성**

`MatchDetailPersonalResponse.java`:
```java
package com.example.pitchboxd.matchDetail.dto.response;

import java.util.List;

public record MatchDetailPersonalResponse(
        boolean isEvaluated,
        MyMatchReviewResponse myMatchReview,
        List<MyPlayerReviewResponse> myPlayerReviews
) {}
```

`MyMatchReviewResponse.java`:
```java
package com.example.pitchboxd.matchDetail.dto.response;

public record MyMatchReviewResponse(
        Long reviewId,
        Integer rating,
        String comment
) {}
```

`MyPlayerReviewResponse.java`:
```java
package com.example.pitchboxd.matchDetail.dto.response;

public record MyPlayerReviewResponse(
        Long playerReviewId,
        Long playerId,
        Integer rating,
        String comment
) {}
```

- [ ] **Step 2: MatchDetailFacadeService 개인 평가 조회 로직 구현**
`MatchDetailFacadeService.java`에 추가:
```java
public MatchDetailPersonalResponse getMatchPersonalData(Long matchId, Long userId) {
    if (userId == null) {
        return new MatchDetailPersonalResponse(false, null, List.of());
    }

    Optional<MatchReview> matchReviewOpt = matchReviewRepository.findByMatchIdAndUserId(matchId, userId);
    if (matchReviewOpt.isEmpty()) {
        return new MatchDetailPersonalResponse(false, null, List.of());
    }

    MatchReview matchReview = matchReviewOpt.get();
    MyMatchReviewResponse myMatchReview = new MyMatchReviewResponse(
            matchReview.getId(),
            matchReview.getPoint(),
            matchReview.getContent()
    );

    List<PlayerReview> playerReviews = playerReviewRepository.findAllByMatchIdAndUserId(matchId, userId);
    List<MyPlayerReviewResponse> myPlayerReviews = playerReviews.stream()
            .map(pr -> new MyPlayerReviewResponse(pr.getId(), pr.getPlayerId(), pr.getPoint(), pr.getContent()))
            .toList();

    return new MatchDetailPersonalResponse(true, myMatchReview, myPlayerReviews);
}
```

- [ ] **Step 3: MatchDetailController에 GET Endpoint 추가**
`MatchDetailController.java`에 추가:
```java
@Operation(summary = "경기 페이지 로그인 유저 개인 평가 데이터", description = "로그인 유저가 남긴 경기 리뷰 및 선수 평점 목록을 가져옵니다.")
@GetMapping("{matchId}/detail/personal")
public ResponseEntity<SuccessResponse<MatchDetailPersonalResponse>> getMatchPersonalData(
        @PathVariable Long matchId,
        @LoginUserId(required = false) Long userId
) {
    MatchDetailPersonalResponse response = matchDetailFacadeService.getMatchPersonalData(matchId, userId);
    HttpStatus status = HttpStatus.OK;

    return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
}
```

- [ ] **Step 4: JUnit 테스트 검증**
`src/test/java/com/example/pitchboxd/matchDetail/presentation/MatchDetailControllerTest.java` 작성 후 실행.
Expected: PASS
- [ ] **Step 5: Commit**
```bash
git add src/main/java/com/example/pitchboxd/matchDetail/dto/response/MatchDetailPersonalResponse.java src/main/java/com/example/pitchboxd/matchDetail/dto/response/MyMatchReviewResponse.java src/main/java/com/example/pitchboxd/matchDetail/dto/response/MyPlayerReviewResponse.java src/main/java/com/example/pitchboxd/matchDetail/presentation/MatchDetailController.java src/main/java/com/example/pitchboxd/matchDetail/service/MatchDetailFacadeService.java
git commit (if auto_commit)
```

---

### Task 4: QueryDSL 전체 리뷰 커서 페이징 조회 구현

**Files:**
* Modify: [MatchReviewQueryRepository](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/match/matchReview/infrastructure/MatchReviewQueryRepository.java)
* Create: `src/main/java/com/example/pitchboxd/matchDetail/dto/response/MatchReviewDetailResponse.java`
* Create: `src/main/java/com/example/pitchboxd/matchDetail/dto/response/MatchReviewSliceResponse.java`
* Modify: [MatchDetailController](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/matchDetail/presentation/MatchDetailController.java)
* Modify: [MatchDetailFacadeService](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/matchDetail/service/MatchDetailFacadeService.java)

- [ ] **Step 1: DTO 생성**

`MatchReviewDetailResponse.java`:
```java
package com.example.pitchboxd.matchDetail.dto.response;

import com.example.pitchboxd.match.matchStatistics.domain.FanType;
import java.time.LocalDateTime;

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
        LocalDateTime createdAt
) {}
```

`MatchReviewSliceResponse.java`:
```java
package com.example.pitchboxd.matchDetail.dto.response;

import java.util.List;

public record MatchReviewSliceResponse(
        List<MatchReviewDetailResponse> reviews,
        Long nextCursorId,
        Long nextCursorLikeCount,
        boolean hasNext
) {}
```

- [ ] **Step 2: MatchReviewQueryRepository QueryDSL 페이징 로직 구현**

`MatchReviewQueryRepository.java`에 추가:
```java
import com.example.pitchboxd.match.matchReview.domain.MatchReview;
import com.querydsl.core.types.dsl.BooleanExpression;

public List<MatchReview> findReviewsByCursor(Long matchId, Long cursorId, Long cursorLikeCount, String sort, int size) {
    QMatchReview matchReview = QMatchReview.matchReview;

    var query = queryFactory
            .selectFrom(matchReview)
            .where(
                    matchReview.matchId.eq(matchId),
                    buildCursorCondition(cursorId, cursorLikeCount, sort)
            )
            .limit(size + 1); // 다음 페이지 존재 여부 확인을 위해 +1 조회

    if ("LIKE".equalsIgnoreCase(sort)) {
        query.orderBy(matchReview.likeCount.desc(), matchReview.id.desc());
    } else {
        query.orderBy(matchReview.id.desc());
    }

    return query.fetch();
}

private BooleanExpression buildCursorCondition(Long cursorId, Long cursorLikeCount, String sort) {
    QMatchReview matchReview = QMatchReview.matchReview;

    if ("LIKE".equalsIgnoreCase(sort)) {
        return lessThanCursorLike(cursorLikeCount, cursorId);
    }
    return lessThanCursorId(cursorId);
}

private BooleanExpression lessThanCursorId(Long cursorId) {
    QMatchReview matchReview = QMatchReview.matchReview;
    return cursorId != null ? matchReview.id.lt(cursorId) : null;
}

private BooleanExpression lessThanCursorLike(Long cursorLikeCount, Long cursorId) {
    QMatchReview matchReview = QMatchReview.matchReview;
    if (cursorLikeCount == null || cursorId == null) {
        return null;
    }
    return matchReview.likeCount.lt(cursorLikeCount)
            .or(matchReview.likeCount.eq(cursorLikeCount).and(matchReview.id.lt(cursorId)));
}
```

- [ ] **Step 3: Facade Service 및 Controller 통합**

`MatchDetailFacadeService.java`에 추가:
```java
public MatchReviewSliceResponse getMatchReviews(Long matchId, Long cursorId, Long cursorLikeCount, String sort, int size, Long userId) {
    // 1. QueryDSL로 size+1개 데이터 조회
    List<MatchReview> reviews = matchReviewQueryRepository.findReviewsByCursor(matchId, cursorId, cursorLikeCount, sort, size);

    boolean hasNext = reviews.size() > size;
    List<MatchReview> content = hasNext ? reviews.subList(0, size) : reviews;

    // 2. 유저 정보 매핑을 위한 userId 조회 (한 번에 조회하여 매핑 성능 확보)
    List<Long> authorIds = content.stream().map(MatchReview::getUserId).toList();
    List<User> authors = userRepository.findAllById(authorIds); // DI 필요
    Map<Long, User> authorMap = authors.stream().collect(Collectors.toMap(User::getId, Function.identity()));

    // 3. 좋아요 상태값 조회
    List<Long> reviewIds = content.stream().map(MatchReview::getId).toList();
    Map<Long, Boolean> likedStatus = matchReviewLikeService.checkLikedStatusForReviews(reviewIds, userId);

    List<MatchReviewDetailResponse> reviewResponses = content.stream()
            .map(r -> {
                User author = authorMap.get(r.getUserId());
                String nickname = author != null ? author.getNickname() : "Unknown";
                String profile = author != null ? author.getProfileImage() : "";
                boolean isLiked = likedStatus.getOrDefault(r.getId(), false);
                boolean isOwner = r.isOwner(userId);
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
                        r.getCreatedAt()
                );
            })
            .toList();

    // 4. 다음 페이지 커서 정보 빌드
    Long nextCursorId = null;
    Long nextCursorLikeCount = null;
    if (hasNext && !content.isEmpty()) {
        MatchReview lastReview = content.get(content.size() - 1);
        nextCursorId = lastReview.getId();
        nextCursorLikeCount = lastReview.getLikeCount();
    }

    return new MatchReviewSliceResponse(reviewResponses, nextCursorId, nextCursorLikeCount, hasNext);
}
```

`MatchDetailController.java`에 추가:
```java
@Operation(summary = "경기 페이지 전체 경기 리뷰 조회 (무한 스크롤 페이징)", description = "최신순(LATEST) 및 추천순(LIKE)으로 리뷰를 페이징 조회합니다.")
@GetMapping("{matchId}/match-reviews")
public ResponseEntity<SuccessResponse<MatchReviewSliceResponse>> getMatchReviews(
        @PathVariable Long matchId,
        @RequestParam(required = false) Long cursorId,
        @RequestParam(required = false) Long cursorLikeCount,
        @RequestParam(defaultValue = "LATEST") String sort,
        @RequestParam(defaultValue = "10") int size,
        @LoginUserId(required = false) Long userId
) {
    MatchReviewSliceResponse response = matchDetailFacadeService.getMatchReviews(matchId, cursorId, cursorLikeCount, sort, size, userId);
    HttpStatus status = HttpStatus.OK;

    return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
}
```

- [ ] **Step 4: JUnit 및 QueryDSL 페이징 로직 검증**
`src/test/java/com/example/pitchboxd/matchDetail/presentation/MatchDetailControllerTest.java`에 최신순 및 추천순 페이징 API 호출 시나리오 테스트를 작성하고 실행합니다.
Expected: PASS
- [ ] **Step 5: Commit**
```bash
git add src/main/java/com/example/pitchboxd/match/matchReview/infrastructure/MatchReviewQueryRepository.java src/main/java/com/example/pitchboxd/matchDetail/dto/response/MatchReviewDetailResponse.java src/main/java/com/example/pitchboxd/matchDetail/dto/response/MatchReviewSliceResponse.java src/main/java/com/example/pitchboxd/matchDetail/presentation/MatchDetailController.java src/main/java/com/example/pitchboxd/matchDetail/service/MatchDetailFacadeService.java
git commit (if auto_commit)
```
