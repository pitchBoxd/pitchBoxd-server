package com.example.pitchboxd.global.infrastructure.naver;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.global.infrastructure.naver.dto.NaverLineupResponse;
import com.example.pitchboxd.global.infrastructure.naver.dto.NaverMatchDetailResponse;
import com.example.pitchboxd.global.infrastructure.naver.dto.NaverScheduleWrapper;
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
    private static final String NAVER_GAMES_BASE_URL = "https://api-gw.sports.naver.com/schedule/games";
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
                    log.warn("네이버 api 호출 실패 (기간 내 게임 불러오기): {}", uri);
                    throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
                })
                .body(NaverScheduleWrapper.class);
    }

    public NaverMatchDetailResponse getMatchDetail(String gameCode) {
        return restClient.get()
                .uri(NAVER_GAMES_BASE_URL + "/{gameCode}", gameCode)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    log.warn("네이버 api 호출 실패 (게임 상세 데이터): 게임코드: {}", gameCode);
                    throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
                })
                .body(NaverMatchDetailResponse.class);
    }

    // ⭐️ 라인업 조회 메서드 추가
    public NaverLineupResponse getMatchLineup(String gameCode) {
        return restClient.get()
                .uri(NAVER_GAMES_BASE_URL + "/{gameCode}" + "/lineup", gameCode) // 엔드포인트는 상세 조회와 동일하지만, 파싱하는 DTO가 다릅니다.
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    log.warn("네이버 api 호출 실패 (라인업 조회): 게임코드: {}", gameCode);
                    throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
                })
                .body(NaverLineupResponse.class);
    }
}
