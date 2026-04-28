package com.example.pitchboxd.match.matchReview.infrastructure;

import com.example.pitchboxd.match.core.domain.QMatch;
import com.example.pitchboxd.match.matchReview.domain.QMatchReview;
import com.example.pitchboxd.match.matchReview.infrastructure.dto.HotReviewSummary;
import com.example.pitchboxd.team.domain.QTeam;
import com.example.pitchboxd.user.domain.QUser;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MatchReviewQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<HotReviewSummary> findHotReviews(List<Long> reviewableMatchIds, int limit) {
        QMatchReview matchReview = QMatchReview.matchReview;
        QMatch match = QMatch.match;
        QUser user = QUser.user;
        QTeam homeTeam = new QTeam("homeTeam");
        QTeam awayTeam = new QTeam("awayTeam");

        return queryFactory
                .select(Projections.constructor(HotReviewSummary.class,
                        matchReview.id,
                        matchReview.matchId,
                        homeTeam.name,
                        awayTeam.name,
                        user.nickname,
                        user.id,
                        matchReview.fanType,
                        matchReview.point,
                        matchReview.content,
                        matchReview.likeCount
                ))
                .from(matchReview)
                .join(match).on(matchReview.matchId.eq(match.id))
                .join(homeTeam).on(match.homeTeamId.eq(homeTeam.id))
                .join(awayTeam).on(match.awayTeamId.eq(awayTeam.id))
                .join(user).on(matchReview.userId.eq(user.id))
                .where(matchReview.matchId.in(reviewableMatchIds))
                .orderBy(matchReview.likeCount.desc(), matchReview.id.desc())
                .limit(limit)
                .fetch();
    }

    public List<HotReviewSummary> findHotReviewsByMatchId(Long matchId, int limit) {
        QMatchReview matchReview = QMatchReview.matchReview;
        QUser user = QUser.user;

        return queryFactory
                .select(Projections.constructor(HotReviewSummary.class,
                        matchReview.id,
                        matchReview.matchId,
                        user.nickname,
                        user.id,
                        matchReview.fanType,
                        matchReview.point,
                        matchReview.content,
                        matchReview.likeCount
                ))
                .from(matchReview)
                .join(user).on(matchReview.userId.eq(user.id))
                .where(matchReview.matchId.eq(matchId))
                .orderBy(matchReview.likeCount.desc(), matchReview.id.desc())
                .limit(limit)
                .fetch();
    }
}
