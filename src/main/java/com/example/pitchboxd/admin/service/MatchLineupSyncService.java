package com.example.pitchboxd.admin.service;

import com.example.pitchboxd.global.infrastructure.naver.NaverSportsMatchClient;
import com.example.pitchboxd.global.infrastructure.naver.dto.NaverLineupResponse;
import com.example.pitchboxd.global.infrastructure.naver.dto.NaverLineupResponse.NaverPlayerNode;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.service.domain.MatchService;
import com.example.pitchboxd.match.lineup.domain.MatchLineup;
import com.example.pitchboxd.match.lineup.domain.ParticipationStatus;
import com.example.pitchboxd.match.lineup.service.MatchLineupService;
import com.example.pitchboxd.player.domain.Player;
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
    private final MatchLineupService matchLineupService;
    private final NaverSportsMatchClient naverSportsClient;

    @Transactional
    public void syncLineup(String naverGameId) {
        Match match = matchService.findByNaverId(naverGameId);

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
            Player player = playerService.findByNaverId(node.playerId());
            lineups.add(createLineup(match.getId(), player.getId(), node, ParticipationStatus.STARTER));
        }
    }

    private void addSubstitutionLineup(List<MatchLineup> lineups, List<NaverPlayerNode> nodes, Match match) {
        for (NaverPlayerNode node : nodes) {
            Player player = playerService.findByNaverId(node.playerId());
            ParticipationStatus status =
                    node.changed() ? ParticipationStatus.SUBSTITUTED_IN : ParticipationStatus.BENCH;

            MatchLineup matchLineup = createLineup(match.getId(), player.getId(), node, status);
            lineups.add(matchLineup);
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
