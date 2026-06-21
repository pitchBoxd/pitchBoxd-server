package com.example.pitchboxd.admin.presentation;

import com.example.pitchboxd.admin.dto.response.AdminMatchResponse;
import com.example.pitchboxd.admin.dto.response.AdminUserResponse;
import com.example.pitchboxd.admin.service.facade.AdminFacadeService;
import com.example.pitchboxd.season.domain.Season;
import com.example.pitchboxd.season.service.SeasonQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminViewController {

    private final AdminFacadeService adminFacadeService;
    private final SeasonQueryService seasonQueryService;

    @GetMapping
    public String dashboard(
            @RequestParam(value = "seasonId", required = false) Long seasonId,
            Model model
    ) {
        List<Season> seasons = seasonQueryService.findAll();
        model.addAttribute("seasons", seasons);

        Long selectedSeasonId = seasonId;
        if (selectedSeasonId == null && !seasons.isEmpty()) {
            selectedSeasonId = seasons.get(seasons.size() - 1).getId(); // Default to latest season
        }
        model.addAttribute("selectedSeasonId", selectedSeasonId);

        List<AdminUserResponse> users = adminFacadeService.getAllUsers();
        model.addAttribute("users", users);

        List<AdminMatchResponse> matches = List.of();
        if (selectedSeasonId != null) {
            matches = adminFacadeService.getMatchesBySeason(selectedSeasonId);
        }
        model.addAttribute("matches", matches);

        return "admin/dashboard";
    }
}
