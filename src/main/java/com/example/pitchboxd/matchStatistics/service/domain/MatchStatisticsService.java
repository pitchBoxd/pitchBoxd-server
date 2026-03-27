package com.example.pitchboxd.matchStatistics.service.domain;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.matchStatistics.domain.FanType;
import com.example.pitchboxd.matchStatistics.domain.MatchStatistics;
import com.example.pitchboxd.matchStatistics.infrastructure.MatchStatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchStatisticsService {

    private final MatchStatisticsRepository matchStatisticsRepository;

    /***
     * 경기 종료 직후 많은 사용자가 몰려서 충돌이 예상됨.
     * 그러면 낙관락보디 비관락이긴 한데 비관락의 단점인 커넥션 풀 고갈이 걱정되네.
     * 해결방법
     * 1. 결국 리뷰 저장 트랜잭션과 통계 업데이트 로직을 이벤트 기반 비동기로 찢어내야 함.
     * 2. 아니면, 리뷰가 N건 쌓이거나, M분 지나면 한꺼번에 처리하는 방식도 좋을듯.
     * ***/
    @Transactional
    public void updateReview(Long matchId, int rating, FanType fanType) {
        MatchStatistics matchStatistics = matchStatisticsRepository.findByIdForUpdate(matchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));

        matchStatistics.applyReview(rating, fanType);
    }
}
