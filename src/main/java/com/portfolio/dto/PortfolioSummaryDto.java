package com.portfolio.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class PortfolioSummaryDto {
    private BigDecimal totalInvested;
    private BigDecimal currentValue;
    private BigDecimal totalPnl;
    private BigDecimal totalPnlPercent;
    private int holdingsCount;
    private List<HoldingDto> holdings;

    public PortfolioSummaryDto(List<HoldingDto> holdings) {
        this.holdings = holdings;
        this.holdingsCount = holdings.size();
        this.totalInvested = BigDecimal.ZERO;
        this.currentValue = BigDecimal.ZERO;
        for (HoldingDto h : holdings) {
            this.totalInvested = this.totalInvested.add(h.getInvestedValue());
            this.currentValue = this.currentValue.add(h.getCurrentValue());
        }
        this.totalPnl = currentValue.subtract(totalInvested).setScale(2, RoundingMode.HALF_UP);
        if (totalInvested.compareTo(BigDecimal.ZERO) > 0) {
            this.totalPnlPercent = totalPnl.multiply(BigDecimal.valueOf(100))
                    .divide(totalInvested, 2, RoundingMode.HALF_UP);
        } else {
            this.totalPnlPercent = BigDecimal.ZERO;
        }
    }

    public BigDecimal getTotalInvested() { return totalInvested; }
    public BigDecimal getCurrentValue() { return currentValue; }
    public BigDecimal getTotalPnl() { return totalPnl; }
    public BigDecimal getTotalPnlPercent() { return totalPnlPercent; }
    public int getHoldingsCount() { return holdingsCount; }
    public List<HoldingDto> getHoldings() { return holdings; }
}
