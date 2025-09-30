package com.thirdeye3.webscrapper.services.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
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

    @Override
    public void updateStocks() {
        String url = baseUrl + "/sm/stocks/webscrapper/" + uniqueId + "/" + uniqueCode;

        HttpHeaders headers = new HttpHeaders();
        headers.set("webscrapper-api-key", webscrapperApiKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Response<List<Stock>>> responseEntity =
                restTemplate.exchange(url, HttpMethod.GET, entity,
                        new ParameterizedTypeReference<Response<List<Stock>>>() {});

        Response<List<Stock>> response = responseEntity.getBody();

        if (response != null && response.isSuccess()) {
            logger.info("✅ Stocks updated at {}", timeManager.getCurrentTime());
            stocks = response.getResponse();
        } else {
            logger.error("❌ Error ({}): {} at {}",
                    response != null ? response.getErrorCode() : "N/A",
                    response != null ? response.getErrorMessage() : "No response body",
                    timeManager.getCurrentTime());
            throw new WebScrapperException("Stock update failed: " +
                    (response != null ? response.getErrorMessage() : "No response body"));
        }
    }

    @Override
    public List<Stock> getStocks() {
        if (stocks == null) {
            updateStocks();
        }
        return this.stocks;
    }
}
