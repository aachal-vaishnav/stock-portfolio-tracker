package com.portfolio.service;

import com.portfolio.dto.TaxReportDto;
import com.portfolio.model.Stock;
import com.portfolio.model.Transaction;
import com.portfolio.model.TransactionType;
import com.portfolio.model.User;
import com.portfolio.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Indian Capital Gains Tax Calculator.
 *
 * Rules (FY 2024-25):
 *   - STCG (Short-term, holding < 12 months on equities): 15% flat
 *   - LTCG (Long-term, holding >= 12 months on equities): 10%
 *     with Rs. 1,00,000 per Financial Year exemption
 *
 * Uses FIFO (First-In-First-Out) accounting as per Indian Income Tax rules.
 */
@Service
public class TaxCalculatorService {

    private static final BigDecimal STCG_RATE = new BigDecimal("0.15");
    private static final BigDecimal LTCG_RATE = new BigDecimal("0.10");
    private static final BigDecimal LTCG_EXEMPTION = new BigDecimal("100000");
    private static final long LONG_TERM_DAYS = 365;

    private final TransactionRepository transactionRepository;

    public TaxCalculatorService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public TaxReportDto calculateTax(User user, int financialYear) {
        // Indian FY: April 1 to March 31
        LocalDate fyStart = LocalDate.of(financialYear, 4, 1);
        LocalDate fyEnd = LocalDate.of(financialYear + 1, 3, 31);

        TaxReportDto report = new TaxReportDto();
        report.setFinancialYear(financialYear);

        // Get all SELLs in this FY
        List<Transaction> sells = transactionRepository
                .findByUserAndTypeAndDateBetween(user, TransactionType.SELL, fyStart, fyEnd);

        BigDecimal stcgGains = BigDecimal.ZERO;
        BigDecimal ltcgGains = BigDecimal.ZERO;
        List<TaxReportDto.TaxLot> lots = new ArrayList<>();

        // Group sells by stock for FIFO matching
        Map<Long, List<Transaction>> sellsByStock = new HashMap<>();
        for (Transaction sell : sells) {
            sellsByStock.computeIfAbsent(sell.getStock().getId(), k -> new ArrayList<>()).add(sell);
        }

        for (Map.Entry<Long, List<Transaction>> entry : sellsByStock.entrySet()) {
            Long stockId = entry.getKey();
            List<Transaction> stockSells = entry.getValue();

            // Get ALL transactions for this stock (need buys before FY too)
            List<Transaction> allTxns = transactionRepository
                    .findByUserAndStockIdOrderByTransactionDateAsc(user, stockId);

            // Build FIFO buy queue
            Deque<BuyLot> buyQueue = new ArrayDeque<>();
            for (Transaction t : allTxns) {
                if (t.getType() == TransactionType.BUY) {
                    buyQueue.addLast(new BuyLot(t.getQuantity(), t.getPrice(), t.getTransactionDate()));
                } else if (t.getType() == TransactionType.SELL) {
                    boolean isInThisFy = !t.getTransactionDate().isBefore(fyStart)
                                       && !t.getTransactionDate().isAfter(fyEnd);
                    int sellQty = t.getQuantity();
                    BigDecimal sellPrice = t.getPrice();
                    LocalDate sellDate = t.getTransactionDate();

                    while (sellQty > 0 && !buyQueue.isEmpty()) {
                        BuyLot lot = buyQueue.peekFirst();
                        int matchQty = Math.min(sellQty, lot.qty);
                        long holdingDays = ChronoUnit.DAYS.between(lot.date, sellDate);
                        BigDecimal gain = sellPrice.subtract(lot.price)
                                .multiply(BigDecimal.valueOf(matchQty))
                                .setScale(2, RoundingMode.HALF_UP);

                        if (isInThisFy) {
                            String type;
                            if (holdingDays >= LONG_TERM_DAYS) {
                                ltcgGains = ltcgGains.add(gain);
                                type = "LTCG";
                            } else {
                                stcgGains = stcgGains.add(gain);
                                type = "STCG";
                            }
                            lots.add(new TaxReportDto.TaxLot(
                                    t.getStock().getSymbol(), matchQty,
                                    lot.price, sellPrice, lot.date, sellDate,
                                    holdingDays, gain, type));
                        }

                        lot.qty -= matchQty;
                        sellQty -= matchQty;
                        if (lot.qty == 0) buyQueue.pollFirst();
                    }
                }
            }
        }

        // Apply LTCG exemption
        BigDecimal ltcgTaxable = ltcgGains.compareTo(LTCG_EXEMPTION) > 0
                ? ltcgGains.subtract(LTCG_EXEMPTION)
                : BigDecimal.ZERO;
        BigDecimal ltcgTax = ltcgTaxable.multiply(LTCG_RATE).setScale(2, RoundingMode.HALF_UP);

        // STCG (no exemption)
        BigDecimal stcgTax = stcgGains.compareTo(BigDecimal.ZERO) > 0
                ? stcgGains.multiply(STCG_RATE).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal totalTax = stcgTax.add(ltcgTax);

        report.setStcgGains(stcgGains.setScale(2, RoundingMode.HALF_UP));
        report.setStcgTax(stcgTax);
        report.setLtcgGains(ltcgGains.setScale(2, RoundingMode.HALF_UP));
        report.setLtcgExemption(LTCG_EXEMPTION);
        report.setLtcgTaxable(ltcgTaxable.setScale(2, RoundingMode.HALF_UP));
        report.setLtcgTax(ltcgTax);
        report.setTotalTax(totalTax);
        report.setLots(lots);
        return report;
    }

    private static class BuyLot {
        int qty;
        final BigDecimal price;
        final LocalDate date;

        BuyLot(int qty, BigDecimal price, LocalDate date) {
            this.qty = qty;
            this.price = price;
            this.date = date;
        }
    }
}
