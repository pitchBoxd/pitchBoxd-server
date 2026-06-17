package com.example.pitchboxd.admin.service.sync;

import com.example.pitchboxd.global.infrastructure.naver.NaverSportsClient;
import com.example.pitchboxd.global.infrastructure.naver.dto.NaverLineupResponse;
import com.example.pitchboxd.global.infrastructure.naver.dto.NaverLineupResponse.NaverPlayerNode;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.service.domain.MatchService;
import com.example.pitchboxd.match.lineup.domain.MatchLineup;
import com.example.pitchboxd.match.lineup.domain.ParticipationStatus;
import com.example.pitchboxd.match.lineup.service.MatchLineupService;
import com.example.pitchboxd.player.domain.Player;
import com.example.pitchboxd.player.infrastructure.PlayerRepository;
import com.example.pitchboxd.player.service.PlayerService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchLineupSyncService {

    private final MatchService matchService;
    private final PlayerService playerService;
    private final PlayerRepository playerRepository;
    private final MatchLineupService matchLineupService;
    private final NaverSportsClient naverSportsClient;

    @Transactional
    public void syncLineup(String naverGameId) {
        Match match = matchService.findByNaverId(naverGameId);

        NaverScheduleWrapperExternalResponseHelper(naverGameId, match);
    }

    private void NaverScheduleWrapperExternalResponseHelper(String naverGameId, Match match) {
        NaverLineupResponse response = naverSportsClient.getMatchLineup(naverGameId);

        List<MatchLineup> lineups = new ArrayList<>();

        addStaterLineup(lineups, response.getHomeStarters(), match);
        addSubstitutionLineup(lineups, response.getHomeSubstitutions(), match);

        addStaterLineup(lineups, response.getAwayStarters(), match);
        addSubstitutionLineup(lineups, response.getAwaySubstitutions(), match);

        matchLineupService.createAllMatchLineup(lineups);
    }

    private void addStaterLineup(List<MatchLineup> lineups, List<NaverPlayerNode> nodes, Match match) {
        for (NaverPlayerNode node : nodes) {
            playerRepository.findByNaverId(node.playerId()).ifPresentOrElse(
                player -> lineups.add(createLineup(match.getId(), player.getId(), node, ParticipationStatus.STARTER)),
                () -> log.warn("선발 라인업 동기화 제외 - DB에 존재하지 않는 선수입니다. (선수 ID: {}, 이름: {})", node.playerId(), node.name())
            );
        }
    }

    private void addSubstitutionLineup(List<MatchLineup> lineups, List<NaverPlayerNode> nodes, Match match) {
        for (NaverPlayerNode node : nodes) {
            playerRepository.findByNaverId(node.playerId()).ifPresentOrElse(
                player -> {
                    ParticipationStatus status =
                            node.changed() ? ParticipationStatus.SUBSTITUTED_IN : ParticipationStatus.BENCH;

                    MatchLineup matchLineup = createLineup(match.getId(), player.getId(), node, status);
                    lineups.add(matchLineup);
                },
                () -> log.warn("교체 라인업 동기화 제외 - DB에 존재하지 않는 선수입니다. (선수 ID: {}, 이름: {})", node.playerId(), node.name())
            );
        }
    }

    private MatchLineup createLineup(Long matchId, Long playerId, NaverPlayerNode node, ParticipationStatus status) {
        // 등번호가 없는 경우를 대비한 방어 로직 (문자열 -> 정수 변환)
        Integer shirtNumber = null;
        try {
            if (node.shirtNumber() != null && !node.shirtNumber().isBlank()) {
                shirtNumber = Integer.parseInt(node.shirtNumber());
            }
        } catch (NumberFormatException e) {
            log.warn("등번호 파싱 실패 - 선수: {}, 번호: {}", node.name(), node.shirtNumber());
        }

        return new MatchLineup(matchId, playerId, shirtNumber, status);
    }
}
