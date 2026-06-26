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
}
