package com.portfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PortfolioApplication {

    public static void main(String[] args) {
        SpringApplication.run(PortfolioApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("  Indian Stock Portfolio Tracker STARTED");
        System.out.println("  Open in browser: http://localhost:8080");
        System.out.println("  Default Login   : admin / admin123");
        System.out.println("========================================\n");
    }
}
