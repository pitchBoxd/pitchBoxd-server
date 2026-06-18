# 2026-06-10 Match Detail API Design Spec

## 1. 개요 (Overview)
본 문서는 Pitchboxd 서비스의 경기 상세 화면에서 사용자에게 풍부한 집단지성 데이터와 편리한 평가 기능을 제공하기 위한 API 설계 명세서입니다. 
성능 최적화(N+1 방지 및 캐싱)와 객체지향적인 책임 분리를 위해 **하이브리드 BFF 구조(Hybrid BFF Architecture)**를 채택하여 설계합니다.

---

## 2. 핵심 비즈니스 가치 (Core Business Value)
1. **집단지성 평점 통계 제공**: 팬들이 직접 평가한 경기 및 양 팀 선수들의 평균 평점, 오늘의 MOM(Man of the Match) 및 Top 3 수훈 선수를 직관적으로 노출합니다.
2. **소통과 감정 공유**: 경기 및 선수에 대해 남긴 한줄평(선택 사항)을 노출하여 리액션을 공유합니다.
3. **참여 유도 및 유연성**: 경기에 대한 평가 여부와 무관하게, 사용자가 원하는 선수들만 개별적으로 즉시 평가할 수 있도록 제약 조건(Validation Constraint)을 없애 참여 장벽을 낮춥니다.

---

## 3. 아키텍처 구성 및 API 명세 (API Specifications)

화면을 구성하는 리소스의 성격(공통 정적/통계 데이터 vs 로그인 유저 개인 데이터)에 따라 **2개의 API로 분리**합니다.

```
                    ┌──────────────────────────────┐
                    │     Client (Frontend)        │
                    └──────────────┬───────────────┘
                                   │
            ┌──────────────────────┴──────────────────────┐
    (API 1: 공통 및 통계)                               (API 2: 로그인 유저 개인화)
GET /detail/static                                   GET /detail/personal
            │                                             │
            ▼                                             ▼
┌──────────────────────────────────────┐      ┌──────────────────────────────────────┐
│        HomeFacadeService             │      │        HomeFacadeService             │
│ (Redis 캐싱 가능 / 모든 사용자 공통)    │      │ (No Cache / 로그인 사용자 세션 필요)   │
└──────────────────────────────────────┘      └──────────────────────────────────────┘
```

### API 1: 공통 정적 정보 & 집단지성 통계
* **Endpoint**: `GET /api/v1/matches/{matchId}/detail/static`
* **설명**: 로그인 여부와 무관하게 모든 사용자에게 동일하게 노출되는 데이터입니다. 데이터가 변하지 않는 정적 성격을 가지므로 백엔드 캐싱이 용이합니다.
* **응답 포맷 (Response Body JSON)**:
  ```json
  {
    "code": "SUCCESS",
    "message": "요청이 성공적으로 처리되었습니다.",
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

### API 2: 로그인 유저 개인화 데이터
* **Endpoint**: `GET /api/v1/matches/{matchId}/detail/personal`
* **설명**: 현재 로그인한 사용자가 작성한 평가 상태입니다. 로그인 상태에서만 호출되며 캐싱되지 않습니다.
* **응답 포맷 (Response Body JSON)**:
  ```json
  {
    "code": "SUCCESS",
    "message": "요청이 성공적으로 처리되었습니다.",
    "data": {
      "isEvaluated": true,
      "myMatchRating": 8,
      "myMatchComment": "엄청난 명경기였습니다! 기성용 중거리 원더골 굿",
      "myPlayerRatings": [
        { "playerId": 101, "rating": 9, "comment": "MOM 급 활약" },
        { "playerId": 102, "rating": 8, "comment": null }
      ]
    }
  }
  ```

---

## 4. 예외 처리 및 비즈니스 규칙 (Business Rules & Validation)
1. **평가 자율성 보장 (No Constraint)**:
   * 유저가 경기 평점(`myMatchRating`)을 매기지 않았더라도, 특정 선수 평점(`myPlayerRatings`)을 자유롭게 등록할 수 있습니다.
   * 마찬가지로 특정 선수 평가 없이 경기 평점만 등록하는 것도 허용됩니다.
2. **평점 입력 범위 검증**:
   * 경기 및 선수 평점은 `0`이상 `10`이하의 정수(또는 0.5 단위 실수) 범위 내에서만 등록 가능합니다.
3. **MOM 선정 알고리즘**:
   * 집계된 선수의 평균 평점이 가장 높은 선수를 `mom`으로 선정하되, 동점자 발생 시 평가 참여 수(투표 수)가 더 많은 선수를 우선하고, 그마저 동일하면 선수 ID 역순(최신 등록 선수)으로 정렬하여 1명을 단독 MOM으로 지정합니다.

---

## 5. 테스트 계획 (Testing Plan)
* **단위 테스트 (Unit Test)**:
  * `HotReviews` 일급 컬렉션이 다수의 선수 평점 목록 중 MOM 및 Top 3를 규칙에 맞게 올바르게 정렬하고 추출해내는지 검증합니다.
  * 경기 미평가 상태에서 선수 평가를 남길 때 Validation이 통과하는지 비즈니스 규칙을 검증합니다.
* **통합 테스트 (Integration Test)**:
  * 비로그인 유저가 `/personal` API를 조회할 시 인증 예외가 발생하지 않고, 비어 있는 기본 객체(`isEvaluated: false`)를 유연하게 응답하는지 검증합니다.
  * `/detail/static` API 호출 시 MOM 및 Top 3 정보가 올바르게 맵핑되어 응답 본문에 탑재되는지 REST Assured를 통해 검증합니다.
