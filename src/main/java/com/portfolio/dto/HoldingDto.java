package com.portfolio.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class HoldingDto {
    private String symbol;
    private String name;
    private String sector;
    private int quantity;
    private BigDecimal avgBuyPrice;
    private BigDecimal currentPrice;
    private BigDecimal investedValue;
    private BigDecimal currentValue;
    private BigDecimal unrealizedPnl;
    private BigDecimal pnlPercent;

    public HoldingDto(String symbol, String name, String sector, int quantity,
                      BigDecimal avgBuyPrice, BigDecimal currentPrice) {
        this.symbol = symbol;
        this.name = name;
        this.sector = sector;
        this.quantity = quantity;
        this.avgBuyPrice = avgBuyPrice;
        this.currentPrice = currentPrice;
        this.investedValue = avgBuyPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
        this.currentValue = currentPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
        this.unrealizedPnl = currentValue.subtract(investedValue).setScale(2, RoundingMode.HALF_UP);
        if (investedValue.compareTo(BigDecimal.ZERO) > 0) {
            this.pnlPercent = unrealizedPnl.multiply(BigDecimal.valueOf(100))
                    .divide(investedValue, 2, RoundingMode.HALF_UP);
        } else {
            this.pnlPercent = BigDecimal.ZERO;
        }
    }

    public String getSymbol() { return symbol; }
    public String getName() { return name; }
    public String getSector() { return sector; }
    public int getQuantity() { return quantity; }
    public BigDecimal getAvgBuyPrice() { return avgBuyPrice; }
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public BigDecimal getInvestedValue() { return investedValue; }
    public BigDecimal getCurrentValue() { return currentValue; }
    public BigDecimal getUnrealizedPnl() { return unrealizedPnl; }
    public BigDecimal getPnlPercent() { return pnlPercent; }
}
