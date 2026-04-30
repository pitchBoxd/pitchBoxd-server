package com.example.pitchboxd.admin.presentation;

import com.example.pitchboxd.admin.dto.request.CreateMatchRequest;
import com.example.pitchboxd.admin.dto.request.CreatePlayerRequest;
import com.example.pitchboxd.admin.dto.request.UpdateMatchRequest;
import com.example.pitchboxd.admin.dto.request.UpdatePlayerRequest;
import com.example.pitchboxd.admin.service.facade.AdminFacadeService;
import com.example.pitchboxd.global.dto.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin API", description = "Admin API 명세")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminFacadeService adminFacadeService;

    @Operation(summary = "K리그 전 경기 저장", description = "특정 날짜 구간 내의 K리그 모든 경기를 DB에 저장합니다.")
    @PostMapping("/sync-tasks/matches")
    public ResponseEntity<SuccessResponse<Void>> syncLeagueMatches(
            @RequestBody CreateMatchRequest request
    ) {
        adminFacadeService.syncMatchesAndStatistics(request);
        HttpStatus status = HttpStatus.CREATED;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }

    @Operation(summary = "K리그 선수 저장", description = "특정 시즌의 K리그 모든 선수를 DB에 저장합니다.")
    @PostMapping("/sync-tasks/players")
    public ResponseEntity<SuccessResponse<Void>> syncPlayers(
            @RequestBody CreatePlayerRequest request
    ) {
        adminFacadeService.syncPlayers(request);
        HttpStatus status = HttpStatus.CREATED;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }

    @Operation(summary = "경기 종료 및 업데이트", description = "특정 경기를 종료시키고 경기 결과를 업데이트합니다.")
    @PatchMapping("/matches/{naverId}:finish")
    public ResponseEntity<SuccessResponse<Void>> finishMatch(@PathVariable String naverId) {

        adminFacadeService.finishMatchAndUpdateLineup(naverId);
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }

    @Operation(summary = "경기 업데이트", description = "경기 정보를 업데이트합니다.")
    @PatchMapping("/matches/{matchId}")
    public ResponseEntity<SuccessResponse<Void>> updateMatch(
            @PathVariable Long matchId,
            @RequestBody UpdateMatchRequest request
    ) {

        adminFacadeService.updateMatch(matchId, request);
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }

    @Operation(summary = "선수 업데이트", description = "선수 정보를 업데이트합니다.")
    @PatchMapping("/players/{playerId}")
    public ResponseEntity<SuccessResponse<Void>> updatePlayer(
            @PathVariable Long playerId,
            @RequestBody UpdatePlayerRequest request
    ) {

        adminFacadeService.updatePlayer(playerId, request);
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }
}
