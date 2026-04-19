package com.example.pitchboxd.match.core.infrastructure.external.dto;

import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchResult;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NaverMatchResponse(
        String gameId,             // 네이버 측 고유 경기 ID
        LocalDateTime gameDateTime,// 경기 일시 ("2026-02-28T14:00:00" 형태라 자동 매핑됨)
        String stadium,            // 경기장 ("인천 전용")
        String homeTeamName,       // 홈팀 ("인천")
        Long homeTeamCode,
        int homeTeamScore,         // 홈팀 스코어
        String awayTeamName,       // 원정팀 ("서울")
        Long awayTeamCode,
        int awayTeamScore,         // 원정팀 스코어
        String statusCode,         // 상태 코드 ("RESULT" = 종료)
        String statusInfo,         // 한글 상태 ("경기종료")
        String matchRound          // 라운드 ("1")
) {

    public Match toMatch() {
        MatchStatus matchStatus = MatchStatus.SCHEDULED;
        Match match = new Match(1L, matchRound, homeTeamCode, awayTeamCode, gameDateTime, matchStatus, stadium, gameId);
        
        if (statusCode.equals("RESULT")) {
            MatchResult matchResult = new MatchResult(homeTeamScore, awayTeamScore, List.of(), List.of());
            match.finish(gameDateTime.plusHours(2));
            match.decideMatchResult(matchResult);
        }

        return match;
    }
}
