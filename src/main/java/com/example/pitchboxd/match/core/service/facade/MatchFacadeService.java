package com.example.pitchboxd.match.core.service.facade;

import com.example.pitchboxd.match.core.service.domain.MatchService;
import com.example.pitchboxd.match.matchReview.service.domain.MatchReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchFacadeService {

    private final MatchService matchService;
    private final MatchReviewService matchReviewService;
}
