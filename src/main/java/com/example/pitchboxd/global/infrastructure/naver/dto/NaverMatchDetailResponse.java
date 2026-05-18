package com.example.pitchboxd.global.infrastructure.naver.dto;

import com.example.pitchboxd.match.core.domain.GoalScorer;
import com.example.pitchboxd.match.core.domain.MatchResult;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties
public record NaverMatchDetailResponse(ResultNode result) {
    // 1. Service 레이어를 위한 편의성 메서드 (Deep 체이닝 방지)
    public boolean isFinished() {
        return "RESULT".equals(result.game().statusCode());
    }


    public MatchResult toMatchResult() {
        return new MatchResult(homeTeamScore(), awayTeamScore(), toHomeScorers(), toAwayScorers());
    }

    public int homeTeamScore() {
        return result.game().homeTeamScore();
    }

    public int awayTeamScore() {
        return result.game().awayTeamScore();
    }

    // 2. 도메인 객체(Scorer)로의 변환 로직 (도메인 보호)
    public List<GoalScorer> toHomeScorers() {
        return result.game().scorers().home().stream()
                .map(NaverScorer::toDomain)
                .toList();
    }

    public List<GoalScorer> toAwayScorers() {
        return result.game().scorers().away().stream()
                .map(NaverScorer::toDomain)
                .toList();
    }

    // =========================================================
    // JSON 파싱을 위한 내부 중첩 레코드 (Nested Records)
    // =========================================================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResultNode(GameNode game) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GameNode(
            String statusCode,
            int homeTeamScore,
            int awayTeamScore,
            ScorersNode scorers
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ScorersNode(
            List<NaverScorer> home,
            List<NaverScorer> away
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NaverScorer(
            int time,
            int addedTime,
            String playerName,
            boolean ownGoal
    ) {
        // 우리 시스템의 Scorer 도메인 객체로 변환 (엔티티 구조에 맞게 수정하세요)
        public GoalScorer toDomain() {
            // 예: 전반 45분 + 추가시간 2분 -> "45+2" 또는 47분으로 합산 처리
            return new GoalScorer(playerName, time, addedTime, ownGoal);
        }
    }
}
