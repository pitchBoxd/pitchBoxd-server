# Spec: Match & Player Review Modified Status in Response

## 1. Overview
When a Match Review (`MatchReview`) or Player Review (`PlayerReview`) is updated, its `updatedAt` field is set to the current time. Currently, this modification status is not exposed in the query APIs. 
This specification outlines the changes required to include a boolean flag (`isModified`) in the response DTOs indicating whether the review has been edited.

## 2. Requirements & Scope
- Expose an `isModified` boolean field in the following response DTOs:
  - `MatchReviewDetailResponse`
  - `MyMatchReviewResponse`
  - `MatchDetailMatchReviewResponse` (Hot Review)
  - `MyPlayerReviewResponse`
- Set `isModified` to `true` if the review's `updatedAt` is not null; otherwise `false`.
- Ensure Querydsl projections (`HotReviewSummary`) pull the `updatedAt` status efficiently.
- Fix all affected tests in `MatchDetailControllerTest` and `MatchDetailFacadeServiceTest`.

## 3. Detailed Design

### 3.1. Entity Changes
Add a helper method `isUpdated()` (or `isModified()`) to `PlayerReview.java` similar to `MatchReview.java`.
```java
// PlayerReview.java
public boolean isUpdated() {
    return updatedAt != null;
}
```

### 3.2. DTO & Model Changes

#### **MatchReviewDetailResponse.java**
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

#### **MyMatchReviewResponse.java**
```java
public record MyMatchReviewResponse(
        Long reviewId,
        Integer rating,
        String comment,
        boolean isModified
) {}
```

#### **MatchDetailMatchReviewResponse.java**
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

#### **MyPlayerReviewResponse.java**
```java
public record MyPlayerReviewResponse(
        Long playerReviewId,
        Long playerId,
        Integer rating,
        String comment,
        boolean isModified
) {}
```

#### **HotReviewSummary.java (Repository projection model)**
Add `boolean isModified` to the constructor mapping.
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

### 3.3. Querydsl Repository Changes
Modify `MatchReviewQueryRepository.java` to project the `isModified` field.
```java
// MatchReviewQueryRepository.java
public List<HotReviewSummary> findHotReviewsByMatchId(Long matchId, int limit) {
    // ...
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
                    matchReview.updatedAt.isNotNull() // maps to isModified
            ))
            // ...
}
```

Apply the same projection mapping in `findHotReviewsByMatchIds` method.

### 3.4. Facade Service Mapping Changes
Update `MatchDetailFacadeService.java` to map `isModified`:
- In `getMatchPersonalData`:
  - `MyMatchReviewResponse` -> pass `matchReview.isUpdated()`
  - `MyPlayerReviewResponse` -> pass `playerReview.isUpdated()`
- In `getMatchReviews`:
  - `MatchReviewDetailResponse` -> pass `r.isUpdated()`

## 4. Verification Plan

### 4.1. Unit & Integration Tests
- Verify compilation of all modified classes.
- Update test fixtures in `MatchDetailControllerTest` and `MatchDetailFacadeServiceTest` to include the new fields.
- Assert that `isModified` is correctly populated as `true` when a review is updated, and `false` when it is newly created (i.e. `updatedAt == null`).
- Run `./gradlew test` to ensure all tests pass.
