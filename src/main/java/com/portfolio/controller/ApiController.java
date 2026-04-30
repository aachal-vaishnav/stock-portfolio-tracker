package com.portfolio.controller;

import com.portfolio.dto.HoldingDto;
import com.portfolio.dto.PortfolioSummaryDto;
import com.portfolio.model.User;
import com.portfolio.service.PortfolioService;
import com.portfolio.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * REST endpoints for AJAX dashboard updates (live prices, charts).
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    private final PortfolioService portfolioService;
    private final UserService userService;

    public ApiController(PortfolioService portfolioService, UserService userService) {
        this.portfolioService = portfolioService;
        this.userService = userService;
    }

    @GetMapping("/portfolio/summary")
    public Map<String, Object> summary(@AuthenticationPrincipal UserDetails principal) {
        User user = userService.getByUsername(principal.getUsername());
        PortfolioSummaryDto s = portfolioService.getPortfolioSummary(user);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalInvested", s.getTotalInvested());
        result.put("currentValue", s.getCurrentValue());
        result.put("totalPnl", s.getTotalPnl());
        result.put("totalPnlPercent", s.getTotalPnlPercent());
        result.put("holdingsCount", s.getHoldingsCount());
        return result;
    }

    @GetMapping("/portfolio/allocation")
    public Map<String, Object> allocation(@AuthenticationPrincipal UserDetails principal) {
        User user = userService.getByUsername(principal.getUsername());
        PortfolioSummaryDto s = portfolioService.getPortfolioSummary(user);

        // By stock
        Map<String, Object> bySymbol = new LinkedHashMap<>();
        for (HoldingDto h : s.getHoldings()) {
            bySymbol.put(h.getSymbol(), h.getCurrentValue());
        }

        // By sector
        Map<String, java.math.BigDecimal> bySector = new HashMap<>();
        for (HoldingDto h : s.getHoldings()) {
            String sector = h.getSector() != null ? h.getSector() : "Other";
            bySector.merge(sector, h.getCurrentValue(), java.math.BigDecimal::add);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("bySymbol", bySymbol);
        result.put("bySector", bySector);
        return result;
    }
}
