package com.example.pitchboxd.global.infrastructure.naver.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NaverLineupResponse(LineUpDataNode lineUpData) {
    // 1. 홈팀 선발 명단 추출 (2D 배열을 1D 배열로 평탄화)
    public List<NaverPlayerNode> getHomeStarters() {
        return lineUpData.lineup().home().players().stream()
                .flatMap(List::stream)
                .toList();
    }

    // 2. 홈팀 교체/벤치 명단 추출
    public List<NaverPlayerNode> getHomeSubstitutions() {
        return lineUpData.substitution().home();
    }

    public List<NaverPlayerNode> getAwayStarters() {
        return lineUpData.lineup().away().players().stream()
                .flatMap(List::stream)
                .toList();
    }

    // 2. 홈팀 교체/벤치 명단 추출
    public List<NaverPlayerNode> getAwaySubstitutions() {
        return lineUpData.substitution().away();
    }

    // (away 팀도 동일한 방식으로 구성)

    // =========================================================
    // JSON 매핑용 내부 레코드
    // =========================================================
    public record LineUpDataNode(SubstitutionNode substitution, LineupNode lineup) {
    }

    public record SubstitutionNode(List<NaverPlayerNode> home, List<NaverPlayerNode> away) {
    }

    public record LineupNode(TeamLineupNode home, TeamLineupNode away) {
    }

    public record TeamLineupNode(List<List<NaverPlayerNode>> players) {
    } // ⭐️ 2D 배열 주의

    public record NaverPlayerNode(
            String playerId,  // 네이버의 선수 고유 ID ("20250023")
            String shirtNumber,
            String name,
            boolean changed
    ) {
    }
}
