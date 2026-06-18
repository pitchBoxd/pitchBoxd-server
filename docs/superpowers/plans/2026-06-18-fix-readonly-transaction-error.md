# Fix Read-Only Transaction Error Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** `AdminFacadeService.autoFinishMatchesAndUpdateLineup` 실행 중 발생하는 `Connection is read-only` 트랜잭션 에러를 해결하고, 외부 API 통신 시 불필요한 DB 커넥션 점유 문제를 해소합니다.

**Architecture:** 
1. `AdminFacadeService` 클래스는 주로 어드민 생성/수정/동기화(쓰기 위주) 작업을 처리하는 클래스이나, 클래스 레벨에 `@Transactional(readOnly = true)`가 잘못 선언되어 있어 산하의 쓰기 메소드들이 읽기 전용 트랜잭션으로 구동됩니다.
2. 따라서 클래스 레벨의 `@Transactional(readOnly = true)` 어노테이션을 **제거**하여 개별 메소드들의 `@Transactional`이 정상적으로 쓰기(Writable) 트랜잭션으로 작동하게 만듭니다.
3. 또한 외부 API를 연쇄적으로 호출하는 대형 제어 메소드인 `autoFinishMatchesAndUpdateLineup` 메소드에서 `@Transactional` 어노테이션을 제거하여, 불필요한 데이터베이스 커넥션 점유를 막고 하위 갱신 메소드(`self.finishMatchAndUpdateLineup`) 단위로 개별 트랜잭션이 수행되도록 변경합니다.

**Tech Stack:** Java, Spring Boot, Spring Data JPA

---

### Task 1: `AdminFacadeService` 트랜잭션 어노테이션 수정

**Files:**
- Modify: [AdminFacadeService.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/admin/service/facade/AdminFacadeService.java)

- [ ] **Step 1: Write the failing test**
[AdminFacadeServiceTest.java](file:///Users/yuseokhyeon/pitchboxd/src/test/java/com/example/pitchboxd/admin/service/facade/AdminFacadeServiceTest.java)에 `autoFinishMatchesAndUpdateLineup_success` 통합 테스트를 작성하여 캐시 비우기(`em.clear()`) 후 경기 상태가 `FINISHED`로 바뀌어 정상 저장되는지 확인합니다.

- [ ] **Step 2: Run test to verify it fails**
(이미 이전 단계에서 예외 삼킴 현상으로 인해 Assert가 SCHEDULED 상태로 실패하는 것을 확인했습니다).

- [ ] **Step 3: Modify implementation**
[AdminFacadeService.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/admin/service/facade/AdminFacadeService.java)를 다음과 같이 수정합니다.
* 클래스 레벨의 `@Transactional(readOnly = true)` 어노테이션을 제거합니다.
* `autoFinishMatchesAndUpdateLineup` 메소드에 붙어있던 `@Transactional` 어노테이션을 제거합니다 (이미 제거 완료).

- [ ] **Step 4: Run test to verify it passes**
Run: `./gradlew test --tests "*AdminFacadeServiceTest*"`
Expected: PASS

- [ ] **Step 5: Commit**
Stage and commit changes if `auto_commit` is enabled.
