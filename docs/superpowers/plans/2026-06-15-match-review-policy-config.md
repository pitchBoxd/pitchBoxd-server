# Match Review Policy Configuration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Externalize the hardcoded match review availability duration limit to allow different limits across environments (`local`, `prod`, `test`).

**Architecture:** Inject `Duration` value using Spring `@Value` annotation from `application.yaml` files to decouple environment configuration from code.

**Tech Stack:** Spring Boot 3.5.x, Java 21, Gradle

---

### Task 1: Add Configuration Properties to YAML Files

**Files:**
- Modify: `src/main/resources/application-local.yaml`
- Modify: `src/main/resources/application-prod.yaml`
- Modify: `src/test/resources/application-test.yaml`

- [ ] **Step 1: Add configuration properties to application-local.yaml**

Modify [application-local.yaml](file:///Users/yuseokhyeon/pitchboxd/src/main/resources/application-local.yaml) to add:
```yaml
app:
  policy:
    match-review-limit: 30d
```

- [ ] **Step 2: Add configuration properties to application-prod.yaml**

Modify [application-prod.yaml](file:///Users/yuseokhyeon/pitchboxd/src/main/resources/application-prod.yaml) to add:
```yaml
app:
  policy:
    match-review-limit: 48h
```

- [ ] **Step 3: Add configuration properties to application-test.yaml**

Modify [application-test.yaml](file:///Users/yuseokhyeon/pitchboxd/src/test/resources/application-test.yaml) to add:
```yaml
app:
  policy:
    match-review-limit: 48h
```

- [ ] **Step 4: Commit (if auto_commit enabled)**

Check `.agent/config.yml` for `auto_commit` setting.
If `auto_commit: true`:
```bash
git add src/main/resources/application-local.yaml src/main/resources/application-prod.yaml src/test/resources/application-test.yaml
git commit -m "config: add environment-specific match review limits"
```
If `auto_commit: false`: skip commit and staging. Print: "Skipping commit (auto_commit: false)."

---

### Task 2: Inject Property into MatchReviewSubmitPolicy

**Files:**
- Modify: `src/main/java/com/example/pitchboxd/match/matchReview/domain/MatchReviewSubmitPolicy.java`

- [ ] **Step 1: Inject duration limit property via constructor `@Value`**

Modify [MatchReviewSubmitPolicy.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/match/matchReview/domain/MatchReviewSubmitPolicy.java) to load `app.policy.match-review-limit` dynamically:

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

- [ ] **Step 2: Commit (if auto_commit enabled)**

Check `.agent/config.yml` for `auto_commit` setting.
If `auto_commit: true`:
```bash
git add src/main/java/com/example/pitchboxd/match/matchReview/domain/MatchReviewSubmitPolicy.java
git commit -m "feat: inject match review limit configuration"
```
If `auto_commit: false`: skip commit and staging. Print: "Skipping commit (auto_commit: false)."

---

### Task 3: Verification and Tests

**Files:**
- Test: Run Gradle tests to verify correctness.

- [ ] **Step 1: Run project build and tests**

Run command:
```bash
./gradlew test
```
Expected: All tests pass, verifying that the Spring Boot test context loads the `application-test.yaml` property correctly and the existing logic functions as expected.

- [ ] **Step 2: Commit (if auto_commit enabled)**

Check `.agent/config.yml` for `auto_commit` setting.
If `auto_commit: true`:
```bash
# No files modified in this step, but commit if any cleanup was done.
```
If `auto_commit: false`: skip commit and staging. Print: "Skipping commit (auto_commit: false)."
