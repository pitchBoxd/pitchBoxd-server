package com.example.pitchboxd.match.playerReview.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import com.example.pitchboxd.match.core.infrastructure.MatchRepository;
import com.example.pitchboxd.match.playerReview.domain.PlayerReview;
import com.example.pitchboxd.match.playerReview.domain.PlayerReviewLike;
import com.example.pitchboxd.player.domain.Player;
import com.example.pitchboxd.player.infrastructure.PlayerRepository;
import com.example.pitchboxd.support.DatabaseCleaner;
import com.example.pitchboxd.team.domain.Team;
import com.example.pitchboxd.team.infrastructure.TeamRepository;
import com.example.pitchboxd.user.domain.User;
import com.example.pitchboxd.user.infrastructure.UserRepository;
import java.time.LocalDateTime;
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
@Import({DatabaseCleaner.class})
class PlayerReviewLikeRepositoryTest {

    @Autowired
    private PlayerReviewLikeRepository playerReviewLikeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PlayerReviewRepository playerReviewRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Test
    void 플레이어_리뷰_좋아요_존재_여부를_확인한다() {
        // given
        User user = userRepository.save(new User("유저", "test@test.com", "password"));
        Team homeTeam = teamRepository.save(new Team("홈팀", "1"));
        Team awayTeam = teamRepository.save(new Team("어웨이팀", "1"));
        Match match = matchRepository
                .save(new Match(1L, "1R", homeTeam.getId(), awayTeam.getId(), LocalDateTime.now(), MatchStatus.FINISHED,
                        "상암", "1"));
        Player player = playerRepository.save(new Player(homeTeam.getId(), "홈 선수", "1"));
        PlayerReview playerReview = playerReviewRepository.save(
                new PlayerReview(match.getId(), player.getId(), user.getId(), 5, "최고의 경기!"));

        playerReviewLikeRepository.save(new PlayerReviewLike(playerReview.getId(), user.getId()));

        // when
        boolean exists = playerReviewLikeRepository.existsByPlayerReviewIdAndUserId(playerReview.getId(), user.getId());
        boolean notExists = playerReviewLikeRepository.existsByPlayerReviewIdAndUserId(playerReview.getId(), 999L);

        // then
        assertAll(
                () -> assertThat(exists).isTrue(),
                () -> assertThat(notExists).isFalse()
        );
    }

    @Test
    void 플레이어_리뷰_좋아요를_삭제한다() {
        // given
        User user = userRepository.save(new User("유저", "test@test.com", "password"));
        Team homeTeam = teamRepository.save(new Team("홈팀", "1"));
        Team awayTeam = teamRepository.save(new Team("어웨이팀", "1"));
        Match match = matchRepository
                .save(new Match(1L, "1R", homeTeam.getId(), awayTeam.getId(), LocalDateTime.now(), MatchStatus.FINISHED,
                        "상암", "1"));
        Player player = playerRepository.save(new Player(homeTeam.getId(), "홈 선수", "1"));
        PlayerReview playerReview = playerReviewRepository.save(
                new PlayerReview(match.getId(), player.getId(), user.getId(), 5, "최고의 경기!"));

        playerReviewLikeRepository.save(new PlayerReviewLike(playerReview.getId(), user.getId()));

        // when
        playerReviewLikeRepository.deleteByPlayerReviewIdAndUserId(playerReview.getId(), user.getId());

        // then
        boolean exists = playerReviewLikeRepository.existsByPlayerReviewIdAndUserId(playerReview.getId(), user.getId());
        assertThat(exists).isFalse();
    }
}
