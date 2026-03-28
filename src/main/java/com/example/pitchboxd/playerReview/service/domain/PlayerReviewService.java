package com.example.pitchboxd.playerReview.service.domain;

import com.example.pitchboxd.playerReview.domain.PlayerReview;
import com.example.pitchboxd.playerReview.dto.request.PlayerReviewCreateRequest;
import com.example.pitchboxd.playerReview.infrastructure.PlayerReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlayerReviewService {

    private final PlayerReviewRepository playerReviewRepository;

    public PlayerReview save(PlayerReviewCreateRequest request, Long matchId, Long userId) {
        PlayerReview playerReview = new PlayerReview(matchId, request.playerId(), userId, request.point(),
                request.content());

        return playerReviewRepository.save(playerReview);
    }

    public boolean hasAlreadyReviewed(Long matchId, Long playerId, Long userId) {
        return playerReviewRepository.existsByMatchIdAndPlayerIdAndUserId(matchId, playerId, userId);
    }
}
