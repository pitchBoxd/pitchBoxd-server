package com.example.pitchboxd.match.matchReview.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.pitchboxd.global.infrastructure.SystemClockHolder;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import com.example.pitchboxd.match.core.infrastructure.MatchRepository;
import com.example.pitchboxd.match.matchReview.domain.MatchReview;
import com.example.pitchboxd.match.matchReview.domain.MatchReviewLike;
import com.example.pitchboxd.support.DatabaseCleaner;
import com.example.pitchboxd.team.domain.Team;
import com.example.pitchboxd.team.infrastructure.TeamRepository;
import com.example.pitchboxd.user.domain.User;
import com.example.pitchboxd.user.infrastructure.UserRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@Import({DatabaseCleaner.class, SystemClockHolder.class})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MatchReviewLikeRepositoryTest {

    @Autowired
    private MatchReviewLikeRepository matchReviewLikeRepository;

    @Autowired
    private MatchReviewRepository matchReviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TeamRepository teamRepository;

    private User liker;
    private MatchReview review;

    @BeforeEach
    void setUp() {
        User reviewer = userRepository.save(new User("Reviewer", "reviewer@test.com", "password"));
        liker = userRepository.save(new User("Liker", "liker@test.com", "password"));

        Team home = teamRepository.save(new Team("HomeTeam"));
        Team away = teamRepository.save(new Team("AwayTeam"));

        Match match = matchRepository
                .save(new Match(1L, "1R", home.getId(), away.getId(), LocalDateTime.now(), MatchStatus.FINISHED, "상암"));
        review = matchReviewRepository.save(new MatchReview(match.getId(), reviewer.getId(), 5, "좋은 경기였습니다."));
    }

    @Test
    void 리뷰_ID와_유저_ID로_좋아요_존재_여부를_확인한다() {
        // given
        matchReviewLikeRepository.save(new MatchReviewLike(review.getId(), liker.getId()));

        // when
        boolean exists = matchReviewLikeRepository.existsByMatchReviewIdAndUserId(review.getId(), liker.getId());
        boolean notExists = matchReviewLikeRepository.existsByMatchReviewIdAndUserId(review.getId(), 999L);

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void 리뷰_ID와_유저_ID로_좋아요를_삭제한다() {
        // given
        matchReviewLikeRepository.save(new MatchReviewLike(review.getId(), liker.getId()));

        // when
        matchReviewLikeRepository.deleteByMatchReviewIdAndUserId(review.getId(), liker.getId());
        matchReviewLikeRepository.flush();

        // then
        boolean exists = matchReviewLikeRepository.existsByMatchReviewIdAndUserId(review.getId(), liker.getId());
        assertThat(exists).isFalse();
    }
}
