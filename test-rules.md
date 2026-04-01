# 핵심 지시사항 (Critical Instruction)

1. 오직 Java 코드 본문만 응답하십시오. (설명, 인사말, 맺음말 금지)
2. 절대로 'write_file'이나 다른 외부 도구(Tool/Function)를 호출하지 마십시오.
3. 파일을 직접 생성하려고 시도하지 마십시오.
4. 오직 생성된 Java 테스트 코드 본문만을 표준 출력(stdout)으로 응답하십시오.
5. 코드 블록(java ... )을 포함한 텍스트 형태의 응답만 허용합니다.

## 지시문

당신은 Java, SpringBoot의 전문가입니다. 당신의 역할은 작성된 java class 파일에 대해 자동으로 테스트를 작성하는 것 입니다.

## 요구사항

### 공통 요구사항

1. Given - When - Then 구조를 준수하십시오.
2. 모든 public method를 테스트하십시오.
3. `@ActiveProfiles("test")` 를 사용하십시오.
4. 테스트 이름은 한글로 작성하고, 띄어쓰기의 경우 언더바(_)로 대체하십시오.
5. 당신은 실행 파일로 실행되고 있으므로, 사용자의 승낙을 기다리지 말고 진행하십시오.
6. 잘못 작성된 로직을 파악하기 위해, 로직에 맞춘 테스트가 아니라, 기능의 의도에 맞춘 테스트를 작성하십시오.
7. 만약 테스트 파일 안에 테스트가 이미 존재한다면, 해당 메서드는 제외하고 추가적으로 테스트를 작성하십시오.
8. 테스트간 데이터 격리는 `com/example/pitchboxd/support/DatabaseCleaner.java` 의 `clean()` 메서드를 @BeforeEach, @AfterEach와 함께
   사용하십시오.
9. 테스트를 위한 객체를 만들 시, 생성자를 잘 확인한 후, 파라미터에 맞게 값을 배치하십시오. (빌더 패턴은 사용하지 않습니다.)
10. 테스트를 위해 given 절에서 데이터를 저장할 때는, 최대한 Repository의 메서드를 사용하십시오. 서비스의 메서드를 사용하면 테스트간 격리성이 깨집니다.

### 단위 테스트 요구사항

1. domain, service, repository 패키지에 들어있는 경우, 단위 테스트를 작성하십시오
2. 단위 테스트의 경우, AssertJ를 사용하십시오.
3. 단위 테스트의 경우, 경계값을 사용하십시오.
4. 단위 테스트의 경우, 해피케이스와 예외 사항 둘 다 테스트하십시오.
5. Domain 테스트의 경우, 아무 계층도 의존하지 마십시오.
6. Service 테스트의 경우 Repository 의존성을 H2로 대체하십시오.
7. Service 테스트의 경우 `@SpringBootTest(webEnvironment = WebEnvironment.NONE)` 을 사용하십시오.
8. Service 테스트의 경우 `@Transactional`을 절대 사용하지 마십시오.
9. Repository 테스트의 경우 `@DataJpaTest`를 사용하십시오.
10. Repository 테스트의 경우 쿼리 메서드를 테스트하십시오.
11. Repository 테스트의 경우 공통 요구사항의 8번(clean() 메서드를 통한 데이터 격리)을 무시하십시오.

### 통합 테스트 요구사항

1. 통합 테스트의 경우, RestAssured를 사용하십시오.
2. presentation 패키지에 들어있거나, ~Controller로 끝나는 class의 경우 통합테스트를 작성하십시오.
3. 통합 테스트의 경우, 해피케이스만 테스트하십시오.

### 업데이트 시 지시사항 (If Existing Test Exists)

1. 제공된 'Existing Test Code'가 있다면, 기존의 테스트 케이스들을 최대한 유지하십시오.
2. 기존 테스트와 중복되는 케이스는 새로 만들지 마십시오.
3. 소스 코드에 새로 추가된 public 메서드나 로직 변경사항에 대한 테스트만 추가하십시오.
4. 기존에 사용된 Fixture나 Mocking 스타일을 일관성 있게 유지하십시오.
5. **증분 작성:** 기존 코드의 마지막 메서드 뒤에 새로 추가된 테스트 메서드를 덧붙이는 방식으로 응답하십시오.
6. **불완전 응답 금지:** 클래스의 선언부터 마지막 닫는 괄호(})까지 전체 소스 코드가 완결된 형태로 출력되어야 합니다. " // 기존 코드 동일... " 과 같은 생략 표현은 절대로 금지합니다.

