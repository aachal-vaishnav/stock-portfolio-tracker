package com.portfolio.service;

import com.portfolio.dto.HoldingDto;
import com.portfolio.dto.PortfolioSummaryDto;
import com.portfolio.model.Stock;
import com.portfolio.model.Transaction;
import com.portfolio.model.TransactionType;
import com.portfolio.model.User;
import com.portfolio.repository.StockRepository;
import com.portfolio.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
public class PortfolioService {

    private final TransactionRepository transactionRepository;
    private final StockRepository stockRepository;

    public PortfolioService(TransactionRepository transactionRepository,
                            StockRepository stockRepository) {
        this.transactionRepository = transactionRepository;
        this.stockRepository = stockRepository;
    }

    @Transactional
    public Transaction addTransaction(User user, Long stockId, TransactionType type,
                                      int quantity, BigDecimal price, LocalDate date) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found"));

        // For SELL, validate user has enough quantity
        if (type == TransactionType.SELL) {
            int currentQty = computeQuantityHeld(user, stockId);
            if (currentQty < quantity) {
                throw new IllegalArgumentException(
                    "Cannot sell " + quantity + " shares of " + stock.getSymbol() +
                    ". You only hold " + currentQty + " shares.");
            }
        }

        Transaction t = new Transaction(user, stock, type, quantity, price, date);
        return transactionRepository.save(t);
    }

    public PortfolioSummaryDto getPortfolioSummary(User user) {
        List<Transaction> all = transactionRepository.findByUserOrderByTransactionDateDesc(user);
        Map<Long, List<Transaction>> byStock = new HashMap<>();
        for (Transaction t : all) {
            byStock.computeIfAbsent(t.getStock().getId(), k -> new ArrayList<>()).add(t);
        }

        List<HoldingDto> holdings = new ArrayList<>();
        for (Map.Entry<Long, List<Transaction>> entry : byStock.entrySet()) {
            HoldingDto h = computeHolding(entry.getValue());
            if (h != null && h.getQuantity() > 0) {
                holdings.add(h);
            }
        }

        // Sort by current value descending
        holdings.sort((a, b) -> b.getCurrentValue().compareTo(a.getCurrentValue()));
        return new PortfolioSummaryDto(holdings);
    }

    private HoldingDto computeHolding(List<Transaction> txns) {
        if (txns.isEmpty()) return null;

        int qtyHeld = 0;
        BigDecimal totalInvested = BigDecimal.ZERO;
        Stock stock = txns.get(0).getStock();

        // Sort by date ascending for FIFO
        List<Transaction> sorted = new ArrayList<>(txns);
        sorted.sort(Comparator.comparing(Transaction::getTransactionDate));

        // Use FIFO accounting for current holdings
        Deque<int[]> buyLots = new ArrayDeque<>(); // [quantity remaining, price as long-cents]
        Deque<BigDecimal> buyPrices = new ArrayDeque<>();

        for (Transaction t : sorted) {
            if (t.getType() == TransactionType.BUY) {
                buyLots.addLast(new int[]{t.getQuantity()});
                buyPrices.addLast(t.getPrice());
                qtyHeld += t.getQuantity();
            } else { // SELL
                int sellQty = t.getQuantity();
                qtyHeld -= sellQty;
                while (sellQty > 0 && !buyLots.isEmpty()) {
                    int[] lot = buyLots.peekFirst();
                    if (lot[0] <= sellQty) {
                        sellQty -= lot[0];
                        buyLots.pollFirst();
                        buyPrices.pollFirst();
                    } else {
                        lot[0] -= sellQty;
                        sellQty = 0;
                    }
                }
            }
        }

        if (qtyHeld <= 0) return null;

        // Compute weighted average buy price from remaining lots
        BigDecimal totalCost = BigDecimal.ZERO;
        int totalQty = 0;
        Iterator<int[]> lotIter = buyLots.iterator();
        Iterator<BigDecimal> priceIter = buyPrices.iterator();
        while (lotIter.hasNext()) {
            int[] lot = lotIter.next();
            BigDecimal price = priceIter.next();
            totalCost = totalCost.add(price.multiply(BigDecimal.valueOf(lot[0])));
            totalQty += lot[0];
        }
        BigDecimal avgPrice = totalQty > 0
                ? totalCost.divide(BigDecimal.valueOf(totalQty), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new HoldingDto(stock.getSymbol(), stock.getName(), stock.getSector(),
                qtyHeld, avgPrice, stock.getCurrentPrice());
    }

    public int computeQuantityHeld(User user, Long stockId) {
        List<Transaction> txns = transactionRepository.findByUserAndStockIdOrderByTransactionDateAsc(user, stockId);
        int qty = 0;
        for (Transaction t : txns) {
            if (t.getType() == TransactionType.BUY) qty += t.getQuantity();
            else qty -= t.getQuantity();
        }
        return qty;
    }

    public List<Transaction> getAllTransactions(User user) {
        return transactionRepository.findByUserOrderByTransactionDateDesc(user);
    }

    @Transactional
    public void deleteTransaction(User user, Long transactionId) {
        Transaction t = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
        if (!t.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Not authorized to delete this transaction");
        }
        transactionRepository.delete(t);
    }
}
