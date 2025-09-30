package com.thirdeye3.webscrapper.services.impl;

import java.time.LocalTime;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.thirdeye3.webscrapper.dtos.Response;
import com.thirdeye3.webscrapper.services.PropertyService;

@Service
public class PropertyServiceImpl implements PropertyService {

    private static final Logger logger = LoggerFactory.getLogger(PropertyServiceImpl.class);

    private Map<String, Object> properties = null;

    private LocalTime marketStart = null;
    private LocalTime marketEnd = null;
    private Integer machineNo = null;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${thirdeye.baseUrlForGateway}")
    private String baseUrl;

    @Value("${webscrapper.uniqueId}")
    private Integer uniqueId;

    @Value("${webscrapper.uniqueCode}")
    private String uniqueCode;

    @Value("${webscrapper.api.key}")
    private String webscrapperApiKey;

    @Override
    public void updateProperties() {
        String url = baseUrl + "/pm/properties/webscrapper/" + uniqueId + "/" + uniqueCode;

        HttpHeaders headers = new HttpHeaders();
        headers.set("webscrapper-api-key", webscrapperApiKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Response<Map<String, Object>>> responseEntity =
                restTemplate.exchange(url, HttpMethod.GET, entity,
                        new ParameterizedTypeReference<Response<Map<String, Object>>>() {});

        Response<Map<String, Object>> response = responseEntity.getBody();

        if (response != null && response.isSuccess()) {
            logger.info("✅ Properties updated.");
            properties = response.getResponse();

            marketStart = LocalTime.of(
                    (int) properties.get("START_TIME_HOUR"),
                    (int) properties.get("START_TIME_MINUTE"),
                    (int) properties.get("START_TIME_SECOND")
            );

            marketEnd = LocalTime.of(
                    (int) properties.get("END_TIME_HOUR"),
                    (int) properties.get("END_TIME_MINUTE"),
                    (int) properties.get("END_TIME_SECOND")
            );

            machineNo = (int) properties.get("MACHINE_NO");

            logger.info("Properties are {}, {}, {}", marketStart, marketEnd, machineNo);
        } else {
            logger.error("❌ Error while updating properties: {}", 
                    response != null ? response.getErrorMessage() : "null response");
            throw new RuntimeException("Property update failed: " +
                    (response != null ? response.getErrorMessage() : "No response body"));
        }
    }

    @Override
    public Map<String, Object> getProperties() {
        if (properties == null) {
            updateProperties();
        }
        return this.properties;
    }

    @Override
    public LocalTime getMarketStart() {
        return marketStart;
    }

    @Override
    public LocalTime getMarketEnd() {
        return marketEnd;
    }
}
