package com.thirdeye3.webscrapper.services.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.thirdeye3.webscrapper.dtos.Response;
import com.thirdeye3.webscrapper.dtos.Stock;
import com.thirdeye3.webscrapper.exceptions.WebScrapperException;
import com.thirdeye3.webscrapper.services.StockService;
import com.thirdeye3.webscrapper.utils.TimeManager;

@Service
public class StockServiceImpl implements StockService {

    private static final Logger logger = LoggerFactory.getLogger(StockServiceImpl.class);

    private List<Stock> stocks = null;

    private final RestTemplate restTemplate = new RestTemplate();
    private final TimeManager timeManager;

    public StockServiceImpl(TimeManager timeManager) {
        this.timeManager = timeManager;
    }

    @Value("${thirdeye.baseUrlForGateway}")
    private String baseUrl;

    @Value("${webscrapper.uniqueId}")
    private Integer uniqueId;

    @Value("${webscrapper.uniqueCode}")
    private String uniqueCode;

    @Value("${webscrapper.api.key}")
    private String webscrapperApiKey;

    @Value("${webscrapper.retry.max-attempts}")
    private int maxRetries;

    @Value("${webscrapper.retry.initial-backoff}")
    private long initialBackoffMs;
    
    @Override
    public void updateStocks() {
        String url = baseUrl + "/sm/stocks/webscrapper/" + uniqueId + "/" + uniqueCode;
        HttpHeaders headers = new HttpHeaders();
        headers.set("webscrapper-api-key", webscrapperApiKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        int attempt = 0;
        long backoff = initialBackoffMs;

        while (attempt < maxRetries) {
            attempt++;
            try {
                logger.info("🔄 Attempt {}/{} to update stocks from {}", attempt, maxRetries, url);

                ResponseEntity<Response<List<Stock>>> responseEntity =
                        restTemplate.exchange(url, HttpMethod.GET, entity,
                                new ParameterizedTypeReference<Response<List<Stock>>>() {});

                Response<List<Stock>> response = responseEntity.getBody();

                if (response != null && response.isSuccess()) {
                    logger.info("✅ {} stocks updated successfully at {}",response.getResponse().size(), timeManager.getCurrentTime());
                    stocks = response.getResponse();
                    return;
                } else {
                    logger.error("❌ Attempt {} failed with error: {}",
                            attempt,
                            response != null ? response.getErrorMessage() : "No response body");
                }

            } catch (RestClientException ex) {
                logger.error("⚠️ Attempt {} failed due to exception: {}", attempt, ex.getMessage());
            }

            if (attempt < maxRetries) {
                try {
                    logger.info("⏸ Waiting {} ms before retrying...", backoff);
                    Thread.sleep(backoff);
                    backoff *= 2;
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new WebScrapperException("Retry interrupted");
                }
            }
        }

        throw new WebScrapperException("Stock update failed after " + maxRetries + " attempts.");
    }

    @Override
    public List<Stock> getStocks() {
        if (stocks == null) {
            updateStocks();
        }
        return this.stocks;
    }
}
