package com.example.pitchboxd.match.infrastructure.external;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.match.infrastructure.external.dto.NaverScheduleWrapper;
import java.net.URI;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverSportsMatchClient {

    // 네이버 스포츠 일정 API의 기본 엔드포인트
    // 사용자님이 주신 베이스 주소
    private static final String NAVER_GAMES_BASE_URL = "https://api-gw.sports.naver.com/schedule/games";
    // 사용자님이 직접 검증하신 그 방대한 필드 목록들 (오타 방지를 위해 그대로 복사 권장)
    private static final String NAVER_FIELDS = "basic,schedule,matchRound,roundTournamentInfo,phaseCode,groupName,leg,hasPtSore,homePtScore,awayPtScore,league,leagueName,aggregateWinner,neutralGround,postponed,manualRelayUrl";

    private final RestClient restClient;

    /**
     * 특정 연월의 K리그
     *
     * @param from 시작날짜
     * @param to   종료날짜
     */
    public NaverScheduleWrapper fetchMatches(LocalDate from, LocalDate to) {
        // 1. UriComponentsBuilder 클래스의 정적 메서드로 URI 객체 생성
        URI uri = UriComponentsBuilder.fromUriString(NAVER_GAMES_BASE_URL)
                .queryParam("fields", NAVER_FIELDS)
                .queryParam("upperCategoryId", "kfootball")
                .queryParam("categoryId", "kleague")
                .queryParam("fromDate", from.toString())
                .queryParam("toDate", to.toString())
                .queryParam("roundCodes", "")
                .queryParam("size", "500") // 넉넉하게 500개
                .build()
                .toUri();

        // 2. 완성된 URI로 요청 전송
        return restClient.get()
                .uri(uri)
                .header(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header(HttpHeaders.REFERER, "https://sports.naver.com/")
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    log.warn("네이버 api 호출 실패 {%s}", uri);
                    throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
                })
                .body(NaverScheduleWrapper.class);
    }
}
