package com.example.pitchboxd.admin.presnetation;

import com.example.pitchboxd.admin.dto.request.CreateMatchRequest;
import com.example.pitchboxd.admin.dto.request.CreatePlayerRequest;
import com.example.pitchboxd.admin.service.facade.AdminFacadeService;
import com.example.pitchboxd.global.dto.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminFacadeService adminFacadeService;

    @PostMapping("/matches")
    public ResponseEntity<SuccessResponse<Void>> syncLeagueMatches(
            @RequestBody CreateMatchRequest request
    ) {
        adminFacadeService.syncMatchesAndStatistics(request);
        HttpStatus status = HttpStatus.CREATED;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }

    @PostMapping("/players")
    public ResponseEntity<SuccessResponse<Void>> syncPlayers(
            @RequestBody CreatePlayerRequest request
    ) {
        adminFacadeService.syncPlayers(request);
        HttpStatus status = HttpStatus.CREATED;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }

    @PatchMapping("/matches/{naverId}")
    public ResponseEntity<SuccessResponse<Void>> finishMatch(@PathVariable String naverId) {

        adminFacadeService.finishMatchAndUpdateLineup(naverId);
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
    }
}