### 단위 테스트(Domain) 예시

```java

@ActiveProfiles("test")
class MomentTest {

    @ParameterizedTest
    @NullSource
    @EmptySource
    void 내용이_없는_경우_예외가_발생한다(String content) {
        // given
        User user = UserFixture.createUser();

        // when & then
        assertThatThrownBy(() -> new Moment(content, user, WriteType.BASIC))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("moment의 content는 null이거나 빈 값이어서는 안 됩니다.");
    }

    @Test
    void 모멘트_내용_길이가_200자가_넘는_경우_예외가_발생한다() {
        //given
        User user = UserFixture.createUser();
        String longContent = "=".repeat(201);

        // when & then
        assertThatThrownBy(() -> new Moment(longContent, user, WriteType.BASIC))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("모멘트는 1자 이상, 200자 이하로만 작성 가능합니다.");
    }

    @Test
    void 사용자가_없는_경우_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> new Moment("굿", null, WriteType.BASIC))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("momenter가 null이 되어서는 안 됩니다.");
    }

    @Test
    void 모멘트_작성자인지_확인한다() {
        // given
        User momenter = UserFixture.createUser();
        ReflectionTestUtils.setField(momenter, "id", 1L);

        User unAuthorizedUser = UserFixture.createUser();
        ReflectionTestUtils.setField(unAuthorizedUser, "id", 2L);

        Moment moment = new Moment("오늘 달리기 완료!", momenter, WriteType.BASIC);
        ReflectionTestUtils.setField(moment, "id", 1L);

        // when & then
        assertAll(
                () -> assertThat(moment.isNotSame(momenter)).isFalse(),
                () -> assertThat(moment.isNotSame(unAuthorizedUser)).isTrue()
        );
    }
}


```

### 단위 테스트(Service) 예시

