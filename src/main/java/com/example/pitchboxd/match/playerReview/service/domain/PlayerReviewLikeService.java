package com.example.pitchboxd.match.playerReview.service.domain;

import com.example.pitchboxd.match.playerReview.domain.PlayerReviewLike;
import com.example.pitchboxd.match.playerReview.infrastructure.PlayerReviewLikeRepository;
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
public class PlayerReviewLikeService {

    private final PlayerReviewLikeRepository playerReviewLikeRepository;

    public boolean isLiked(Long playerReviewId, Long userId) {
        return playerReviewLikeRepository.existsByPlayerReviewIdAndUserId(playerReviewId, userId);
    }

    public Map<Long, Boolean> checkLikedStatusForReviews(List<Long> playerReviewIds, Long userId) {
        if (playerReviewIds == null || playerReviewIds.isEmpty()) {
            return Collections.emptyMap();
        }
        if (userId == null) {
            return playerReviewIds.stream().collect(Collectors.toMap(id -> id, id -> false));
        }
        Set<Long> likedIds = new java.util.HashSet<>(
                playerReviewLikeRepository.findLikedReviewIdsIn(playerReviewIds, userId)
        );
        return playerReviewIds.stream().collect(Collectors.toMap(id -> id, likedIds::contains));
    }

    @Transactional
    public void save(Long playerReviewId, Long userId) {
        PlayerReviewLike playerReviewLike = new PlayerReviewLike(playerReviewId, userId);
        playerReviewLikeRepository.save(playerReviewLike);
    }

    @Transactional
    public void delete(Long playerReviewId, Long userId) {
        playerReviewLikeRepository.deleteByPlayerReviewIdAndUserId(playerReviewId, userId);
    }
}
