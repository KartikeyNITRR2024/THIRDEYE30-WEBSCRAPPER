package com.thirdeye3.webscrapper.services.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
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
    
    @Override
    public void sendStocks(List<Stock> stocks) {
        WebscrapperRequest webscrapperRequest = new WebscrapperRequest();
    	
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

        ResponseEntity<Response<Boolean>> responseEntity =
                restTemplate.exchange(url, HttpMethod.POST, entity,
                        new ParameterizedTypeReference<Response<Boolean>>() {});

        Response<Boolean> response = responseEntity.getBody();

        if (response != null && response.isSuccess()) {
            Boolean check = response.getResponse();
            logger.info("✅ Stocks sent at {} and response to restart is {}", timeManager.getCurrentTime(), check);
            if (Boolean.TRUE.equals(check)) {
                try {
                    initiatier.init();
                    logger.info("✅ Initiatier restarted.");
                } catch (Exception e) {
                    logger.error("❌ Failed to restart initiatier", e);
                }
            }
        } else {
            logger.error("❌ Error ({}): {} at {}", 
                    response != null ? response.getErrorCode() : "N/A", 
                    response != null ? response.getErrorMessage() : "No response body", 
                    timeManager.getCurrentTime());
            throw new WebScrapperException("Sending stock failed: " +
                    (response != null ? response.getErrorMessage() : "No response body"));
        }
    }
}