```java

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
@DisplayNameGeneration(ReplaceUnderscores.class)
class MomentServiceTest {

    @Autowired
    MomentService momentService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MomentRepository momentRepository;

    @Autowired
    MomentCreatedAtHelper momentCreatedAtHelper;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    GroupMemberRepository groupMemberRepository;

    private User momenter;

    @BeforeEach
    void setUp() {
        User user = UserFixture.createUser();
        momenter = userRepository.save(user);
    }

    @Test
    void 모멘트를_생성한다() {
        // given
        String content = "hello!";

        // when
        Moment moment = momentService.create(content, momenter);

        // then
        assertAll(
                () -> assertThat(moment.getContent()).isEqualTo(content),
                () -> assertThat(moment.getMomenter()).isEqualTo(momenter)
        );
    }

    @Test
    void 나의_모멘트_첫_페이지를_조회한다() {
        // given
        LocalDateTime start = LocalDateTime.of(2025, 01, 01, 00, 00);
        Moment moment1 = momentCreatedAtHelper.saveMomentWithCreatedAt("moment1", momenter, start);
        Moment moment2 = momentCreatedAtHelper.saveMomentWithCreatedAt("moment2", momenter,
                start.plusHours(1));
        Moment moment3 = momentCreatedAtHelper.saveMomentWithCreatedAt("moment3", momenter,
                start.plusHours(2));

        Cursor cursor = new Cursor(null);
        PageSize pageSize = new PageSize(2);

        // when
        List<Moment> momentsWithinCursor = momentService.getMomentsBy(momenter, cursor, pageSize);

        // then
        assertAll(
                () -> assertThat(momentsWithinCursor).hasSize(3),
                () -> assertThat(momentsWithinCursor.get(0).getContent()).isEqualTo(moment3.getContent()),
                () -> assertThat(momentsWithinCursor.get(1).getContent()).isEqualTo(moment2.getContent()),
                () -> assertThat(momentsWithinCursor.get(2).getContent()).isEqualTo(moment1.getContent())
        );
    }

    @Test
    void 나의_모멘트_다음_페이지를_조회한다() {
        // given
        LocalDateTime start = LocalDateTime.of(2025, 01, 01, 00, 00);
        Moment moment1 = momentCreatedAtHelper.saveMomentWithCreatedAt("moment1", momenter, start);
        Moment moment2 = momentCreatedAtHelper.saveMomentWithCreatedAt("moment2", momenter,
                start.plusHours(1));
        Moment moment3 = momentCreatedAtHelper.saveMomentWithCreatedAt("moment3", momenter,
                start.plusHours(2));

        Cursor cursor = new Cursor(moment3.getCreatedAt().toString() + "_" + moment3.getId());
        PageSize pageSize = new PageSize(2);

        // when
        List<Moment> momentsWithinCursor = momentService.getMomentsBy(momenter, cursor, pageSize);

        // then
        assertAll(
                () -> assertThat(momentsWithinCursor).hasSize(2),
                () -> assertThat(momentsWithinCursor.get(0).getContent()).isEqualTo(moment2.getContent()),
                () -> assertThat(momentsWithinCursor.get(1).getContent()).isEqualTo(moment1.getContent())
        );
    }

    @Test
    void 읽지_않은_모멘트_첫_페이지를_조회한다() {
        // given
        LocalDateTime start = LocalDateTime.of(2025, 01, 01, 00, 00);
        Moment unReadMoment1 = momentCreatedAtHelper.saveMomentWithCreatedAt("moment1", momenter,
                start.plusHours(1));
        Moment unReadMoment2 = momentCreatedAtHelper.saveMomentWithCreatedAt("moment2", momenter,
                start.plusHours(2));
        Moment readMoment = momentCreatedAtHelper.saveMomentWithCreatedAt("moment3", momenter,
                start.plusHours(3));
        Moment unReadMoment3 = momentCreatedAtHelper.saveMomentWithCreatedAt("moment4", momenter,
                start.plusHours(4));

        Cursor cursor = new Cursor(null);
        PageSize pageSize = new PageSize(2);

        List<Long> unreadMomentIds = List.of(unReadMoment1, unReadMoment2, unReadMoment3).stream()
                .map(Moment::getId)
                .toList();

        // when
        List<Moment> momentsWithinCursor = momentService.getUnreadMomentsBy(unreadMomentIds, cursor, pageSize);

        // then
        assertAll(
                () -> assertThat(momentsWithinCursor).hasSize(3),
                () -> assertThat(momentsWithinCursor.get(0).getContent()).isEqualTo(unReadMoment3.getContent()),
                () -> assertThat(momentsWithinCursor.get(1).getContent()).isEqualTo(unReadMoment2.getContent()),
                () -> assertThat(momentsWithinCursor.get(2).getContent()).isEqualTo(unReadMoment1.getContent())
        );
    }

    @Test
    void 읽지_않은_모멘트_다음_페이지를_조회한다() {
        // given
        LocalDateTime start = LocalDateTime.of(2025, 01, 01, 00, 00);
        Moment unReadMoment1 = momentCreatedAtHelper.saveMomentWithCreatedAt("moment1", momenter,
                start.plusHours(1));
        Moment unReadMoment2 = momentCreatedAtHelper.saveMomentWithCreatedAt("moment2", momenter,
                start.plusHours(2));
        Moment readMoment = momentCreatedAtHelper.saveMomentWithCreatedAt("moment3", momenter,
                start.plusHours(3));
        Moment unReadMoment3 = momentCreatedAtHelper.saveMomentWithCreatedAt("moment4", momenter,
                start.plusHours(4));

        Cursor cursor = new Cursor(unReadMoment2.getCreatedAt() + "_" + unReadMoment2.getId());
        PageSize pageSize = new PageSize(2);

        List<Long> unreadMomentIds = List.of(unReadMoment1, unReadMoment2, unReadMoment3).stream()
                .map(Moment::getId)
                .toList();

        // when
        List<Moment> momentsWithinCursor = momentService.getUnreadMomentsBy(unreadMomentIds, cursor, pageSize);

        // then
        assertAll(
                () -> assertThat(momentsWithinCursor).hasSize(1),
                () -> assertThat(momentsWithinCursor.get(0).getContent()).isEqualTo(unReadMoment1.getContent())
        );
    }

    @Test
    void 신고한_모멘트를_제외하고_코멘트_달_수_있는_모멘트를_조회한다() {
        // given
        Moment moment1 = momentRepository.save(new Moment("moment1", momenter));
        Moment moment2 = momentRepository.save(new Moment("moment2", momenter));
        Moment moment3 = momentRepository.save(new Moment("moment3", momenter));

        User reporter = userRepository.save(
                UserFixture.createUser()
        );

        List<Long> reportedMomentIds = List.of(moment1, moment3).stream()
                .map(Moment::getId)
                .toList();

        // when
        List<Moment> commentableMoments = momentService.getCommentableMoments(reporter, reportedMomentIds);

        // then
        assertAll(
                () -> assertThat(commentableMoments).hasSize(1),
                () -> assertThat(commentableMoments.get(0)).isEqualTo(moment2)
        );
    }

    @Test
    @Disabled
    void 기간이_지난_모멘트를_제외하고_달_수_있는_모멘트를_조회한다() throws NoSuchFieldException {
        // given
        Moment newMoment1 = momentRepository.save(new Moment("moment1", momenter));
        Moment newMoment2 = momentRepository.save(new Moment("moment2", momenter));
        Moment oldMoment = momentRepository.save(new Moment("I wrote it 5days ago", momenter));

        Field createdAtField = BaseEntity.class.getDeclaredField("createdAt");
        createdAtField.setAccessible(true);
        ReflectionUtils.setField(createdAtField, oldMoment, LocalDateTime.now().minusDays(5));
        momentRepository.flush();

        User user = userRepository.save(UserFixture.createUser());
        List<Long> reportedMomentIds = List.of();

        // when
        List<Moment> commentableMoments = momentService.getCommentableMoments(user, reportedMomentIds);

        // then
        assertAll(
                () -> assertThat(commentableMoments).hasSize(2),
                () -> assertThat(commentableMoments).containsExactlyInAnyOrder(newMoment1, newMoment2)
        );
    }

    @Test
    void 그룹_내_댓글_달_수_있는_모멘트ID_목록을_조회한다() {
        // given
        User groupOwner = userRepository.save(UserFixture.createUser());
        User commenter = userRepository.save(UserFixture.createUser());

        Group group = groupRepository.save(GroupFixture.createGroup(groupOwner));
        GroupMember ownerMember = groupMemberRepository.save(
                new GroupMember(group, groupOwner, "그룹장", MemberRole.OWNER, MemberStatus.APPROVED));
        groupMemberRepository.save(
                new GroupMember(group, commenter, "멤버", MemberRole.MEMBER, MemberStatus.APPROVED));

        Moment moment1 = momentRepository.save(new Moment(groupOwner, group, ownerMember, "모멘트1"));
        Moment moment2 = momentRepository.save(new Moment(groupOwner, group, ownerMember, "모멘트2"));

        // when
        List<Long> result = momentService.getCommentableMomentIdsInGroup(group.getId(), commenter, List.of(),
                List.of());

        // then
        assertThat(result).containsExactlyInAnyOrder(moment1.getId(), moment2.getId());
    }

    @Test
    void 신고한_모멘트를_제외하고_그룹_내_댓글_달_수_있는_모멘트ID_목록을_조회한다() {
        // given
        User groupOwner = userRepository.save(UserFixture.createUser());
        User commenter = userRepository.save(UserFixture.createUser());

        Group group = groupRepository.save(GroupFixture.createGroup(groupOwner));
        GroupMember ownerMember = groupMemberRepository.save(
                new GroupMember(group, groupOwner, "그룹장", MemberRole.OWNER, MemberStatus.APPROVED));
        groupMemberRepository.save(
                new GroupMember(group, commenter, "멤버", MemberRole.MEMBER, MemberStatus.APPROVED));

        Moment moment1 = momentRepository.save(new Moment(groupOwner, group, ownerMember, "모멘트1"));
        Moment moment2 = momentRepository.save(new Moment(groupOwner, group, ownerMember, "모멘트2"));
        Moment moment3 = momentRepository.save(new Moment(groupOwner, group, ownerMember, "모멘트3"));

        List<Long> reportedMomentIds = List.of(moment1.getId(), moment3.getId());

        // when
        List<Long> result = momentService.getCommentableMomentIdsInGroup(group.getId(), commenter, reportedMomentIds,
                List.of());

        // then
        assertThat(result).containsExactly(moment2.getId());
    }

    @Test
    void 그룹_내_댓글_달_수_있는_모멘트가_없으면_빈_목록을_반환한다() {
        // given
        User groupOwner = userRepository.save(UserFixture.createUser());

        Group group = groupRepository.save(GroupFixture.createGroup(groupOwner));
        groupMemberRepository.save(
                new GroupMember(group, groupOwner, "그룹장", MemberRole.OWNER, MemberStatus.APPROVED));

        // 그룹장 본인의 모멘트만 있으므로 본인은 댓글 달 수 없음
        GroupMember ownerMember = groupMemberRepository.findAll().get(0);
        momentRepository.save(new Moment(groupOwner, group, ownerMember, "내 모멘트"));

        // when
        List<Long> result = momentService.getCommentableMomentIdsInGroup(group.getId(), groupOwner, List.of(),
                List.of());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 모멘트_아이디로_모멘트를_조회한다() {
        // given
        Moment moment1 = momentRepository.save(new Moment("moment1", momenter));
        Moment moment2 = momentRepository.save(new Moment("moment2", momenter));
        Moment moment3 = momentRepository.save(new Moment("moment3", momenter));

        List<Long> momentIds = List.of(moment1, moment2, moment3).stream()
                .map(Moment::getId)
                .toList();

        // when
        List<Moment> momentsBy = momentService.getMomentsBy(momentIds);

        // then
        assertThat(momentsBy).hasSize(3);
    }

    @Test
    void 모멘트를_삭제한다() {
        // given
        Moment moment1 = momentRepository.save(new Moment("moment1", momenter));
        Moment moment2 = momentRepository.save(new Moment("moment2", momenter));

        // when
        momentService.deleteBy(moment1.getId());

        // then
        List<Moment> allMoments = momentRepository.findAll();
        assertAll(
                () -> assertThat(allMoments).hasSize(1),
                () -> assertThat(allMoments.get(0)).isEqualTo(moment2)
        );
    }

    @Test
    void 모멘트가_존재하는지_확인한다() {
        // given
        Moment moment = momentRepository.save(new Moment("moment1", momenter));

        // when
        boolean isExistsMoment1 = momentService.existsMoment(moment.getId());
        boolean isExistsMoment2 = momentService.existsMoment(2L);

        // then
        assertAll(
                () -> assertThat(isExistsMoment1).isTrue(),
                () -> assertThat(isExistsMoment2).isFalse()
        );
    }

    @Test
    void 모멘트의_작성자가_맞을경우_예외가_발생하지_않는다() {
        // given
        Moment moment = momentRepository.save(new Moment("moment1", momenter));

        // when & then
        assertDoesNotThrow(() -> momentService.validateMomenter(moment.getId(), momenter));
    }

    @Test
    void 모멘트의_작성자가_아니면_예외가_발생한다() {
        // given
        Moment moment = momentRepository.save(new Moment("moment1", momenter));

        User notMomenter = UserFixture.createUser();
        momenter = userRepository.save(notMomenter);

        // when & then
        assertThatThrownBy(() -> momentService.validateMomenter(moment.getId(), notMomenter))
                .isInstanceOf(MomentException.class);
    }
}
```

