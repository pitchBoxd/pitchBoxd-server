package com.example.pitchboxd.match.matchReview.service.domain;

import com.example.pitchboxd.match.matchReview.domain.MatchReviewLike;
import com.example.pitchboxd.match.matchReview.infrastructure.MatchReviewLikeRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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

    public Map<Long, Boolean> checkLikedStatusForReviews(List<Long> matchReviewIds, Long userId) {

        if (matchReviewIds == null || matchReviewIds.isEmpty()) {
            return Collections.emptyMap();
        }

        if (userId == null) {
            return matchReviewIds.stream()
                    .collect(Collectors.toMap(
                            reviewId -> reviewId,
                            reviewId -> false
                    ));
        }

        List<MatchReviewLike> likes = matchReviewLikeRepository.findByUserIdAndMatchReviewIdIn(userId, matchReviewIds);

        Set<Long> likedReviewIds = likes.stream()
                .map(MatchReviewLike::getMatchReviewId)
                .collect(Collectors.toSet());

        return matchReviewIds.stream()
                .collect(Collectors.toMap(
                        matchReviewId -> matchReviewId,
                        likedReviewIds::contains
                ));
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
