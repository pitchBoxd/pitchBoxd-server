package com.example.pitchboxd.match.playerReview.infrastructure;

import com.example.pitchboxd.match.matchReview.domain.ReviewSortType;
import com.example.pitchboxd.match.playerReview.domain.PlayerReview;
import com.example.pitchboxd.match.playerReview.domain.QPlayerReview;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PlayerReviewQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<PlayerReview> findReviewsByCursor(Long matchId, Long playerId, Long cursorId, Long cursorLikeCount, ReviewSortType sort, int size) {
        QPlayerReview playerReview = QPlayerReview.playerReview;

        var query = queryFactory
                .selectFrom(playerReview)
                .where(
                        playerReview.matchId.eq(matchId),
                        playerReview.playerId.eq(playerId),
                        buildCursorCondition(cursorId, cursorLikeCount, sort)
                );

        if (ReviewSortType.LIKE == sort) {
            query = query.orderBy(playerReview.likeCount.desc(), playerReview.id.desc());
        } else {
            query = query.orderBy(playerReview.id.desc());
        }

        query = query.limit(size + 1);

        return query.fetch();
    }

    private BooleanExpression buildCursorCondition(Long cursorId, Long cursorLikeCount, ReviewSortType sort) {
        if (ReviewSortType.LIKE == sort) {
            return lessThanCursorLike(cursorLikeCount, cursorId);
        }
        return lessThanCursorId(cursorId);
    }

    private BooleanExpression lessThanCursorId(Long cursorId) {
        QPlayerReview playerReview = QPlayerReview.playerReview;
        return cursorId != null ? playerReview.id.lt(cursorId) : null;
    }

    private BooleanExpression lessThanCursorLike(Long cursorLikeCount, Long cursorId) {
        QPlayerReview playerReview = QPlayerReview.playerReview;
        if (cursorLikeCount == null || cursorId == null) {
            return null;
        }
        return playerReview.likeCount.lt(cursorLikeCount)
                .or(playerReview.likeCount.eq(cursorLikeCount).and(playerReview.id.lt(cursorId)));
    }
}
