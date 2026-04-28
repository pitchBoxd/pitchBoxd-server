package com.example.pitchboxd.season.service;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.season.domain.Season;
import com.example.pitchboxd.season.infrastructure.SeasonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SeasonQueryService {

    private final SeasonRepository seasonRepository;

    public Season findById(Long id) {
        return seasonRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SEASON_NOT_FOUND));
    }
}
