# /users/me 엔드포인트에 favoriteTeamId 추가 구현 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**목표:** 로그인한 유저의 정보 조회 API (`/api/v1/users/me`) 응답(`UserResponse`)에 유저가 응원하는 팀의 ID(`favoriteTeamId`)를 추가합니다.

**아키텍처:** `UserResponse` 자바 레코드(record)에 `favoriteTeamId` 필드를 추가하고, `User` 엔티티의 `favoriteTeamId` 값을 가져오도록 `from` 정적 팩토리 메서드를 수정합니다. 테스트 코드를 업데이트하여 정상 작동하는지 검증합니다.

**기술 스택:** Java, Spring Boot, Spring Data JPA, RestAssured, JUnit 5

---

### Task 1: UserResponse DTO 수정

**대상 파일:**
- 수정: `src/main/java/com/example/pitchboxd/user/dto/response/UserResponse.java`

- [x] **Step 1: UserResponse 레코드 수정**
  레코드 필드에 `Long favoriteTeamId`를 추가하고, `from` 메서드에서도 `user.getFavoriteTeamId()`를 넘겨주도록 수정합니다.

  ```java
  package com.example.pitchboxd.user.dto.response;

  import com.example.pitchboxd.user.domain.User;

  public record UserResponse(Long id, String nickname, Long favoriteTeamId) {

      public static UserResponse from(User user) {
          return new UserResponse(user.getId(), user.getNickname(), user.getFavoriteTeamId());
      }
  }
  ```

- [x] **Step 2: 코드 컴파일 검증**
  실행: `./gradlew compileJava`
  예상 결과: 성공 (PASS)

- [x] **Step 3: 커밋 (auto_commit 활성화 시)**
  `.agent/config.yml`의 `auto_commit` 설정을 확인합니다.
  만약 `auto_commit: true` 인 경우:
  ```bash
  git add src/main/java/com/example/pitchboxd/user/dto/response/UserResponse.java
  git commit -m "feat: add favoriteTeamId to UserResponse"
  ```
  만약 `auto_commit: false` 인 경우: 커밋 및 스테이징 단계를 건너뛰고 "Skipping commit (auto_commit: false)." 문구를 출력합니다.


---

### Task 2: UserServiceTest 및 UserControllerTest 수정 및 테스트 실행

**대상 파일:**
- 수정: `src/test/java/com/example/pitchboxd/user/application/UserServiceTest.java`
- 수정: `src/test/java/com/example/pitchboxd/user/presentation/UserControllerTest.java`

- [x] **Step 1: UserServiceTest 수정**
  `유저_정보를_정상적으로_조회한다` 테스트 메서드를 수정하여 `favoriteTeamId`를 포함한 유저를 저장한 뒤, 조회된 `UserResponse`에 해당 ID가 잘 매핑되는지 검증합니다.

  ```java
      @Test
      void 유저_정보를_정상적으로_조회한다() {
          // given
          String nickname = "테스트유저";
          Long favoriteTeamId = 1L;
          User user = userRepository.save(new User(nickname, "test@example.com", "password123!", favoriteTeamId));

          // when
          UserResponse response = userService.getUserInfo(user.getId());

          // then
          assertAll(
                  () -> assertThat(user.getId()).isEqualTo(response.id()),
                  () -> assertThat(response.nickname()).isEqualTo(nickname),
                  () -> assertThat(response.favoriteTeamId()).isEqualTo(favoriteTeamId)
          );
      }
  ```

- [x] **Step 2: UserControllerTest 수정**
  `UserControllerTest.java`에 `UserRepository` 의존성을 주입하고, `내_정보를_조회한다` 테스트 메서드에서 `favoriteTeamId`가 지정된 유저를 DB에 저장한 뒤, `/api/v1/users/me` 엔드포인트를 호출했을 때 응답에 `favoriteTeamId`가 올바르게 반환되는지 확인합니다.

  - `UserRepository` 필드 추가:
    ```java
        @Autowired
        private UserRepository userRepository;
    ```
  - `내_정보를_조회한다` 테스트 수정:
    ```java
        @Test
        void 내_정보를_조회한다() {
            // given
            String username = "loggedInUser";
            String email = "loggedin@example.com";
            String password = "securepassword";
            Long favoriteTeamId = 1L;
            User user = userRepository.save(new User(username, email, password, favoriteTeamId));

            String accessToken = tokenManager.createAccessToken(user.getId(), email);

            // when
            UserResponse response = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + accessToken)
                    .when().get("/api/v1/users/me")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .extract()
                    .jsonPath()
                    .getObject("data", UserResponse.class);

            // then
            assertAll(
                    () -> assertThat(response.id()).isEqualTo(user.getId()),
                    () -> assertThat(response.nickname()).isEqualTo(username),
                    () -> assertThat(response.favoriteTeamId()).isEqualTo(favoriteTeamId)
            );
        }
    ```

- [x] **Step 3: UserServiceTest 실행**
  실행: `./gradlew test --tests com.example.pitchboxd.user.application.UserServiceTest`
  예상 결과: 성공 (PASS)

- [x] **Step 4: UserControllerTest 실행**
  실행: `./gradlew test --tests com.example.pitchboxd.user.presentation.UserControllerTest`
  예상 결과: 성공 (PASS)

- [x] **Step 5: 전체 프로젝트 테스트 실행**
  실행: `./gradlew test`
  예상 결과: 성공 (PASS)

- [x] **Step 6: 커밋 (auto_commit 활성화 시)**
  `.agent/config.yml`의 `auto_commit` 설정을 확인합니다.
  만약 `auto_commit: true` 인 경우:
  ```bash
  git add src/test/java/com/example/pitchboxd/user/application/UserServiceTest.java src/test/java/com/example/pitchboxd/user/presentation/UserControllerTest.java
  git commit -m "test: add tests for favoriteTeamId in users/me"
  ```
  만약 `auto_commit: false` 인 경우: 커밋 및 스테이징 단계를 건너뛰고 "Skipping commit (auto_commit: false)." 문구를 출력합니다.

