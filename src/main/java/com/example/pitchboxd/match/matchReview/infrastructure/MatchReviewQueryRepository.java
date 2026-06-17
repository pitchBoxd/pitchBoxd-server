package com.example.pitchboxd.match.matchReview.infrastructure;

import com.example.pitchboxd.match.matchReview.domain.MatchReview;
import com.example.pitchboxd.match.matchReview.domain.QMatchReview;
import com.example.pitchboxd.match.matchReview.domain.ReviewSortType;
import com.example.pitchboxd.match.matchReview.infrastructure.dto.HotReviewSummary;
import com.example.pitchboxd.user.domain.QUser;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MatchReviewQueryRepository {

    private final JPAQueryFactory queryFactory;

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
                        matchReview.likeCount,
                        matchReview.updatedAt.isNotNull()
                ))
                .from(matchReview)
                .join(user).on(matchReview.userId.eq(user.id))
                .where(matchReview.matchId.eq(matchId))
                .orderBy(matchReview.likeCount.desc(), matchReview.id.desc())
                .limit(limit)
                .fetch();
    }

    public List<HotReviewSummary> findHotReviewsByMatchIds(List<Long> matchIds) {
        if (matchIds == null || matchIds.isEmpty()) {
            return List.of();
        }

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
                        matchReview.likeCount,
                        matchReview.updatedAt.isNotNull()
                ))
                .from(matchReview)
                .join(user).on(matchReview.userId.eq(user.id))
                .where(matchReview.matchId.in(matchIds))
                .orderBy(matchReview.likeCount.desc(), matchReview.id.desc())
                .fetch();
    }

    public List<MatchReview> findReviewsByCursor(Long matchId, Long cursorId, Long cursorLikeCount, ReviewSortType sort,
                                                 int size) {
        QMatchReview matchReview = QMatchReview.matchReview;

        var query = queryFactory
                .selectFrom(matchReview)
                .where(
                        matchReview.matchId.eq(matchId),
                        buildCursorCondition(cursorId, cursorLikeCount, sort)
                );

        if (ReviewSortType.LIKE == sort) {
            query = query.orderBy(matchReview.likeCount.desc(), matchReview.id.desc());
        } else {
            query = query.orderBy(matchReview.id.desc());
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
        QMatchReview matchReview = QMatchReview.matchReview;
        return cursorId != null ? matchReview.id.lt(cursorId) : null;
    }

    private BooleanExpression lessThanCursorLike(Long cursorLikeCount, Long cursorId) {
        QMatchReview matchReview = QMatchReview.matchReview;
        if (cursorLikeCount == null || cursorId == null) {
            return null;
        }
        return matchReview.likeCount.lt(cursorLikeCount)
                .or(matchReview.likeCount.eq(cursorLikeCount).and(matchReview.id.lt(cursorId)));
    }
}
