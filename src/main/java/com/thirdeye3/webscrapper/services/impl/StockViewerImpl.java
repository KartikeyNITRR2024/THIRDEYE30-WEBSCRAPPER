package com.thirdeye3.webscrapper.services.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.thirdeye3.webscrapper.dtos.Response;
import com.thirdeye3.webscrapper.dtos.Stock;
import com.thirdeye3.webscrapper.dtos.WebscrapperRequest;
import com.thirdeye3.webscrapper.exceptions.InvalidMachineException;
import com.thirdeye3.webscrapper.exceptions.WebScrapperException;
import com.thirdeye3.webscrapper.services.StockViewer;
import com.thirdeye3.webscrapper.utils.Converter;
import com.thirdeye3.webscrapper.utils.Initiatier;
import com.thirdeye3.webscrapper.utils.TimeManager;

@Service
public class StockViewerImpl implements StockViewer {
    
    private static final Logger logger = LoggerFactory.getLogger(StockViewerImpl.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private TimeManager timeManager;
    
    @Autowired
    private Initiatier initiatier;

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
    public void sendStocks(List<Stock> stocks) {
        WebscrapperRequest webscrapperRequest = new WebscrapperRequest();
        logger.info("🚀 Trying to send {} stocks.");
        
        if (uniqueCode.charAt(8) == '1') {
            webscrapperRequest.setStockList(stocks);
        } else if (uniqueCode.charAt(8) == '2') {
            webscrapperRequest.setStockMap(Converter.stockListToStockMap(stocks));
        } else {
            throw new InvalidMachineException("Unknown machine type");
        }

        String url = baseUrl + "/sv/webscrapper/" + uniqueId + "/" + uniqueCode;

        HttpHeaders headers = new HttpHeaders();
        headers.set("webscrapper-api-key", webscrapperApiKey);

        HttpEntity<WebscrapperRequest> entity = new HttpEntity<>(webscrapperRequest, headers);

        int attempt = 0;
        long backoff = maxRetries;

        while (attempt < maxRetries) {
            attempt++;
            try {
                logger.info("🚀 Attempt {}/{} to send stocks to {}", attempt, maxRetries, url);

                ResponseEntity<Response<Boolean>> responseEntity =
                        restTemplate.exchange(url, HttpMethod.POST, entity,
                                new ParameterizedTypeReference<Response<Boolean>>() {});

                Response<Boolean> response = responseEntity.getBody();

                if (response != null && response.isSuccess()) {
                    Boolean restart = response.getResponse();
                    logger.info("✅ Stocks sent at {} — restart flag = {}", timeManager.getCurrentTime(), restart);
                    if (Boolean.TRUE.equals(restart)) {
                        try {
                            initiatier.init();
                            logger.info("♻️ Initiatier restarted successfully.");
                        } catch (Exception e) {
                            logger.error("❌ Failed to restart initiatier", e);
                        }
                    }
                    return;
                } else {
                    logger.error("❌ Attempt {} failed — Error ({}): {}", 
                            attempt,
                            response != null ? response.getErrorCode() : "N/A",
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

        throw new WebScrapperException("Failed to send stocks after " + maxRetries + " attempts.");
    }
}
