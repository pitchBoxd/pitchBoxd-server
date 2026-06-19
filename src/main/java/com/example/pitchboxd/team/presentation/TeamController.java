package com.example.pitchboxd.team.presentation;

import com.example.pitchboxd.global.dto.response.SuccessResponse;
import com.example.pitchboxd.team.dto.response.TeamResponses;
import com.example.pitchboxd.team.service.TeamQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.pitchboxd.team.dto.response.TeamFollowerCountResponses;

import com.example.pitchboxd.team.dto.response.TeamDetailResponses;

@Tag(name = "Team API", description = "Team API 명세")
@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamQueryService teamQueryService;

    @Operation(summary = "모든 팀 조회", description = "모든 팀을 조회합니다.")
    @GetMapping
    public ResponseEntity<SuccessResponse<TeamResponses>> getAllTeams() {
        TeamResponses responses = TeamResponses.of(teamQueryService.findAllTeam());
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, responses));
    }

    @Operation(summary = "팀별 팔로워 수 조회", description = "팀별 팔로워(응원 유저 수) 수를 조회합니다.")
    @GetMapping("/followers")
    public ResponseEntity<SuccessResponse<TeamFollowerCountResponses>> getTeamFollowerCounts() {
        TeamFollowerCountResponses responses = new TeamFollowerCountResponses(teamQueryService.findTeamFollowerCounts());
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, responses));
    }

    @Operation(summary = "종합 팀 정보 조회", description = "팀 명, 팀 id, 홈 경기장, 팔로워 숫자를 포함한 종합 팀 정보를 조회합니다.")
    @GetMapping("/details")
    public ResponseEntity<SuccessResponse<TeamDetailResponses>> getTeamDetails() {
        TeamDetailResponses responses = TeamDetailResponses.of(teamQueryService.findTeamDetails());
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, responses));
    }
}
