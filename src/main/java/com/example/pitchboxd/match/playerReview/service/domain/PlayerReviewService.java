package com.example.pitchboxd.match.playerReview.service.domain;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.match.playerReview.domain.PlayerReview;
import com.example.pitchboxd.match.playerReview.dto.request.PlayerReviewCreateRequest;
import com.example.pitchboxd.match.playerReview.infrastructure.PlayerReviewRepository;
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
        return playerReviewRepository.
                existsByMatchIdAndPlayerIdAndUserId(matchId, playerId, userId);
    }

    public PlayerReview findByIdForUpdate(Long playerReviewId) {
        return playerReviewRepository.findByIdWithPessimisticLock(playerReviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAYER_REVIEW_NOT_FOUND));
    }

    public PlayerReview findById(Long playerReviewId) {
        return playerReviewRepository.findById(playerReviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAYER_REVIEW_NOT_FOUND));
    }
}
