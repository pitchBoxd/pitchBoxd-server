package com.example.pitchboxd.match.matchReview.service.domain;

import com.example.pitchboxd.match.matchReview.domain.MatchReviewLike;
import com.example.pitchboxd.match.matchReview.infrastructure.MatchReviewLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchReviewLikeService {

    private final MatchReviewLikeRepository matchReviewLikeRepository;

    public boolean isLiked(Long matchReviewId, Long userId) {
        return matchReviewLikeRepository.existsByMatchReviewIdAndUserId(matchReviewId, userId);
    }

    @Transactional
    public void save(Long matchReviewId, Long userId) {
        MatchReviewLike matchReviewLike = new MatchReviewLike(matchReviewId, userId);
        matchReviewLikeRepository.save(matchReviewLike);
    }

    @Transactional
    public void delete(Long matchReviewId, Long userId) {
        matchReviewLikeRepository.deleteByMatchReviewIdAndUserId(matchReviewId, userId);
    }
}
