# 디자인 스펙: 경기 상세 페이지 API 분리 (정적 결과 및 동적 통계 데이터)

## 1. 개요 (Overview)
현재 경기 상세 페이지의 정적 데이터 조회 API(`GET /api/v1/matches/{matchId}/detail/static`)는 경기 결과 및 라인업 정보(정적 데이터)와 경기 평점 통계, MOM, 별점 분포(동적 데이터)를 하나의 DTO(`MatchDetailResponse`)로 묶어 반환하고 있습니다.
이 구조는 동적 데이터의 변경(리뷰 작성으로 인한 실시간 평점 변동 등)으로 인해 정적 데이터까지 불필요하게 캐시에서 무효화(Eviction)되거나, 정적 데이터조차 짧은 TTL(Time-To-Live)을 적용받아 DB 커넥션을 낭비하는 캐싱 최적화의 한계를 야기합니다.
따라서, 두 데이터군을 별도의 API 엔드포인트와 DTO로 분리하여 향후 캐싱 전략(정적 정보 장기 캐싱, 동적 통계 단기 캐싱)을 효율적으로 적용할 수 있는 구조를 확립하고자 합니다.

## 2. 목표 (Goals & Non-Goals)
### 목표 (Goals)
* 기존의 정적 경기 정보 및 라인업 데이터를 반환하는 `/api/v1/matches/{matchId}/detail/result` API 생성
* 실시간 평점 통계, 별점 분포, MOM/Top3 데이터를 반환하는 `/api/v1/matches/{matchId}/detail/stats` API 신설
* 서비스 레이어(`MatchDetailFacadeService`)의 비즈니스 로직 및 DTO를 분리하여 단일 책임 원칙(SRP) 준수
* 기존 통합 API 테스트 코드를 분리된 API 사양에 맞춰 수정 및 보강
* **(중요)** 이번 단계에서는 실제 Redis 캐싱 처리는 하지 않으며, 캐싱을 적용하기 용이하도록 코드와 API를 분리하는 데 집중합니다.

### 비목표 (Non-Goals)
* Redis 캐시 어노테이션(`@Cacheable`) 또는 RedisTemplate을 사용한 실제 캐시 로직 구현 (추후 사용자가 직접 실습할 영역으로 남겨둠)
* 경기 리뷰 조회, 핫리뷰 조회, 개인 평가 정보 조회 API의 구조 변경

## 3. 상세 설계 (Detailed Design)

### 3.1 DTO 변경 및 추가

#### MatchDetailResultResponse (경기 정보 및 라인업 - 정적 데이터)
기존 `MatchDetailResponse`를 제거(또는 리팩토링)하고, 다음 DTO를 신설합니다.
```java
package com.example.pitchboxd.matchDetail.dto.response;

import java.time.LocalDateTime;

public record MatchDetailResultResponse(
        String seasonName,
        String round,
        LocalDateTime startTime,
        String location,
        String homeTeamName,
        String awayTeamName,
        Integer homeScore,
        Integer awayScore,
        LineupResponses homeLineups,
        LineupResponses awayLineups
) {}
```

#### MatchDetailStatsResponse (평점 및 통계 - 동적 데이터)
실시간 별점 분포 및 평점 데이터를 반환할 신규 DTO를 생성합니다.
```java
package com.example.pitchboxd.matchDetail.dto.response;

import java.util.List;
import java.util.Map;

public record MatchDetailStatsResponse(
        Double totalAverage,
        Double homeAverage,
        Double awayAverage,
        Map<Integer, Long> distributionMap,
        MatchHighlightsResponse highlights
) {
    public record HighlightPlayerResponse(
            Long playerId,
            String playerName,
            Double averageRating
    ) {}

    public record MatchHighlightsResponse(
            HighlightPlayerResponse mom,
            List<HighlightPlayerResponse> top3
    ) {}
}
```

### 3.2 컨트롤러 레이어 변경 (`MatchDetailController`)

* `GET /api/v1/matches/{matchId}/detail/static` 엔드포인트 **삭제**
* `GET /api/v1/matches/{matchId}/detail/result` 엔드포인트 **추가**
  * 반환 타입: `SuccessResponse<MatchDetailResultResponse>`
  * 호출 메서드: `matchDetailFacadeService.getMatchResultData(matchId)`
