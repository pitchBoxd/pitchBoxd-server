package com.example.pitchboxd.match.lineup.infrastructure;

import static com.example.pitchboxd.match.lineup.domain.QMatchLineup.matchLineup;
import static com.example.pitchboxd.player.domain.QPlayer.player;

import com.example.pitchboxd.match.lineup.domain.ParticipationStatus;
import com.example.pitchboxd.match.lineup.infrastructure.dto.LineupPlayerModel;
import com.example.pitchboxd.match.lineup.infrastructure.dto.QLineupPlayerModel;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MatchLineupQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<LineupPlayerModel> findLineupPlayersByMatchId(Long matchId) {
        return queryFactory
                .select(new QLineupPlayerModel(
                        player.id,
                        player.name,
                        matchLineup.backNumber,
                        player.teamId,
                        matchLineup.status
                ))
                .from(matchLineup)
                .innerJoin(player).on(matchLineup.playerId.eq(player.id))
                .where(
                        matchLineup.matchId.eq(matchId),
                        matchLineup.status.ne(ParticipationStatus.BENCH)
                )
                .fetch();
    }
}
