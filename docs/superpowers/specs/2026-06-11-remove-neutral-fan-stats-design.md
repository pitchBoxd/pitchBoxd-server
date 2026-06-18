# Design Spec: Remove Neutral Fan Average Rating from Match Detail Statistics

## Objective
Modify the Match Detail Static API to exclude neutral fan rating calculations and only output statistics based on total (all) fans, home fans, and away fans.

## Proposed Changes

### 1. MatchDetailResponse DTO
Modify [MatchDetailResponse.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/matchDetail/dto/response/MatchDetailResponse.java):
* Remove `Double neutralFanAverageRating` field from the record definition.

### 2. MatchDetailFacadeService
Modify [MatchDetailFacadeService.java](file:///Users/yuseokhyeon/pitchboxd/src/main/java/com/example/pitchboxd/matchDetail/service/MatchDetailFacadeService.java):
* Remove the variables `neutralCount`, `neutralSum`, and `neutralAverage` calculation inside `getMatchStaticData`.
* Remove the `neutralAverage` argument from `MatchDetailResponse` instantiation.

### 3. MatchDetailFacadeServiceTest
Modify [MatchDetailFacadeServiceTest.java](file:///Users/yuseokhyeon/pitchboxd/src/test/java/com/example/pitchboxd/matchDetail/service/MatchDetailFacadeServiceTest.java):
* Rename `경기의_정적_데이터_조회_시_중립팬_평균평점과_평점분포도가_올바르게_계산된다` to `경기의_정적_데이터_조시_평균평점들과_평점분포도가_올바르게_계산된다`.
* Remove the assertion:
  ```java
  assertThat(result.neutralFanAverageRating()).isEqualTo(3.5);
  ```

## Verification Plan
1. Compile the project: `./gradlew compileJava compileTestJava`
2. Run unit and integration tests: `./gradlew test`
3. Verify all tests pass.