* `GET /api/v1/matches/{matchId}/detail/stats` 엔드포인트 **추가**
  * 반환 타입: `SuccessResponse<MatchDetailStatsResponse>`
  * 호출 메서드: `matchDetailFacadeService.getMatchStatsData(matchId)`

### 3.3 서비스 레이어 변경 (`MatchDetailFacadeService`)

#### `getMatchResultData(Long matchId)` 메서드 구현
```java
    public MatchDetailResultResponse getMatchResultData(Long matchId) {
        MatchDetailStaticModel matchDetail = matchQueryService.findMatchStaticDetailById(matchId);
        List<LineupPlayerModel> lineups = matchLineupQueryService.findLineupAndPlayedPlayers(matchId);

        List<PlayerStatistics> playerStats = playerStatisticsRepository.findAllByMatchId(matchId);
        Map<Long, Double> playerRatingsMap = playerStats.stream()
                .collect(Collectors.toMap(
                        PlayerStatistics::getPlayerId,
                        PlayerStatistics::getAverageRating,
                        (existing, replacement) -> existing
                ));

        List<LineupResponse> homeLineupResponses = lineups.stream()
                .filter(l -> l.teamId().equals(matchDetail.homeTeamId()))
                .map(l -> LineupResponse.of(l, playerRatingsMap.getOrDefault(l.playerId(), 0.0)))
                .toList();

        List<LineupResponse> awayLineupResponses = lineups.stream()
                .filter(l -> l.teamId().equals(matchDetail.awayTeamId()))
                .map(l -> LineupResponse.of(l, playerRatingsMap.getOrDefault(l.playerId(), 0.0)))
                .toList();

        return new MatchDetailResultResponse(
                matchDetail.seasonName(),
                matchDetail.round(),
                matchDetail.startTime(),
                matchDetail.location(),
                matchDetail.homeTeamName(),
                matchDetail.awayTeamName(),
                matchDetail.homeScore(),
                matchDetail.awayScore(),
                new LineupResponses(homeLineupResponses),
                new LineupResponses(awayLineupResponses)
        );
    }
```

#### `getMatchStatsData(Long matchId)` 메서드 구현
```java
    public MatchDetailStatsResponse getMatchStatsData(Long matchId) {
        List<LineupPlayerModel> lineups = matchLineupQueryService.findLineupAndPlayedPlayers(matchId);
        List<PlayerStatistics> playerStats = playerStatisticsRepository.findAllByMatchId(matchId);

        MatchStatistics matchStats = matchStatisticsRepository.findByMatchId(matchId)
                .orElse(new MatchStatistics(matchId));

        List<Object[]> rawDistribution = matchReviewRepository.countPointDistributionByMatchId(matchId);
        Map<Integer, Long> distributionMap = new HashMap<>();
        for (int i = 0; i <= 10; i++) {
            distributionMap.put(i, 0L);
        }
        for (Object[] row : rawDistribution) {
            Integer point = (Integer) row[0];
            Long count = (Long) row[1];
            if (point >= 0 && point <= 10) {
                distributionMap.put(point, count);
            }
        }

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
                        ps.getPlayerId(),
                        playerNamesMap.getOrDefault(ps.getPlayerId(), "Unknown Player"),
                        ps.getAverageRating()
                ))
                .toList();

        return new MatchDetailStatsResponse(
                matchStats.getTotalAverage(),
                matchStats.getHomeAverage(),
                matchStats.getAwayAverage(),
                distributionMap,
                new MatchDetailStatsResponse.MatchHighlightsResponse(mom, top3)
        );
    }
```

## 4. 검증 계획 (Verification Plan)

### 빌드 및 컴파일
* 전체 컴파일 및 테스트 빌드 오류 여부 검증
  * `./gradlew compileJava compileTestJava`

### 단위/통합 테스트 (`MatchDetailFacadeServiceTest.java`)
* 기존 `경기_상세_페이지_정적_데이터를_정확히_조회한다` 등의 통합 테스트 코드를 다음과 같이 2개로 분리하여 동작 및 응답의 무결성을 검증합니다.
  1. `경기_상세_결과와_라인업을_정확히_조회한다()`
  2. `경기_평점_통계와_수훈선수를_정확히_조회한다()`
* 전체 테스트 성공 검증:
  * `./gradlew test`
