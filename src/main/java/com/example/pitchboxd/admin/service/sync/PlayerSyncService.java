package com.example.pitchboxd.admin.service.sync;

import com.example.pitchboxd.global.infrastructure.naver.NaverSportsClient;
import com.example.pitchboxd.global.infrastructure.naver.dto.NaverPlayerResponse;
import com.example.pitchboxd.player.domain.Player;
import com.example.pitchboxd.player.service.PlayerService;
import com.example.pitchboxd.team.domain.Team;
import com.example.pitchboxd.team.service.TeamQueryService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PlayerSyncService {

    private final PlayerService playerService;
    private final TeamQueryService teamQueryService;
    private final NaverSportsClient naverSportsClient;
    
    public void syncAllPlayers(String season) {
        List<Team> allTeams = teamQueryService.findAllTeam();
        List<Player> playersToSave = new ArrayList<>();

        for (Team team : allTeams) {
            NaverPlayerResponse response = naverSportsClient.getPlayersByTeam(season, team.getNaverId());

            // 1. 외부 API에서 가져온 해당 팀 선수들의 네이버 ID 추출
            List<String> playerNaverId = response.getPlayers().stream()
                    .map(NaverPlayerResponse.NaverPlayerNode::playerId)
                    .toList();

            // 2. 이미 DB에 존재하는 선수 ID 조회 (IN 쿼리 방어막)
            Set<String> existingNaverIds = new HashSet<>(
                    playerService.findExistingNaverId(playerNaverId)
            );

            // 3. 신규 선수만 필터링하여 엔티티 변환
            List<Player> newPlayers = response.getPlayers().stream()
                    .filter(node -> !existingNaverIds.contains(node.playerId()))
                    .map(node -> new Player(team.getId(), node.playerName(), node.playerId()))
                    .toList();

            playersToSave.addAll(newPlayers);
        }

        // 4. 전체 신규 선수 일괄 저장
        if (!playersToSave.isEmpty()) {
            playerService.saveAll(playersToSave);
            log.info("시즌 {} 신규 선수 {}명 적재 완료", season, playersToSave.size());
        }
    }
}
