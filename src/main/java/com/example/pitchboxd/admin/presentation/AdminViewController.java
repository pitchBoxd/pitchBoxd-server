package com.example.pitchboxd.admin.presentation;

import com.example.pitchboxd.admin.dto.response.AdminUserResponse;
import com.example.pitchboxd.admin.service.facade.AdminFacadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminViewController {

    private final AdminFacadeService adminFacadeService;

    @GetMapping
    public String dashboard(Model model) {
        List<AdminUserResponse> users = adminFacadeService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/dashboard";
    }
}
