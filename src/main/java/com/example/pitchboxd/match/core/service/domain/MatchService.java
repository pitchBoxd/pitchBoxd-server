package com.example.pitchboxd.match.core.service.domain;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.match.core.domain.Match;
import com.example.pitchboxd.match.core.infrastructure.MatchRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MatchService {

    private final MatchRepository matchRepository;

    public Match findById(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));
    }

    public Match findByNaverId(String naverId) {
        return matchRepository.findByNaverId(naverId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));
    }

    @Transactional
    public List<Match> createAllMatches(List<Match> matches) {
        return matchRepository.saveAll(matches);
    }
}
