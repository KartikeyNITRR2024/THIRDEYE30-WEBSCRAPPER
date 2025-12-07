package com.thirdeye3.webscrapper.services.impl;

import java.util.ArrayList;
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

    private List<List<Stock>> stockBatches = null;

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

    @Value("${webscrapper.numberofcycle.perminute}")
    private Integer noOfCyclePerMinute;

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
                logger.info("🔄 Attempt {}/{} to update stocks", attempt, maxRetries);

                ResponseEntity<Response<List<Stock>>> responseEntity =
                        restTemplate.exchange(url, HttpMethod.GET, entity,
                                new ParameterizedTypeReference<Response<List<Stock>>>() {});

                Response<List<Stock>> response = responseEntity.getBody();

                if (response != null && response.isSuccess()) {

                    List<Stock> fullStockList = response.getResponse();
                    logger.info("✅ {} stocks fetched successfully at {}", fullStockList.size(), timeManager.getCurrentTime());

                    stockBatches = divideIntoBatches(fullStockList, noOfCyclePerMinute);

                    logger.info("📦 Stocks divided into {} batches", stockBatches.size());

                    return;
                } else {
                    logger.error("❌ Attempt {} failed with error: {}", attempt,
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

    private List<List<Stock>> divideIntoBatches(List<Stock> stocks, int totalBatches) {

        List<List<Stock>> batches = new ArrayList<>();

        int totalStocks = stocks.size();
        int baseSize = totalStocks / totalBatches;     
        int remainder = totalStocks % totalBatches;

        int index = 0;

        for (int i = 0; i < totalBatches; i++) {

            int currentBatchSize = baseSize + (i < remainder ? 1 : 0);

            int endIndex = Math.min(index + currentBatchSize, totalStocks);

            List<Stock> batch = new ArrayList<>(stocks.subList(index, endIndex));
            batches.add(batch);

            index = endIndex;
        }

        return batches;
    }


    @Override
    public List<Stock> getStocks() {
        if (stockBatches == null) {
            updateStocks();
        }
        List<Stock> merged = new ArrayList<>();
        for (List<Stock> batch : stockBatches) {
            merged.addAll(batch);
        }
        return merged;
    }

    @Override
    public List<List<Stock>> getStockBatches() {
        if (stockBatches == null) {
            updateStocks();
        }
        return stockBatches;
    }
    
    @Override
    public List<Stock> getStockByBatchNo(int batch)
    {
    	if (stockBatches == null) {
            updateStocks();
        }
    	if(batch >= stockBatches.size())
    	{
    		throw new WebScrapperException("Invalid batch Number "+batch);
    	}
    	logger.info("Returning stock batch {} of size {}", batch, stockBatches.get(batch).size());
    	return stockBatches.get(batch);
    }
}
