# 2026-06-11 Match Detail Endpoints Design Spec

## 1. 개요 (Overview)
본 문서는 K리그 경기 리뷰 플랫폼 **Pitchboxd**의 경기 상세 화면에서 필요한 조회 API의 최종 설계 명세서입니다. 
성능 최적화와 글로벌 캐싱을 위해 공통 데이터(Static)와 로그인 유저 개인화 데이터(Personal), 핫 리뷰(Hot Reviews), 전체 리뷰 목록(Reviews List with Cursor Pagination)을 분리하여 제공합니다.

---

## 2. API 명세서 (API Specifications)

모든 API 응답은 프로젝트의 공통 응답 포맷인 [SuccessResponse](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/global/dto/response/SuccessResponse.java) 구조(`{ "status": 200, "data": { ... } }`)를 따릅니다.

### API 1. 경기 상세 공통 정적 정보 & 집단지성 통계
* **Endpoint**: `GET /api/v1/matches/{matchId}/detail/static`
* **설명**: 로그인 여부와 무관하게 조회 가능하며 Redis 캐싱 적용에 적합합니다.
* **응답 데이터 구조**:
  ```json
  {
    "status": 200,
    "data": {
      "seasonName": "2026 K리그1",
      "round": "15R",
      "startTime": "2026-06-10T19:00:00",
      "location": "상암 월드컵경기장",
      "homeTeam": {
        "id": 1,
        "name": "FC서울",
        "score": 2,
        "lineups": [
          { "playerId": 101, "name": "기성용", "number": 6, "position": "MF", "averageRating": 8.4 }
        ]
      },
      "awayTeam": {
        "id": 2,
        "name": "수원삼성",
        "score": 1,
        "lineups": [
          { "playerId": 201, "name": "김주찬", "number": 12, "position": "FW", "averageRating": 6.2 }
        ]
      },
      "matchAverageRating": 7.8,
      "homeFanAverageRating": 9.2,
      "awayFanAverageRating": 3.4,
      "neutralFanAverageRating": 8.1,
      "ratingDistribution": {
        "1": 5, "2": 2, "3": 10, "4": 15, "5": 20, "6": 45, "7": 120, "8": 240, "9": 110, "10": 35
      },
      "highlights": {
        "mom": { "playerId": 101, "name": "기성용", "averageRating": 8.4 },
        "top3": [
          { "playerId": 101, "name": "기성용", "averageRating": 8.4 },
          { "playerId": 102, "name": "일류첸코", "averageRating": 7.9 },
          { "playerId": 103, "name": "임상협", "averageRating": 7.5 }
        ]
      }
    }
  }
  ```

### API 2. 로그인 유저 개인화 데이터
* **Endpoint**: `GET /api/v1/matches/{matchId}/detail/personal`
* **설명**: 현재 로그인한 사용자가 경기 및 선수에 대해 남긴 평점/한줄평 정보를 조회합니다. 비로그인 상태일 때는 빈 응답 형태(`isEvaluated: false`)를 제공합니다.
* **응답 데이터 구조**:
  ```json
  {
    "status": 200,
    "data": {
      "isEvaluated": true,
      "myMatchReview": {
        "reviewId": 456,
        "rating": 8,
        "comment": "엄청난 명경기였습니다! 기성용 중거리 원더골 굿"
      },
      "myPlayerReviews": [
        { "playerReviewId": 789, "playerId": 101, "rating": 9, "comment": "MOM 급 활약" },
        { "playerReviewId": 790, "playerId": 102, "rating": 8, "comment": null }
      ]
    }
  }
  ```

### API 3. 핫한 경기 리뷰 (5개 단건 리스트)
* **Endpoint**: `GET /api/v1/matches/{matchId}/match-reviews/hot`
* **설명**: 해당 경기의 한줄평 중 추천(좋아요)이 가장 많은 5개의 핫 리뷰 목록을 조회합니다. 로그인 유저의 좋아요 상태(`isLiked`)와 본인 작성 여부(`isOwner`)를 포함합니다.
* **응답 데이터 구조**:
  ```json
  {
    "status": 200,
    "data": {
      "reviews": [
        {
          "reviewId": 12,
          "userId": 5,
          "nickname": "서울의기성용",
          "profileImage": "https://example.com/profiles/1.png",
          "fanType": "HOME",
          "point": 8,
          "content": "이것은 최고의 매치였습니다.",
          "likeCount": 150,
          "isLiked": true,
          "isOwner": false,
          "createdAt": "2026-06-10T21:05:00"
        }
      ]
    }
  }
  ```

