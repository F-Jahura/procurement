package com.fatema.procurement.controller;

import com.fatema.procurement.dto.DashboardStats;
import com.fatema.procurement.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/")
    public String home(Model model) {
        DashboardStats stats = dashboardService.getStats();
        model.addAttribute("stats", stats);
        return "dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        return home(model);
    }

    @GetMapping("/purchases")
    public String purchases() {
        return "purchases";
    }

    @GetMapping("/sales")
    public String sales() {
        return "sales";
    }

    // Страница входа (не перенаправляем, а показываем форму)
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/auth/login")
    public String authLogin() {
        return "login";
    }

    @GetMapping("/auth/logout")
    public String authLogout() {
        return "redirect:/logout";
    }
}
