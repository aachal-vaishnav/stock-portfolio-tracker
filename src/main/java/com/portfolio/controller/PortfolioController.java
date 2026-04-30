package com.portfolio.controller;

import com.portfolio.dto.PortfolioSummaryDto;
import com.portfolio.model.User;
import com.portfolio.service.PortfolioService;
import com.portfolio.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final UserService userService;

    public PortfolioController(PortfolioService portfolioService, UserService userService) {
        this.portfolioService = portfolioService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails principal, Model model) {
        User user = userService.getByUsername(principal.getUsername());
        PortfolioSummaryDto summary = portfolioService.getPortfolioSummary(user);
        model.addAttribute("user", user);
        model.addAttribute("summary", summary);
        return "dashboard";
    }
}
