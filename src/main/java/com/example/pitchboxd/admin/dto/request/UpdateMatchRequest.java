package com.example.pitchboxd.admin.dto.request;

import com.example.pitchboxd.match.core.domain.GoalScorer;
import com.example.pitchboxd.match.core.domain.MatchResult;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import java.time.LocalDateTime;
import java.util.List;

public record UpdateMatchRequest(
        String round,
        Long homeTeamId,
        Long awayTeamId,
        LocalDateTime startTime,
        LocalDateTime finishedAt,
        MatchStatus status,
        String location,
        MatchResultRequest matchResult,
        String naverId
) {
    public record MatchResultRequest(
            Integer homeScore,
            Integer awayScore,
            List<GoalScorerRequest> homeScorers,
            List<GoalScorerRequest> awayScorers
    ) {
        // DTO를 도메인 모델(Embeddable)로 변환하는 편의 메서드
        public MatchResult toEmbeddable() {
            if (homeScore == null || awayScore == null) {
                return null;
            }

            return new MatchResult(
                    this.homeScore,
                    this.awayScore,
                    toGoalScorerList(this.homeScorers),
                    toGoalScorerList(this.awayScorers)
            );
        }

        private List<GoalScorer> toGoalScorerList(List<GoalScorerRequest> requests) {
            if (requests == null) {
                return List.of();
            }
            return requests.stream()
                    .map(GoalScorerRequest::toDomain)
                    .toList();
        }
    }

    // 내부 중첩 레코드: 득점자 정보
    public record GoalScorerRequest(
            String playerName,
            Integer minute,
            Integer addedTime,
            boolean ownGoal
    ) {
        public GoalScorer toDomain() {
            return new GoalScorer(playerName, minute, addedTime, ownGoal);
        }
    }
}
