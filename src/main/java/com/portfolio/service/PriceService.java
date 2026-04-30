package com.portfolio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.model.Stock;
import com.portfolio.repository.StockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Random;

/**
 * Simulates live NSE price feed using geometric random walk.
 * Updates all stock prices every 30 seconds.
 * In production, replace with NSE API or yahoo-finance scraper.
 */

@Service
public class PriceService {

    private static final Logger log = LoggerFactory.getLogger(PriceService.class);
    private static final String YAHOO_URL =
            "https://query1.finance.yahoo.com/v8/finance/chart/%s.NS";

    private final StockRepository stockRepository;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    public PriceService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /** Runs every 60 seconds — fetches real NSE prices from Yahoo Finance. */
    @Scheduled(fixedDelay = 60_000, initialDelay = 5_000)
    public void updatePrices() {
        List<Stock> stocks = stockRepository.findAll();
        int success = 0, failed = 0;

        for (Stock stock : stocks) {
            try {
                BigDecimal livePrice = fetchLivePrice(stock.getSymbol());
                if (livePrice != null) {
                    stock.setCurrentPrice(livePrice);
                    stockRepository.save(stock);
                    success++;
                } else {
                    simulateFallback(stock);
                    failed++;
                }
            } catch (Exception e) {
                log.warn("Price fetch failed for {}: {}", stock.getSymbol(), e.getMessage());
                simulateFallback(stock);
                failed++;
            }
        }
        log.info("Price update done. Live: {}, Simulated fallback: {}", success, failed);
    }

    private BigDecimal fetchLivePrice(String symbol) throws Exception {
        String url = String.format(YAHOO_URL, symbol);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0")
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            return null;
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode priceNode = root.path("chart").path("result").get(0)
                .path("meta").path("regularMarketPrice");

        if (priceNode.isMissingNode() || priceNode.isNull()) {
            return null;
        }
        return BigDecimal.valueOf(priceNode.asDouble())
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** If API fails (offline / market closed), keep small random walk so demo works. */
    private void simulateFallback(Stock stock) {
        BigDecimal current = stock.getCurrentPrice();
        if (current == null) return;
        double drift = (random.nextDouble() - 0.5) * 0.01; // ±0.5%
        BigDecimal newPrice = current.multiply(BigDecimal.valueOf(1 + drift))
                .setScale(2, RoundingMode.HALF_UP);
        stock.setCurrentPrice(newPrice);
        stockRepository.save(stock);
    }
}