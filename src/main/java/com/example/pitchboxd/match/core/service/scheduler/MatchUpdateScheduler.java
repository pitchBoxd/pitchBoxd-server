package com.example.pitchboxd.match.core.service.scheduler;

import com.example.pitchboxd.global.domain.ClockHolder;
import com.example.pitchboxd.global.infrastructure.naver.NaverSportsClient;
import com.example.pitchboxd.global.infrastructure.naver.dto.NaverMatchDetailResponse;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.domain.MatchStatus;
import com.example.pitchboxd.match.core.service.domain.MatchService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchUpdateScheduler {

    private final MatchService matchService;
    private final NaverSportsClient naverSportsClient;
    private final ClockHolder clockHolder;

    /**
     * 5분마다 db를 조회해, 오늘 경기 && 경기 시작 시간에서 105분이 지났다면 종료됐는지 확인 후 종료시킵니다.
     */
    @Scheduled(cron = "0 0/5 * * * *")
    public void updateFinishedMatches() {
        LocalDateTime now = clockHolder.now();
        LocalDateTime checkTime = now.minusMinutes(105);
        LocalDateTime timeLimit = now.minusDays(1);

        List<Match> matches = matchService.findByMatchStatusAndStartTimeBetween(MatchStatus.SCHEDULED, timeLimit,
                checkTime);

        for (Match match : matches) {
            updateFinishedMatch(match, now);
        }
    }

    private void updateFinishedMatch(Match match, LocalDateTime now) {
        try {
            String naverId = match.getNaverId();
            NaverMatchDetailResponse matchDetailResponse = naverSportsClient.getMatchDetail(naverId);

            if (matchDetailResponse.isFinished()) {
                matchService.updateMatchResult(match.getId(), matchDetailResponse.toMatchResult(), now);
            }
        } catch (Exception e) {
            log.error("경기 업데이트 중 오류 발생: matchId={}, error={}", match.getId(), e.getMessage());
        }
    }
}
