package com.thirdeye3.webscrapper.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.thirdeye3.webscrapper.services.PropertyService;
import com.thirdeye3.webscrapper.services.StockService;

import jakarta.annotation.PostConstruct;

@Component
public class Initiatier {
	
    private static final Logger logger = LoggerFactory.getLogger(Initiatier.class);
    
    @Autowired
    private StockService stockService;
    
    @Autowired
    private PropertyService propertyService;
	
	@PostConstruct
    public void init() throws Exception{
        logger.info("Initializing Initiatier...");
        propertyService.getProperties();
        stockService.updateStocks();
        logger.info("Initiatier initialized.");
    }

}