### API 4. 전체 경기 리뷰 목록 (커서 기반 페이징)
* **Endpoint**: `GET /api/v1/matches/{matchId}/match-reviews`
* **설명**: 경기 상세 더보기 화면에서 무한 스크롤 형식으로 전체 리뷰를 최신순/추천순으로 제공합니다.
* **Query Parameters**:
  * `sort`: 정렬 기준 (enum: `LATEST`, `LIKE`, 기본값 `LATEST`)
  * `cursorId`: 페이징 기준이 되는 리뷰 ID (null 가능)
  * `cursorLikeCount`: 추천순 정렬 시 페이징 기준이 되는 좋아요 수 (sort가 `LIKE`인 경우 필수, null 가능)
  * `size`: 페이지당 반환할 개수 (기본값 10)
* **응답 데이터 구조**:
  ```json
  {
    "status": 200,
    "data": {
      "reviews": [
        {
          "reviewId": 11,
          "userId": 8,
          "nickname": "수원언제반등하냐",
          "profileImage": "https://example.com/profiles/2.png",
          "fanType": "AWAY",
          "point": 4,
          "content": "공격 전개가 너무 아쉽네요.",
          "likeCount": 85,
          "isLiked": false,
          "isOwner": false,
          "createdAt": "2026-06-10T21:02:00"
        }
      ],
      "nextCursorId": 10,
      "nextCursorLikeCount": 70,
      "hasNext": true
    }
  }
  ```

---

## 3. QueryDSL 커서 기반 페이징 로직 (Pagination Logic)

[MatchReviewQueryRepository](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/match/matchReview/infrastructure/MatchReviewQueryRepository.java)에서 다음 로직으로 커서 조건문을 빌드합니다.

### 3.1 최신순 정렬 (`LATEST`)
* **정렬 조건**: `matchReview.id DESC`
* **커서 조건**: `matchReview.id < :cursorId`
* **QueryDSL Where 조건**:
  ```java
  private BooleanExpression ltCursorId(Long cursorId) {
      return cursorId != null ? matchReview.id.lt(cursorId) : null;
  }
  ```

### 3.2 추천순 정렬 (`LIKE`)
* **정렬 조건**: `matchReview.likeCount DESC, matchReview.id DESC`
* **커서 조건**: `(likeCount < :cursorLikeCount) OR (likeCount = :cursorLikeCount AND id < :cursorId)`
* **QueryDSL Where 조건**:
  ```java
  private BooleanExpression ltCursorLike(Long cursorLikeCount, Long cursorId) {
      if (cursorLikeCount == null || cursorId == null) {
          return null;
      }
      return matchReview.likeCount.lt(cursorLikeCount)
              .or(matchReview.likeCount.eq(cursorLikeCount).and(matchReview.id.lt(cursorId)));
  }
  ```

---

## 4. 구현 및 아키텍처 흐름

```
[Client]
   │
   ├── 1. GET .../detail/static    ──> [MatchDetailController] ──> [MatchDetailFacadeService] ──> Redis / DB
   ├── 2. GET .../detail/personal  ──> [MatchDetailController] ──> [MatchDetailFacadeService] ──> DB (User-specific)
   ├── 3. GET .../match-reviews/hot ─> [MatchDetailController] ──> [MatchDetailFacadeService] ──> DB (Top 5 Likes)
   └── 4. GET .../match-reviews     ─> [MatchReviewQueryController] (신설) ──> [MatchReviewFacadeService] ──> QueryDSL (Cursor)
```

1. **`MatchDetailFacadeService` 확장**:
   * `getMatchStaticData()` 내부에 팀별/매치 평균 평점 계산, 삼색 통계 집계, 평점 분포 계산, MOM/Top3 선정 로직 통합.
   * `getMatchPersonalData(matchId, userId)` 구현. 로그인하지 않았을 경우 유연한 빈 데이터 처리.
2. **`MatchReviewQueryController` 신설**:
   * `/api/v1/matches/{matchId}/match-reviews` 엔드포인트를 신설하여 무한스크롤 목록 조회 담당.
3. **인증 처리**:
   * `GET /api/v1/matches/{matchId}/detail/static` 엔드포인트는 비로그인 호출 가능하도록 `SecurityConfig`에 등록.
   * `GET /api/v1/matches/{matchId}/detail/personal` 및 전체 리뷰 목록 조회 등은 로그인 유저가 존재하면 로그인 유저 정보를 주입받고(`@LoginUserId(required = false)`), 비로그인이면 `userId = null`로 유연하게 처리하여 유저 맞춤 데이터(좋아요 여부 등)를 바인딩합니다.

---

## 5. 테스트 계획 (Test Plan)
1. **단위 테스트**:
   * 커서 값이 Null일 때와 존재할 때 QueryDSL 쿼리가 올바른 range 조건을 주입하여 데이터를 조회해오는지 검증.
   * MOM 선정 시 동점자 발생 기준(평점 -> 좋아요 수 -> 최신 ID 역순)이 정상 작동하는지 비즈니스 규칙 테스트 작성.
2. **통합 테스트**:
   * 로그인 상태와 비로그인 상태 각각에 대해 `/detail/personal` 및 `/match-reviews` 호출 시 예외 없이 성공 응답 및 알맞은 `isLiked` 상태가 바인딩되는지 검증.
