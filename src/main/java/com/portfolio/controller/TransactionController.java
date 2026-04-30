package com.portfolio.controller;

import com.portfolio.model.Stock;
import com.portfolio.model.Transaction;
import com.portfolio.model.TransactionType;
import com.portfolio.model.User;
import com.portfolio.repository.StockRepository;
import com.portfolio.service.PortfolioService;
import com.portfolio.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    private final PortfolioService portfolioService;
    private final UserService userService;
    private final StockRepository stockRepository;

    public TransactionController(PortfolioService portfolioService, UserService userService,
                                 StockRepository stockRepository) {
        this.portfolioService = portfolioService;
        this.userService = userService;
        this.stockRepository = stockRepository;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails principal, Model model) {
        User user = userService.getByUsername(principal.getUsername());
        List<Transaction> txns = portfolioService.getAllTransactions(user);
        model.addAttribute("user", user);
        model.addAttribute("transactions", txns);
        return "transactions";
    }

    @GetMapping("/add")
    public String addForm(@AuthenticationPrincipal UserDetails principal, Model model) {
        User user = userService.getByUsername(principal.getUsername());
        List<Stock> stocks = stockRepository.findAll();
        stocks.sort((a, b) -> a.getSymbol().compareTo(b.getSymbol()));
        model.addAttribute("user", user);
        model.addAttribute("stocks", stocks);
        model.addAttribute("today", LocalDate.now());
        return "add-transaction";
    }

    @PostMapping("/add")
    public String addTransaction(@AuthenticationPrincipal UserDetails principal,
                                  @RequestParam Long stockId,
                                  @RequestParam TransactionType type,
                                  @RequestParam Integer quantity,
                                  @RequestParam BigDecimal price,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                  Model model) {
        User user = userService.getByUsername(principal.getUsername());
        try {
            portfolioService.addTransaction(user, stockId, type, quantity, price, date);
            return "redirect:/dashboard";
        } catch (IllegalArgumentException e) {
            List<Stock> stocks = stockRepository.findAll();
            stocks.sort((a, b) -> a.getSymbol().compareTo(b.getSymbol()));
            model.addAttribute("user", user);
            model.addAttribute("stocks", stocks);
            model.addAttribute("today", LocalDate.now());
            model.addAttribute("error", e.getMessage());
            return "add-transaction";
        }
    }

    @PostMapping("/delete/{id}")
    public String delete(@AuthenticationPrincipal UserDetails principal, @PathVariable Long id) {
        User user = userService.getByUsername(principal.getUsername());
        portfolioService.deleteTransaction(user, id);
        return "redirect:/transactions";
    }
}
