package com.example.pitchboxd.matchReview.service.domain;

import com.example.pitchboxd.match.dto.request.MatchReviewCreateRequest;
import com.example.pitchboxd.matchReview.domain.MatchReview;
import com.example.pitchboxd.matchReview.infrastructure.MatchReviewRepository;
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
}
