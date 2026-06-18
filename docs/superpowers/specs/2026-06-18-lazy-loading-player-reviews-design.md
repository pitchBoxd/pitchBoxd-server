# Lazy Loading Player Reviews Design Spec

**Goal:** 경기 상세 화면에서 선수 카드를 클릭했을 때, 해당 선수의 상세 텍스트 리뷰 목록을 커서 기반 무한 스크롤로 지연 로딩(Lazy Loading)하는 API와 QueryDSL 기반 쿼리 레이어를 설계하고 구현합니다.

**Architecture:**
* **API Layer**: `MatchDetailController` 내에 특정 플레이어의 리뷰 페이징 목록을 반환하는 API 엔드포인트를 추가합니다.
* **Service Layer**: `PlayerReviewFacadeService`에서 지연 로딩 조회 흐름을 제어하고, DTO 변환 시 발생할 수 있는 N+1 쿼리 문제를 방지하기 위해 좋아요 상태 및 응원팀 이름을 벌크로 일괄 조회하여 매핑합니다.
* **Repository Layer**: QueryDSL을 활용해 `PlayerReviewQueryRepository`를 정의하고 커서 조건 및 정렬(최신순, 추천순) 동적 쿼리를 구현합니다.

**Tech Stack:** Java 17, Spring Boot, Spring Data JPA, QueryDSL, JUnit 5

---

## 1. Detailed API Specification

### Endpoint
* **HTTP Method**: `GET`
* **URL**: `/api/v1/matches/{matchId}/players/{playerId}/player-reviews`
* **Security**: Optional JWT (로그인하지 않은 사용자도 조회 가능, 로그인 시 본인의 좋아요 여부 반영)

### Request Parameters
| Name | Type | Required | Default | Description |
|---|---|---|---|---|
| `cursorId` | Long | No | - | 무한 스크롤 페이징용 다음 기준 리뷰 ID |
| `cursorLikeCount` | Long | No | - | 추천순 정렬 시 사용되는 다음 기준 좋아요 수 |
| `sort` | ReviewSortType | No | `LATEST` | 정렬 기준 (`LATEST`: 최신순, `LIKE`: 추천순) |
| `size` | Integer | No | `10` | 한 페이지에 조회할 리뷰 개수 |

### Response Schema (`PlayerReviewSliceResponse`)
```json
{
  "status": "SUCCESS",
  "code": 200,
  "message": "요청이 성공했습니다.",
  "data": {
    "reviews": [
      {
        "id": 12,
        "nickname": "축구마니아",
        "favoriteTeamName": "울산 HD FC",
        "point": 9,
        "content": "공수 양면에서 활동량이 엄청났습니다. 오늘 MOM급 활약이네요.",
        "fanType": "HOME",
        "likeCount": 15,
        "isLiked": true,
        "createdAt": "2026-06-18T17:00:00"
      }
    ],
    "nextCursorId": 11,
    "nextCursorLikeCount": 15,
    "hasNext": true
  }
}
```

---

## 2. Component Design

### 2.1. PlayerReviewQueryRepository (QueryDSL)
커서 기반 무한 스크롤 페이징 처리를 위해 `JPAQueryFactory`를 사용하여 조회합니다.
```java
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

        return query.limit(size + 1).fetch(); // hasNext 판단을 위해 size + 1 개 조회
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

### 2.2. Service & DTO Mapping Optimization
단순히 루프를 돌며 작성자의 응원 팀 및 좋아요 여부를 매 건마다 쿼리하면 N+1 성능 문제가 발생합니다. 이를 지양하고 일괄 조회(In-query) 방식을 적용합니다.
1. **좋아요 여부 (`isLiked`) 일괄 조회**:
   ```java
   // 로그인 유저가 존재할 경우
   List<Long> reviewIds = playerReviews.stream().map(PlayerReview::getId).toList();
   Set<Long> likedReviewIds = playerReviewLikeRepository.findLikedReviewIdsIn(reviewIds, loginUserId);
   ```
2. **응원 팀 이름 (`favoriteTeamName`) 일괄 조회**:
   * 작성자들의 `favoriteTeamId` 목록을 추출하여 `teamRepository.findAllById(teamIds)`로 조회한 뒤, `Map<Long, String>` 형태로 캐싱하여 DTO 매핑 시 활용합니다.

---

## 3. Testing Plan

### Unit/Slice Tests
* **PlayerReviewQueryRepositoryTest**:
  * `sort=LATEST` 시 커서 기반 페이징이 최신순으로 잘 작동하는지 검증
  * `sort=LIKE` 시 추천수가 같을 때 ID 역순으로 동적 정렬 및 페이징이 잘 작동하는지 검증
* **PlayerReviewFacadeServiceTest**:
  * 비로그인 유저 조회 시 `isLiked`가 모두 `false`로 제공되는지 검증
  * 로그인 유저 조회 시 자신이 좋아요를 누른 리뷰만 `isLiked=true`로 매핑되는지 검증 (N+1 쿼리 최적화 확인)

### Integration Tests
* **MatchDetailControllerTest**:
  * `GET /api/v1/matches/{matchId}/players/{playerId}/player-reviews` API 호출 결과가 페이징 구조로 정상 반환되는지 확인
