package com.thirdeye3.webscrapper.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.thirdeye3.webscrapper.dtos.Response;
import com.thirdeye3.webscrapper.dtos.Stock;
import com.thirdeye3.webscrapper.exceptions.WebScrapperException;
import com.thirdeye3.webscrapper.services.SchedulerService;
import com.thirdeye3.webscrapper.services.StockService;
import com.thirdeye3.webscrapper.utils.ApiClient;
import com.thirdeye3.webscrapper.utils.AsyncStockService;
import com.thirdeye3.webscrapper.utils.Initiatier;
import com.thirdeye3.webscrapper.utils.TimeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class SchedulerServiceImpl implements SchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerServiceImpl.class);

    @Autowired
    private AsyncStockService asyncStockService;

    @Autowired
    private StockService stockService;

    @Autowired
    private Initiatier initiatier;
    
    @Autowired
    private StockViewerImpl stockViewerImpl;
    
    @Autowired
    private TimeManager timeManager;
    
    @Autowired
    private ApiClient apiClient;

    private List<Stock> tempStockList = null;
    
    @Value("${webscrapper.priority}")
    private Integer priority;
    
    @Value("${webscrapper.uniqueId}")
    private Integer uniqueId;

    @Value("${webscrapper.uniqueCode}")
    private String uniqueCode;
    
    @Value("${webscrapper.baseUrlForCheckStatus}")
    private String baseUrl;

    @Override
    @Scheduled(cron = "${webscrapper.scheduler.cronToWebScrap}", zone = "${webscrapper.timezone}")
    public void runToWebscrap() {
    	if(timeManager.isMarketOpen())
    	{
	        try {
	            tempStockList = stockService.getStocks();
	
	            long start = System.currentTimeMillis();
	            logger.info("✅ Scheduler started at {}", timeManager.getCurrentTime());
	
	            List<CompletableFuture<Void>> futures = new ArrayList<>();
	            for (Stock stock : tempStockList) {
	                futures.add(asyncStockService.fetchStockAsync(stock));
	            }
	            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
	
	            long end = System.currentTimeMillis();
	            logger.info("✅ Scheduler finished at {}. Total time = {} ms",
	                    timeManager.getCurrentTime(), (end - start));
	        } catch (Exception e) {
	            logger.error("❌ Scheduler failed at {}: {}", timeManager.getCurrentTime(), e.getMessage());
	            throw new WebScrapperException("Scheduler runToWebscrap failed: " + e.getMessage());
	        }
    	}
    	else
    	{
    		logger.info("Market is currently close");
    	}
    }

    @Override
    @Scheduled(cron = "${webscrapper.scheduler.cronToSendData}", zone = "${webscrapper.timezone}")
    public void runToSendData() {
        try {
        	if(tempStockList != null)
        	{
        		logger.info("📤 Sending data at {}", timeManager.getCurrentTime());
        		stockViewerImpl.sendStocks(tempStockList);
        	}
        } catch (Exception e) {
            logger.error("❌ Failed to send data at {}: {}", timeManager.getCurrentTime(), e.getMessage());
            throw new WebScrapperException("Scheduler runToSendData failed: " + e.getMessage());
        }
    }

    @Override
    @Scheduled(cron = "${webscrapper.scheduler.cronToRefreshData}", zone = "${webscrapper.timezone}")
    public void runToRefreshdata() {
        try {
        	TimeUnit.SECONDS.sleep(priority * 3); 
            initiatier.init();
            logger.info("🔄 Data refreshed at {}", timeManager.getCurrentTime());
        } catch (Exception e) {
            logger.error("❌ Failed to refresh data at {}: {}", timeManager.getCurrentTime(), e.getMessage());
            throw new WebScrapperException("Scheduler runToRefreshdata failed: " + e.getMessage());
        }
    }
    
    @Override
    @Scheduled(fixedRateString = "${webscrapper.scheduler.runToCheckStatus}")
    public void statusChecker() {
        Response<String> response = apiClient.getForObject(
                baseUrl + "/api/statuschecker/" + uniqueId + "/" + uniqueCode,
                new ParameterizedTypeReference<Response<String>>() {}
                );
        logger.info("🔄 Checking status. result is {}", response.getResponse());

    }
}
