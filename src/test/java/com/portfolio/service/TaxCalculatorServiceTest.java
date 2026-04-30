package com.portfolio.service;

import com.portfolio.dto.TaxReportDto;
import com.portfolio.model.*;
import com.portfolio.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TaxCalculatorServiceTest {

    private TransactionRepository txnRepo;
    private TaxCalculatorService taxService;
    private User user;
    private Stock reliance;

    @BeforeEach
    void setUp() {
        txnRepo = mock(TransactionRepository.class);
        taxService = new TaxCalculatorService(txnRepo);
        user = new User("test", "pwd", "t@t.com", "Test");
        user.setId(1L);
        reliance = new Stock(1L, "RELIANCE", "Reliance", "NSE", "Energy", new BigDecimal("3000"));
    }

    @Test
    @DisplayName("STCG: Buy and sell within 12 months → 15% tax")
    void stcg_within_oneYear() {
        Transaction buy = new Transaction(user, reliance, TransactionType.BUY, 100,
                new BigDecimal("2000"), LocalDate.of(2024, 5, 1));
        Transaction sell = new Transaction(user, reliance, TransactionType.SELL, 100,
                new BigDecimal("2500"), LocalDate.of(2024, 10, 1));

        when(txnRepo.findByUserAndTypeAndDateBetween(eq(user), eq(TransactionType.SELL), any(), any()))
                .thenReturn(Collections.singletonList(sell));
        when(txnRepo.findByUserAndStockIdOrderByTransactionDateAsc(user, 1L))
                .thenReturn(Arrays.asList(buy, sell));

        TaxReportDto report = taxService.calculateTax(user, 2024);
        // Gain = (2500 - 2000) * 100 = 50,000
        assertEquals(new BigDecimal("50000.00"), report.getStcgGains());
        // Tax = 50,000 * 0.15 = 7,500
        assertEquals(new BigDecimal("7500.00"), report.getStcgTax());
        assertEquals(BigDecimal.ZERO.setScale(2), report.getLtcgGains());
    }

    @Test
    @DisplayName("LTCG: Held > 1 year, gain < ₹1L → no tax")
    void ltcg_belowExemption() {
        Transaction buy = new Transaction(user, reliance, TransactionType.BUY, 100,
                new BigDecimal("2000"), LocalDate.of(2023, 1, 1));
        Transaction sell = new Transaction(user, reliance, TransactionType.SELL, 100,
                new BigDecimal("2800"), LocalDate.of(2024, 6, 1));

        when(txnRepo.findByUserAndTypeAndDateBetween(eq(user), eq(TransactionType.SELL), any(), any()))
                .thenReturn(Collections.singletonList(sell));
        when(txnRepo.findByUserAndStockIdOrderByTransactionDateAsc(user, 1L))
                .thenReturn(Arrays.asList(buy, sell));

        TaxReportDto report = taxService.calculateTax(user, 2024);
        // Gain = (2800 - 2000) * 100 = 80,000 < 1L exemption
        assertEquals(new BigDecimal("80000.00"), report.getLtcgGains());
        assertEquals(BigDecimal.ZERO.setScale(2), report.getLtcgTaxable());
        assertEquals(BigDecimal.ZERO.setScale(2), report.getLtcgTax());
    }

    @Test
    @DisplayName("LTCG: Gain > ₹1L → 10% on excess")
    void ltcg_aboveExemption() {
        Transaction buy = new Transaction(user, reliance, TransactionType.BUY, 100,
                new BigDecimal("2000"), LocalDate.of(2023, 1, 1));
        Transaction sell = new Transaction(user, reliance, TransactionType.SELL, 100,
                new BigDecimal("4000"), LocalDate.of(2024, 6, 1));

        when(txnRepo.findByUserAndTypeAndDateBetween(eq(user), eq(TransactionType.SELL), any(), any()))
                .thenReturn(Collections.singletonList(sell));
        when(txnRepo.findByUserAndStockIdOrderByTransactionDateAsc(user, 1L))
                .thenReturn(Arrays.asList(buy, sell));

        TaxReportDto report = taxService.calculateTax(user, 2024);
        // Gain = 200,000; Taxable = 200,000 - 100,000 = 100,000
        assertEquals(new BigDecimal("200000.00"), report.getLtcgGains());
        assertEquals(new BigDecimal("100000.00"), report.getLtcgTaxable());
        // Tax = 100,000 * 0.10 = 10,000
        assertEquals(new BigDecimal("10000.00"), report.getLtcgTax());
    }

    @Test
    @DisplayName("FIFO: Multiple buys, partial sell uses oldest first")
    void fifo_partialSell() {
        Transaction buy1 = new Transaction(user, reliance, TransactionType.BUY, 100,
                new BigDecimal("2000"), LocalDate.of(2024, 1, 1));
        Transaction buy2 = new Transaction(user, reliance, TransactionType.BUY, 100,
                new BigDecimal("2200"), LocalDate.of(2024, 5, 1));
        Transaction sell = new Transaction(user, reliance, TransactionType.SELL, 50,
                new BigDecimal("2500"), LocalDate.of(2024, 10, 1));

        when(txnRepo.findByUserAndTypeAndDateBetween(eq(user), eq(TransactionType.SELL), any(), any()))
                .thenReturn(Collections.singletonList(sell));
        when(txnRepo.findByUserAndStockIdOrderByTransactionDateAsc(user, 1L))
                .thenReturn(Arrays.asList(buy1, buy2, sell));

        TaxReportDto report = taxService.calculateTax(user, 2024);
        // FIFO: 50 shares matched against buy1 (Jan 2024)
        // Gain = (2500 - 2000) * 50 = 25,000 STCG (Jan to Oct = ~9 months < 1yr)
        assertEquals(new BigDecimal("25000.00"), report.getStcgGains());
        assertEquals(1, report.getLots().size());
        assertEquals("STCG", report.getLots().get(0).getType());
    }
}