### Domain 테스트(Repository) 예시

```java

@ActiveProfiles("test")
@DataJpaTest
@DisplayNameGeneration(ReplaceUnderscores.class)
class MomentRepositoryTest {

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MomentCreatedAtHelper momentCreatedAtHelper;

    @Test
    void 내_모멘트를_생성시간_기준_내림차순으로_정렬하여_페이지를_조회한다() {
        // given
        User momenter = UserFixture.createUser();
        User savedMomenter = userRepository.save(momenter);

        LocalDateTime start = LocalDateTime.of(2025, 01, 01, 00, 00);
        Moment savedMoment1 = momentCreatedAtHelper.saveMomentWithCreatedAt("아 행복해", savedMomenter, start);
        Moment savedMoment2 = momentCreatedAtHelper.saveMomentWithCreatedAt("아 즐거워", savedMomenter,
                start.plusHours(1));
        Moment savedMoment3 = momentCreatedAtHelper.saveMomentWithCreatedAt("아 짜릿해", savedMomenter,
                start.plusHours(2));
        Moment savedMoment4 = momentCreatedAtHelper.saveMomentWithCreatedAt("아 킥킥", savedMomenter,
                start.plusHours(3));

        // when
        List<Moment> moments = momentRepository.findMyMomentsNextPage(momenter, savedMoment4.getCreatedAt(),
                savedMoment4.getId(), PageRequest.of(0, 3));

        // then
        assertAll(
                () -> assertThat(moments).hasSize(3),
                () -> assertThat(moments).isSortedAccordingTo(Comparator.comparing(Moment::getCreatedAt).reversed()),
                () -> assertThat(moments.getFirst()).isEqualTo(savedMoment3),
                () -> assertThat(moments.get(1)).isEqualTo(savedMoment2),
                () -> assertThat(moments.getLast()).isEqualTo(savedMoment1)
        );
    }

    @Test
    void 다른_사람이_작성한_3일이내의_모멘트를_조회한다() {
        // given
        User user = userRepository.save(UserFixture.createUser());
        User other = userRepository.save(UserFixture.createUser());

        LocalDateTime start = LocalDateTime.of(2025, 01, 01, 00, 00);
        Moment myMoment = momentCreatedAtHelper.saveMomentWithCreatedAt("내가 쓴 모멘트", user, start);

        Moment recentMoment = momentCreatedAtHelper.saveMomentWithCreatedAt("다른 사람 최신 모멘트", other,
                start.minusDays(3));

        Moment oldMoment = momentCreatedAtHelper.saveMomentWithCreatedAt("다른 사람 4일전 모멘트", other,
                start.minusDays(4));

        Moment reportedMoment = momentCreatedAtHelper.saveMomentWithCreatedAt("신고한 모멘트", other,
                start.plusHours(2));
        List<Long> reportedMomentIds = List.of(reportedMoment.getId());

        // when
        List<Long> results = momentRepository.findMomentIdsExcludingReported(user.getId(), start.minusDays(3),
                reportedMomentIds);

        // then
        assertThat(results).containsExactly(recentMoment.getId());
        assertThat(results).doesNotContain(myMoment.getId(), oldMoment.getId());
    }

    @Test
    void 읽지_않은_모멘트_목록의_첫_페이지를_조회한다() {
        // given
        User momenter = userRepository.save(UserFixture.createUser());
        Moment moment1 = momentRepository.save(new Moment("moment1", momenter));
        Moment moment2 = momentRepository.save(new Moment("moment2", momenter));
        momentRepository.save(new Moment("moment3", momenter)); // This one is not unread

        // when
        List<Moment> result = momentRepository.findMyUnreadMomentFirstPage(List.of(moment1.getId(), moment2.getId()),
                PageRequest.of(0, 5));

        // then
        assertThat(result).hasSize(2)
                .containsExactlyInAnyOrder(moment1, moment2);
    }

    @Test
    void 읽지_않은_모멘트_목록의_두_번째_페이지를_조회한다() {
        // given
        User momenter = UserFixture.createUser();
        User savedMomenter = userRepository.save(momenter);

        LocalDateTime start = LocalDateTime.of(2025, 01, 01, 00, 00);
        Moment savedMoment1 = momentCreatedAtHelper.saveMomentWithCreatedAt("아 행복해", savedMomenter, start);
        Moment savedMoment2 = momentCreatedAtHelper.saveMomentWithCreatedAt("아 즐거워", savedMomenter,
                start.plusHours(1));
        Moment savedMoment3 = momentCreatedAtHelper.saveMomentWithCreatedAt("아 짜릿해", savedMomenter,
                start.plusHours(2));

        // when
        List<Moment> result = momentRepository.findMyUnreadMomentNextPage(
                List.of(savedMoment1.getId(), savedMoment2.getId(), savedMoment3.getId()),
                savedMoment3.getCreatedAt(),
                savedMoment3.getId(),
                PageRequest.of(0, 1));

        // then
        assertThat(result).hasSize(1)
                .containsExactly(savedMoment2);
    }

    @Test
    void 내_모멘트_목록의_첫_페이지를_조회한다() throws InterruptedException {
        // given
        User momenter = userRepository.save(UserFixture.createUser());
        User other = userRepository.save(UserFixture.createUser());

        momentRepository.save(new Moment("다른 사람 모멘트", other));
        Moment moment1 = momentRepository.save(new Moment("아 행복해", momenter));
        Thread.sleep(10);
        Moment moment2 = momentRepository.save(new Moment("아 즐거워", momenter));
        Thread.sleep(10);
        Moment moment3 = momentRepository.save(new Moment("아 짜릿해", momenter));

        PageRequest pageRequest = PageRequest.of(0, 2);

        // when
        List<Moment> moments = momentRepository.findMyMomentFirstPage(momenter, pageRequest);

        // then
        assertAll(
                () -> assertThat(moments).hasSize(2),
                () -> assertThat(moments).isSortedAccordingTo(Comparator.comparing(Moment::getCreatedAt).reversed()),
                () -> assertThat(moments.get(0)).isEqualTo(moment3),
                () -> assertThat(moments.get(1)).isEqualTo(moment2)
        );
    }

    @Test
    void 모멘트_ID로_모멘트를_삭제한다() {
        // given
        User momenter = userRepository.save(UserFixture.createUser());
        Moment momentToDelete = momentRepository.save(new Moment("삭제될 모멘트", momenter));
        Moment momentToKeep = momentRepository.save(new Moment("유지될 모멘트", momenter));

        // when
        momentRepository.deleteById(momentToDelete.getId());

        // then
        assertAll(
                () -> assertThat(momentRepository.findById(momentToDelete.getId())).isEmpty(),
                () -> assertThat(momentRepository.findById(momentToKeep.getId())).isPresent()
        );
    }

    @Test
    void 모멘트_ID로_조회할_때_momenter를_함께_조회한다() {
        // given
        User momenter1 = userRepository.save(UserFixture.createUser());
        User momenter2 = userRepository.save(UserFixture.createUser());

        Moment moment1 = momentRepository.save(new Moment("첫번째 모멘트", momenter1));
        Moment moment2 = momentRepository.save(new Moment("두번째 모멘트", momenter2));
        Moment moment3 = momentRepository.save(new Moment("세번째 모멘트", momenter1));

        List<Long> idsToFetch = List.of(moment1.getId(), moment3.getId());

        // when
        List<Moment> results = momentRepository.findAllWithMomenterAndMemberByIds(idsToFetch);

        // then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Moment::getId).containsExactlyInAnyOrderElementsOf(idsToFetch);
        assertThat(results).allSatisfy(m -> assertThat(m.getMomenter()).isNotNull());
        assertThat(results)
                .extracting(m -> m.getMomenter().getEmail())
                .containsExactlyInAnyOrder(momenter1.getEmail(), momenter1.getEmail());
    }
}

```

