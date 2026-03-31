package com.example.pitchboxd.match.playerReview.service.domain;

import com.example.pitchboxd.match.playerReview.domain.PlayerReviewLike;
import com.example.pitchboxd.match.playerReview.infrastructure.PlayerReviewLikeRepository;
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
