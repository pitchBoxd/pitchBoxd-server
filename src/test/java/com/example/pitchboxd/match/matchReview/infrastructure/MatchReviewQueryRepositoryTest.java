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
    void 특정_경기의_인기_리뷰_목록을_좋아요_순_리뷰_ID_역순으로_조회한다() {
        // given
        User user = new User("닉네임", "user@test.com", "password");
        entityManager.persist(user);

        Team homeTeam = new Team("홈팀", "1");
        Team awayTeam = new Team("어웨이팀", "2");
        entityManager.persist(homeTeam);
        entityManager.persist(awayTeam);

        Match match1 = new Match(1L, "11R", homeTeam.getId(), awayTeam.getId(), LocalDateTime.now(),
                MatchStatus.FINISHED, "상암", "1");
        Match match2 = new Match(1L, "12R", homeTeam.getId(), awayTeam.getId(), LocalDateTime.now(),
                MatchStatus.FINISHED, "빅버드", "2");
        entityManager.persist(match1);
        entityManager.persist(match2);

        MatchReview review1 = new MatchReview(match1.getId(), user.getId(), 10, "좋아요 1개", FanType.HOME);
        MatchReview review2 = new MatchReview(match1.getId(), user.getId(), 8, "좋아요 2개", FanType.AWAY);
        MatchReview review3 = new MatchReview(match1.getId(), user.getId(), 10, "좋아요 3개", FanType.HOME);
        MatchReview review4 = new MatchReview(match1.getId(), user.getId(), 10, "좋아요 4개", FanType.HOME);
        MatchReview review5 = new MatchReview(match2.getId(), user.getId(), 5, "다른 경기 리뷰", FanType.HOME);

        review4.addOneLikeCount();
        review4.addOneLikeCount();
        review4.addOneLikeCount();
        review4.addOneLikeCount();

        review3.addOneLikeCount();
        review3.addOneLikeCount();
        review3.addOneLikeCount();

        review2.addOneLikeCount();
        review2.addOneLikeCount();

        review1.addOneLikeCount();

        entityManager.persist(review1);
        MatchReview savedReview2 = entityManager.persist(review2);
        MatchReview savedReview3 = entityManager.persist(review3);
        MatchReview savedReview4 = entityManager.persist(review4);
        entityManager.persist(review5);

        // when
        List<HotReviewSummary> result = matchReviewQueryRepository.findHotReviewsByMatchId(match1.getId(), 3);

        // then
        assertAll(
                () -> assertThat(result).hasSize(3),
                () -> assertThat(result.get(0).reviewId()).isEqualTo(savedReview4.getId()), // 좋아요 4
                () -> assertThat(result.get(1).reviewId()).isEqualTo(savedReview3.getId()), // 좋아요 3
                () -> assertThat(result.get(2).reviewId()).isEqualTo(savedReview2.getId()), // 좋아요 2
                () -> assertThat(result.get(0).matchId()).isEqualTo(match1.getId())
        );
    }
}