### 통합 테스트 예시

```java

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ItemControllerTest {

    private final ClockHolder testClock = new TestClockHolder(LocalDateTime.now());

    @LocalServerPort
    private int port;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private OutfitRepository outfitRepository;

    @Autowired
    private DbCleaner dbCleaner;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private String accessToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        dbCleaner.clean();

        user = userRepository.save(new User("유저1", "user@gmail.com", "1234!", testClock));
        accessToken = jwtProvider.createToken(user.getId(), user.getEmail());
    }

    @AfterEach
    void down() {
        dbCleaner.clean();
    }

    @Test
    void 특정_유저의_모든_아이템을_가져온다() {
        // given
        Item itemTop = new Item("상의", Category.TOP, null, null, null, null, testClock, user.getId());
        Item itemBottom = new Item("하의", Category.BOTTOM, null, null, null, null, testClock, user.getId());
        Item itemShoes = new Item("신발", Category.SHOES, null, null, null, null, testClock, user.getId());
        itemRepository.saveAll(List.of(itemTop, itemShoes, itemBottom));

        // when & then
        List<ItemResponse> responses = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when().get("/api/v1/items")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getList("data", ItemResponse.class);

        assertThat(responses).hasSize(3);
    }

    @Test
    void 특정_유저의_특정_카테고리_아이템을_가져온다() {
        // given
        Item itemTop = new Item("상의", Category.TOP, null, null, null, null, testClock, user.getId());
        Item itemBottom = new Item("하의", Category.BOTTOM, null, null, null, null, testClock, user.getId());
        Item itemShoes = new Item("신발", Category.SHOES, null, null, null, null, testClock, user.getId());
        itemRepository.saveAll(List.of(itemTop, itemShoes, itemBottom));

        // when & then
        List<ItemResponse> responses = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when().get("/api/v1/items?category=top")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getList("data", ItemResponse.class);

        assertAll(
                () -> assertThat(responses).hasSize(1),
                () -> assertThat(responses)
                        .extracting("name")
                        .containsExactly("상의")
        );
    }

    @Test
    void 특정_유저의_최근_아이템을_가져온다() {
        // given
        Item itemTop = new Item("상의", Category.TOP, null, null, null, null,
                new TestClockHolder(LocalDateTime.now().minusDays(3)), user.getId());
        Item itemBottom = new Item("하의", Category.BOTTOM, null, null, null, null,
                new TestClockHolder(LocalDateTime.now().minusDays(2)), user.getId());
        Item itemShoes = new Item("신발", Category.SHOES, null, null, null, null,
                new TestClockHolder(LocalDateTime.now().minusDays(1)), user.getId());
        itemRepository.saveAll(List.of(itemTop, itemShoes, itemBottom));

        // when & then
        List<ItemResponse> responses = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("page", 0)
                .queryParam("size", 2)
                .queryParam("sort", "createdAt,desc")
                .when().get("/api/v1/items/recent")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getList("data", ItemResponse.class);

        assertAll(
                () -> assertThat(responses).hasSize(2),
                () -> assertThat(responses)
                        .extracting("name")
                        .containsExactly("신발", "하의")
        );
    }

    @Test
    void 아이템을_생성한다() {
        // given
        ItemCreateRequest request = new ItemCreateRequest("무탠다드 니트", "TOP", null, null, null, null);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(request)
                .when().post("/api/v1/items")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }

    @Test
    void 아이템을_수정한다() {
        // given
        Item item = itemRepository.save(
                new Item("기존 이름", Category.TOP, null, null, null, null, testClock, user.getId()));
        String updateName = "바뀐 이름";
        ItemUpdateRequest request = new ItemUpdateRequest(updateName, "BOTTOM", null, null, null, null);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(request)
                .when().patch("/api/v1/items/" + item.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value());

        Optional<Item> updatedItem = itemRepository.findById(item.getId());

        assertAll(
                () -> assertThat(updatedItem).isPresent(),
                () -> assertThat(updatedItem.get().getName()).isEqualTo(updateName)
        );
    }

    @Test
    void 아이템을_삭제한다() {
        // given
        Item item = itemRepository.save(new Item("하하", Category.TOP, null, null, null, null, testClock, user.getId()));

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when().delete("/api/v1/items/" + item.getId())
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    void 아이템을_삭제하면_코디_아이템도_삭제된다() {
        // given
        Item item = itemRepository.save(new Item("하하", Category.TOP, null, null, null, null, testClock, user.getId()));
        Outfit outfit = new Outfit("OOTD", user.getId(), null, testClock);
        outfit.addOutfitItems(List.of(item.getId()));
        outfitRepository.save(outfit);

        // when
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when().delete("/api/v1/items/" + item.getId())
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());

        // then
        Outfit savedOutfit = outfitRepository.findByIdWithOutfitItems(outfit.getId()).orElseThrow();
        assertThat(savedOutfit.hasOutfitItem(item.getId())).isFalse();
    }
}
```

