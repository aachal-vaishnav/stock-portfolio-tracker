package com.portfolio.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "stocks")
public class Stock {

    @Id
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String symbol;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 10)
    private String exchange;

    @Column
    private String sector;

    @Column(name = "current_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal currentPrice;

    public Stock() {}

    public Stock(Long id, String symbol, String name, String exchange, String sector, BigDecimal currentPrice) {
        this.id = id;
        this.symbol = symbol;
        this.name = name;
        this.exchange = exchange;
        this.sector = sector;
        this.currentPrice = currentPrice;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }
    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
}
