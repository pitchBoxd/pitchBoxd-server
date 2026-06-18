package com.example.pitchboxd.match.playerReview.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.global.config.QueryDslConfig;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import com.example.pitchboxd.match.core.infrastructure.MatchRepository;
import com.example.pitchboxd.match.matchReview.domain.ReviewSortType;
import com.example.pitchboxd.match.playerReview.domain.PlayerReview;
import com.example.pitchboxd.player.domain.Player;
import com.example.pitchboxd.player.infrastructure.PlayerRepository;
import com.example.pitchboxd.support.DatabaseCleaner;
import com.example.pitchboxd.team.domain.Team;
import com.example.pitchboxd.team.infrastructure.TeamRepository;
import com.example.pitchboxd.user.domain.User;
import com.example.pitchboxd.user.infrastructure.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@DisplayNameGeneration(ReplaceUnderscores.class)
@Import({QueryDslConfig.class, PlayerReviewQueryRepository.class, DatabaseCleaner.class})
class PlayerReviewQueryRepositoryTest {

    @Autowired
    private PlayerReviewQueryRepository playerReviewQueryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PlayerReviewRepository playerReviewRepository;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();
    }

    @Test
    void 특정_경기의_특정_플레이어에_속한_리뷰_데이터를_최신순으로_커서_페이징_조회한다() {
        // given
        User user = userRepository.save(new User("유저", "test@test.com", "password"));
        Team homeTeam = teamRepository.save(new Team("홈팀", "1"));
        Team awayTeam = teamRepository.save(new Team("어웨이팀", "1"));
        Match match = matchRepository.save(
                new Match(1L, "1R", homeTeam.getId(), awayTeam.getId(), LocalDateTime.now(), MatchStatus.FINISHED,
                        "상암", "1"));
        Player player = playerRepository.save(new Player(homeTeam.getId(), "선수", "1"));

        PlayerReview review1 = playerReviewRepository.save(new PlayerReview(match.getId(), player.getId(), user.getId(), 5, "리뷰1"));
        PlayerReview review2 = playerReviewRepository.save(new PlayerReview(match.getId(), player.getId(), user.getId(), 4, "리뷰2"));
        PlayerReview review3 = playerReviewRepository.save(new PlayerReview(match.getId(), player.getId(), user.getId(), 3, "리뷰3"));
        PlayerReview review4 = playerReviewRepository.save(new PlayerReview(match.getId(), player.getId(), user.getId(), 2, "리뷰4"));

        // when - size가 2이면 hasNext 판단을 위해 size+1 즉, 3개가 조회되어야 함.
        List<PlayerReview> result = playerReviewQueryRepository.findReviewsByCursor(
                match.getId(), player.getId(), null, null, ReviewSortType.LATEST, 2
        );

        // then - id 역순(최신순)
        assertAll(
                () -> assertThat(result).hasSize(3),
                () -> assertThat(result.get(0).getId()).isEqualTo(review4.getId()),
                () -> assertThat(result.get(1).getId()).isEqualTo(review3.getId()),
                () -> assertThat(result.get(2).getId()).isEqualTo(review2.getId())
        );
    }

    @Test
    void 특정_경기의_특정_플레이어에_속한_리뷰_데이터를_추천순으로_커서_페이징_조회한다() {
        // given
        User user = userRepository.save(new User("유저", "test@test.com", "password"));
        Team homeTeam = teamRepository.save(new Team("홈팀", "1"));
        Team awayTeam = teamRepository.save(new Team("어웨이팀", "1"));
        Match match = matchRepository.save(
                new Match(1L, "1R", homeTeam.getId(), awayTeam.getId(), LocalDateTime.now(), MatchStatus.FINISHED,
                        "상암", "1"));
        Player player = playerRepository.save(new Player(homeTeam.getId(), "선수", "1"));

        PlayerReview review1 = new PlayerReview(match.getId(), player.getId(), user.getId(), 5, "리뷰1");
        PlayerReview review2 = new PlayerReview(match.getId(), player.getId(), user.getId(), 4, "리뷰2");
        PlayerReview review3 = new PlayerReview(match.getId(), player.getId(), user.getId(), 3, "리뷰3");
        PlayerReview review4 = new PlayerReview(match.getId(), player.getId(), user.getId(), 2, "리뷰4");

        review3.addOneLikeCount(); // likeCount 1
        review3.addOneLikeCount(); // likeCount 2

        review2.addOneLikeCount(); // likeCount 1

        PlayerReview savedReview1 = playerReviewRepository.save(review1);
        PlayerReview savedReview2 = playerReviewRepository.save(review2);
        PlayerReview savedReview3 = playerReviewRepository.save(review3);
        PlayerReview savedReview4 = playerReviewRepository.save(review4);

        // when - size가 2이면 3개 조회되어야 함. 추천순(likeCount desc, id desc)
        List<PlayerReview> result = playerReviewQueryRepository.findReviewsByCursor(
                match.getId(), player.getId(), null, null, ReviewSortType.LIKE, 2
        );

        // then - 정렬 순서: review3(like: 2, id: 3), review2(like: 1, id: 2), review4(like: 0, id: 4) 순이어야 함
        assertAll(
                () -> assertThat(result).hasSize(3),
                () -> assertThat(result.get(0).getId()).isEqualTo(savedReview3.getId()),
                () -> assertThat(result.get(1).getId()).isEqualTo(savedReview2.getId()),
                () -> assertThat(result.get(2).getId()).isEqualTo(savedReview4.getId())
        );
    }
}
