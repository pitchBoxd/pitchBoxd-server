package com.example.pitchboxd.team.service;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.team.domain.Team;
import com.example.pitchboxd.team.infrastructure.TeamRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TeamQueryService {

    private final TeamRepository teamRepository;

    public Team findByNaverCode(String naverCode) {
        return teamRepository.findByNaverId(naverCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));
    }

    public Team findById(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));
    }

    public List<Team> findAllTeam() {
        return teamRepository.findAll();
    }
}
