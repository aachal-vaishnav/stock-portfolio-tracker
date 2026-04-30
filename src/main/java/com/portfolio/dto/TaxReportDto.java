package com.portfolio.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TaxReportDto {
    private int financialYear;
    private BigDecimal stcgGains;
    private BigDecimal stcgTax;
    private BigDecimal ltcgGains;
    private BigDecimal ltcgExemption;
    private BigDecimal ltcgTaxable;
    private BigDecimal ltcgTax;
    private BigDecimal totalTax;
    private List<TaxLot> lots = new ArrayList<>();

    public static class TaxLot {
        private final String symbol;
        private final int quantity;
        private final BigDecimal buyPrice;
        private final BigDecimal sellPrice;
        private final java.time.LocalDate buyDate;
        private final java.time.LocalDate sellDate;
        private final long holdingDays;
        private final BigDecimal gainLoss;
        private final String type;

        public TaxLot(String symbol, int quantity, BigDecimal buyPrice, BigDecimal sellPrice,
                      java.time.LocalDate buyDate, java.time.LocalDate sellDate,
                      long holdingDays, BigDecimal gainLoss, String type) {
            this.symbol = symbol;
            this.quantity = quantity;
            this.buyPrice = buyPrice;
            this.sellPrice = sellPrice;
            this.buyDate = buyDate;
            this.sellDate = sellDate;
            this.holdingDays = holdingDays;
            this.gainLoss = gainLoss;
            this.type = type;
        }

        public String getSymbol() { return symbol; }
        public int getQuantity() { return quantity; }
        public BigDecimal getBuyPrice() { return buyPrice; }
        public BigDecimal getSellPrice() { return sellPrice; }
        public java.time.LocalDate getBuyDate() { return buyDate; }
        public java.time.LocalDate getSellDate() { return sellDate; }
        public long getHoldingDays() { return holdingDays; }
        public BigDecimal getGainLoss() { return gainLoss; }
        public String getType() { return type; }
    }

    public int getFinancialYear() { return financialYear; }
    public void setFinancialYear(int financialYear) { this.financialYear = financialYear; }
    public BigDecimal getStcgGains() { return stcgGains; }
    public void setStcgGains(BigDecimal stcgGains) { this.stcgGains = stcgGains; }
    public BigDecimal getStcgTax() { return stcgTax; }
    public void setStcgTax(BigDecimal stcgTax) { this.stcgTax = stcgTax; }
    public BigDecimal getLtcgGains() { return ltcgGains; }
    public void setLtcgGains(BigDecimal ltcgGains) { this.ltcgGains = ltcgGains; }
    public BigDecimal getLtcgExemption() { return ltcgExemption; }
    public void setLtcgExemption(BigDecimal ltcgExemption) { this.ltcgExemption = ltcgExemption; }
    public BigDecimal getLtcgTaxable() { return ltcgTaxable; }
    public void setLtcgTaxable(BigDecimal ltcgTaxable) { this.ltcgTaxable = ltcgTaxable; }
    public BigDecimal getLtcgTax() { return ltcgTax; }
    public void setLtcgTax(BigDecimal ltcgTax) { this.ltcgTax = ltcgTax; }
    public BigDecimal getTotalTax() { return totalTax; }
    public void setTotalTax(BigDecimal totalTax) { this.totalTax = totalTax; }
    public List<TaxLot> getLots() { return lots; }
    public void setLots(List<TaxLot> lots) { this.lots = lots; }
}
