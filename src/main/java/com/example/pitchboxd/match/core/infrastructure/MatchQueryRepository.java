package com.example.pitchboxd.match.core.infrastructure;

import static com.example.pitchboxd.match.core.domain.QMatch.match;
import static com.example.pitchboxd.match.matchStatistics.domain.QMatchStatistics.matchStatistics;
import static com.querydsl.core.types.dsl.Expressions.numberTemplate;

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
        return findFinishedMatchesSince(threshold, null);
    }

    // 2. 메인 쿼리 메서드 (teamId가 null이면 전체 조회, 값이 있으면 해당 팀 필터링)
    public List<MatchSummary> findFinishedMatchesSince(LocalDateTime threshold, Long teamId) {
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
                        // 복잡한 계산식은 가급적 DB보다 애플리케이션(DTO)에서 처리하는 것도 좋습니다.
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
                        match.status.eq(MatchStatus.FINISHED),
                        match.finishedAt.goe(threshold),
                        eqTeamId(teamId)
                )
                .orderBy(match.startTime.asc())
                .fetch();
    }

    // 3. 동적 쿼리를 위한 BooleanExpression 메서드
    private BooleanExpression eqTeamId(Long teamId) {
        if (teamId == null) {
            return null; // QueryDSL은 where 절에 null이 들어오면 무시(동적 필터링)합니다.
        }
        // 홈 팀이거나 어웨이 팀인 경우
        return match.homeTeamId.eq(teamId).or(match.awayTeamId.eq(teamId));
    }
}
