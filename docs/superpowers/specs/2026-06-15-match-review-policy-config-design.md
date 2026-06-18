# 2026-06-15 Match Review Policy Configuration Design

This design spec details how to externalize the hardcoded match review availability policy duration limit in PitchBoxd and apply different limits for `local`, `prod`, and `test` environments.

## Problem Statement
Currently, the match review submit limit is hardcoded as a `private static final Duration REVIEW_SUBMIT_LIMIT = Duration.ofHours(48);` inside [MatchReviewSubmitPolicy.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/match/matchReview/domain/MatchReviewSubmitPolicy.java).
This makes it difficult to test match reviews locally because local match data might be older than 48 hours, forcing developers to constantly update match dates in the database.

## Requirements
- Support environment-specific configuration limits for match review submissions.
- `local` environment: 30 days (`30d`).
- `prod` environment: 48 hours (`48h`).
- `test` environment: 48 hours (`48h`).

## Proposed Design

### 1. Configuration Changes

#### Local Configuration
Add configuration to [application-local.yaml](file:///Users/yuseokhyeon/pitchboxd/src/main/resources/application-local.yaml):
```yaml
app:
  policy:
    match-review-limit: 30d
```

#### Prod Configuration
Add configuration to [application-prod.yaml](file:///Users/yuseokhyeon/pitchboxd/src/main/resources/application-prod.yaml):
```yaml
app:
  policy:
    match-review-limit: 48h
```

#### Test Configuration
Add configuration to [application-test.yaml](file:///Users/yuseokhyeon/pitchboxd/src/test/resources/application-test.yaml):
```yaml
app:
  policy:
    match-review-limit: 48h
```

### 2. Code Changes
Modify [MatchReviewSubmitPolicy.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/match/matchReview/domain/MatchReviewSubmitPolicy.java) to load the property via `@Value`:

```java
package com.example.pitchboxd.match.matchReview.domain;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.match.core.domain.Match;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MatchReviewSubmitPolicy {

    private final Duration reviewSubmitLimit;

    public MatchReviewSubmitPolicy(
        @Value("${app.policy.match-review-limit:48h}") Duration reviewSubmitLimit
    ) {
        this.reviewSubmitLimit = reviewSubmitLimit;
    }

    public void validateMatchStatus(Match match, LocalDateTime now) {
        if (!match.isEnd(now) || match.isPassed(now, reviewSubmitLimit)) {
            throw new BusinessException(ErrorCode.MATCH_REVIEW_INVALID_REVIEW_TIME);
        }
    }

    public void validateUserCondition(boolean isAlreadyReviewed) {
        if (isAlreadyReviewed) {
            throw new BusinessException(ErrorCode.MATCH_REVIEW_ALREADY_REVIEWED);
        }
    }
    
    public LocalDateTime getReviewableThreshold(LocalDateTime now) {
        return now.minus(reviewSubmitLimit);
    }
}
```

## Verification Plan
1. Check if the project compiles successfully.
2. Run existing tests (`./gradlew test`) to verify that nothing is broken, especially in test profile since it retains `48h`.
