package com.example.pitchboxd.match.infrastructure.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * 네이버 API의 최상위 JSON 응답 구조를 매핑하는 Wrapper Record
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NaverScheduleWrapper(
        NaverScheduleResult result
) {
    /**
     * 서비스 레이어에서 깊게 체이닝(wrapper.result().games())하지 않도록 편의 메서드 제공
     */
    public List<NaverMatchResponse> getMatches() {
        if (this.result == null || this.result.games() == null) {
            return List.of(); // NullPointerException 방어
        }
        return this.result.games();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NaverScheduleResult(
            // 실제 JSON 내에서 방금 우리가 만든 DTO 배열이 담긴 키 이름 (보통 'games' 또는 'model' 등)
            List<NaverMatchResponse> games
    ) {
    }
}