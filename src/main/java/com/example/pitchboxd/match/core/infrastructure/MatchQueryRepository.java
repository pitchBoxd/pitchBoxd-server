package com.example.pitchboxd.match.core.infrastructure;

import static com.example.pitchboxd.match.core.domain.QMatch.match;
import static com.example.pitchboxd.match.matchStatistics.domain.QMatchStatistics.matchStatistics;
import static com.querydsl.core.types.dsl.Expressions.numberTemplate;

import com.example.pitchboxd.match.core.domain.MatchFilter;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import com.example.pitchboxd.match.core.infrastructure.dto.MatchSummary;
import com.example.pitchboxd.match.core.infrastructure.dto.QMatchSummary;
import com.example.pitchboxd.team.domain.QTeam;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MatchQueryRepository {

    private final JPAQueryFactory queryFactory;

    // 1. 서비스 계층을 위한 오버로딩 (기존 코드 호환성 유지)
    public List<MatchSummary> findFinishedMatchesSince(LocalDateTime threshold) {
        return findMatches(null, MatchFilter.REVIEWABLE, threshold);
    }

    //TODO: 더 이상 사용하지 않을 것으로 판단됨. 추후 삭제 필
    public List<MatchSummary> findFinishedMatchesSince(LocalDateTime threshold, Long teamId) {
        return queryFactory
                .select(new QMatchSummary(
                        match.id,
                        match.round,
                        match.startTime,
                        match.location,
                        new QTeam("homeTeam").name,
                        match.matchResult.homeScore,
                        new QTeam("awayTeam").name,
                        match.matchResult.awayScore,
                        matchStatistics.totalReviewCount,
                        numberTemplate(Double.class,
                                "COALESCE({0} * 1.0 / NULLIF({1}, 0) / 2.0, 0.0)",
                                matchStatistics.totalRatingSum,
                                matchStatistics.totalReviewCount)
                ))
                .from(match)
                .innerJoin(new QTeam("homeTeam")).on(match.homeTeamId.eq(new QTeam("homeTeam").id))
                .innerJoin(new QTeam("awayTeam")).on(match.awayTeamId.eq(new QTeam("awayTeam").id))
                .leftJoin(matchStatistics).on(match.id.eq(matchStatistics.matchId))
                .where(
                        match.status.eq(MatchStatus.FINISHED),
                        match.finishedAt.goe(threshold),
                        eqTeamId(teamId)
                )
                .orderBy(match.startTime.asc())
                .fetch();
    }

    public List<MatchSummary> findMatches(Long seasonId, MatchFilter state, LocalDateTime reviewableThreshold) {
        QTeam homeTeam = new QTeam("homeTeam");
        QTeam awayTeam = new QTeam("awayTeam");

        return queryFactory
                .select(new QMatchSummary(
                        match.id,
                        match.round,
                        match.startTime,
                        match.location,
                        homeTeam.name,
                        match.matchResult.homeScore,
                        awayTeam.name,
                        match.matchResult.awayScore,
                        matchStatistics.totalReviewCount,
                        numberTemplate(Double.class,
                                "COALESCE({0} * 1.0 / NULLIF({1}, 0) / 2.0, 0.0)",
                                matchStatistics.totalRatingSum,
                                matchStatistics.totalReviewCount)
                ))
                .from(match)
                .innerJoin(homeTeam).on(match.homeTeamId.eq(homeTeam.id))
                .innerJoin(awayTeam).on(match.awayTeamId.eq(awayTeam.id))
                .leftJoin(matchStatistics).on(match.id.eq(matchStatistics.matchId))
                .where(
                        eqSeasonId(seasonId),
                        eqState(state, reviewableThreshold)
                )
                .orderBy(match.startTime.asc())
                .fetch();
    }

    private BooleanExpression eqSeasonId(Long seasonId) {
        return seasonId != null ? match.seasonId.eq(seasonId) : null;
    }

    private BooleanExpression eqState(MatchFilter state, LocalDateTime threshold) {
        if (MatchFilter.REVIEWABLE == state) {
            return match.status.eq(MatchStatus.FINISHED)
                    .and(match.finishedAt.goe(threshold));
        }
        return null;
    }

    // 3. 동적 쿼리를 위한 BooleanExpression 메서드
    private BooleanExpression eqTeamId(Long teamId) {
        if (teamId == null) {
            return null;
        }
        // 홈 팀이거나 어웨이 팀인 경우
        return match.homeTeamId.eq(teamId).or(match.awayTeamId.eq(teamId));
    }
}
