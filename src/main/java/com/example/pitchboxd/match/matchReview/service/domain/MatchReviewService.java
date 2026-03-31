package com.example.pitchboxd.match.matchReview.service.domain;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.match.matchReview.domain.MatchReview;
import com.example.pitchboxd.match.matchReview.dto.request.MatchReviewCreateRequest;
import com.example.pitchboxd.match.matchReview.infrastructure.MatchReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchReviewService {

    private final MatchReviewRepository matchReviewRepository;

    @Transactional
    public MatchReview save(MatchReviewCreateRequest request, Long matchId, Long userId) {
        MatchReview matchReview = new MatchReview(matchId, userId, request.point(), request.content());

        return matchReviewRepository.save(matchReview);
    }

    public boolean isExist(Long matchId, Long userId) {
        return matchReviewRepository.existsByMatchIdAndUserId(matchId, userId);
    }

    public MatchReview findById(Long matchReviewId) {
        return matchReviewRepository.findById(matchReviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_REVIEW_NOT_FOUND));
    }

    public MatchReview findByIdForUpdate(Long matchReviewId) {
        return matchReviewRepository.findByIdWithPessimisticLock(matchReviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_REVIEW_NOT_FOUND));
    }
}
