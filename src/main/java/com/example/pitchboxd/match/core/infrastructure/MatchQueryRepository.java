package com.example.pitchboxd.match.core.infrastructure;

import static com.example.pitchboxd.match.core.domain.QMatch.match;
import static com.example.pitchboxd.match.matchStatistics.domain.QMatchStatistics.matchStatistics;
import static com.querydsl.core.types.dsl.Expressions.numberTemplate;

import com.example.pitchboxd.match.core.service.domain.dto.MatchSummary;
import com.example.pitchboxd.match.core.service.domain.dto.QMatchSummary;
import com.example.pitchboxd.team.domain.QTeam;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MatchQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<MatchSummary> findMatchesBetween(LocalDateTime start, LocalDateTime end) {
        // 1. 같은 Team 테이블을 구분하기 위한 별칭 생성
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
                .where(match.startTime.between(start, end))
                .orderBy(match.startTime.asc())
                .fetch();
    }
    
}
