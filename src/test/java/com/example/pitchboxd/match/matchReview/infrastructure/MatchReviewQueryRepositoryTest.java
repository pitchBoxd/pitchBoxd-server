package com.example.pitchboxd.match.matchReview.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.global.config.QueryDslConfig;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import com.example.pitchboxd.match.matchReview.domain.MatchReview;
import com.example.pitchboxd.match.matchReview.infrastructure.dto.HotReviewSummary;
import com.example.pitchboxd.match.matchStatistics.domain.FanType;
import com.example.pitchboxd.team.domain.Team;
import com.example.pitchboxd.user.domain.User;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@Import({QueryDslConfig.class, MatchReviewQueryRepository.class})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MatchReviewQueryRepositoryTest {

    @Autowired
    private MatchReviewQueryRepository matchReviewQueryRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void 인기_리뷰_목록을_좋아요_순_리뷰_ID_역순으로_조회한다() {
        // given
        String user1Name = "닉네임1";
        User user1 = new User(user1Name, "user1@test.com", "password");
        User user2 = new User("닉네임2", "user2@test.com", "password");
        entityManager.persist(user1);
        entityManager.persist(user2);

        Team homeTeam = new Team("홈팀", "1");
        Team awayTeam = new Team("어웨이팀", "2");
        entityManager.persist(homeTeam);
        entityManager.persist(awayTeam);

        Match match = new Match(1L, "11R", homeTeam.getId(), awayTeam.getId(), LocalDateTime.now(),
                MatchStatus.FINISHED, "상암", "1");
        entityManager.persist(match);

        MatchReview review1 = new MatchReview(match.getId(), user1.getId(), 10, "최고의 경기였어요!", FanType.HOME);
        MatchReview review2 = new MatchReview(match.getId(), user2.getId(), 8, "아쉬운 경기네요.", FanType.AWAY);
        MatchReview review3 = new MatchReview(match.getId(), user1.getId(), 10, "평범한 경기였습니다.", FanType.HOME);

        review3.addOneLikeCount();
        review3.addOneLikeCount();
        review3.addOneLikeCount();

        review2.addOneLikeCount();
        review2.addOneLikeCount();

        review1.addOneLikeCount();

        MatchReview savedReview1 = entityManager.persist(review1);
        MatchReview savedReview2 = entityManager.persist(review2);
        MatchReview savedReview3 = entityManager.persist(review3);

        List<Long> reviewableMatchIds = List.of(match.getId());

        // when
        List<HotReviewSummary> result = matchReviewQueryRepository.findHotReviews(reviewableMatchIds, 10);

        // then
        assertAll(
                () -> assertThat(result).hasSize(3),
                () -> assertThat(result.get(0).reviewId()).isEqualTo(savedReview3.getId()), // 좋아요 3
                () -> assertThat(result.get(1).reviewId()).isEqualTo(savedReview2.getId()), // 좋아요 2
                () -> assertThat(result.get(2).reviewId()).isEqualTo(savedReview1.getId()), // 좋아요 1
                () -> assertThat(result.get(0).homeTeamName()).isEqualTo("홈팀"),
                () -> assertThat(result.get(0).awayTeamName()).isEqualTo("어웨이팀"),
                () -> assertThat(result.get(0).authorNickname()).isEqualTo(user1Name)
        );
    }

    @Test
    void 리뷰_가능한_경기_ID_목록에_포함된_리뷰만_조회한다() {
        // given
        User user = new User("닉네임", "user@test.com", "password");
        entityManager.persist(user);

        Team matchHomeTeam = new Team("나와야하는 홈 팀", "1");
        Team matchAwayTeam = new Team("나와야하는 원정 팀", "2");
        Team otherHomeTeam = new Team("안나와야하는 홈 팀", "3");
        Team otherAwayTeam = new Team("안나와야하는 원정 팀", "4");

        entityManager.persist(matchHomeTeam);
        entityManager.persist(matchAwayTeam);
        entityManager.persist(otherHomeTeam);
        entityManager.persist(otherAwayTeam);

        Match match1 = new Match(1L, "3R", matchHomeTeam.getId(), matchAwayTeam.getId(), LocalDateTime.now(),
                MatchStatus.FINISHED, "상암", "1");
        Match match2 = new Match(1L, "3R", otherHomeTeam.getId(), otherAwayTeam.getId(), LocalDateTime.now(),
                MatchStatus.FINISHED, "빅버드", "2");

        entityManager.persist(match1);
        entityManager.persist(match2);

        MatchReview review1 = new MatchReview(match1.getId(), user.getId(), 5, "경기1 리뷰", FanType.HOME);
        MatchReview review2 = new MatchReview(match2.getId(), user.getId(), 5, "경기2 리뷰", FanType.HOME);
        entityManager.persist(review1);
        entityManager.persist(review2);

        List<Long> reviewableMatchIds = List.of(match1.getId());

        // when
        List<HotReviewSummary> result = matchReviewQueryRepository.findHotReviews(reviewableMatchIds, 10);

        // then
        assertAll(
                () -> assertThat(result).hasSize(1),
                () -> assertThat(result.get(0).reviewId()).isEqualTo(review1.getId())
        );
    }

    @Test
    void 요청한_제한_개수만큼만_인기_리뷰를_반환한다() {
        // given
        User user = new User("닉네임", "user@test.com", "password");
        entityManager.persist(user);

        Team homeTeam = new Team("홈팀", "1");
        Team awayTeam = new Team("원정", "1");
        entityManager.persist(homeTeam);
        entityManager.persist(awayTeam);

        Match match = new Match(1L, "3R", homeTeam.getId(), awayTeam.getId(), LocalDateTime.now(), MatchStatus.FINISHED,
                "지구", "1");
        entityManager.persist(match);

        for (int i = 1; i <= 5; i++) {
            entityManager.persist(new MatchReview(match.getId(), user.getId(), 5, "리뷰" + i, FanType.HOME));
        }

        List<Long> reviewableMatchIds = List.of(match.getId());
        int limit = 3;

        // when
        List<HotReviewSummary> result = matchReviewQueryRepository.findHotReviews(reviewableMatchIds, limit);

        // then
        assertThat(result).hasSize(limit);
    }
}
