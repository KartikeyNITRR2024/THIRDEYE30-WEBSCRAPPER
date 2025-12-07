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
import java.util.concurrent.atomic.AtomicBoolean;

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

    @Value("${webscrapper.thread.MaximumLifeCycle}")
    private Integer threadMaximumLifeCycle;

    private final AtomicBoolean running = new AtomicBoolean(false);
    
    @Override
    public void runToWebscrap(int cycle) {

        if (!running.compareAndSet(false, true)) {
            logger.warn("Previous scheduler still running — skipping this cycle.");
            return;
        }

        try {
            if (timeManager.isMarketOpen()) {
                long start = System.currentTimeMillis();
                logger.info("Scheduler started at {}", timeManager.getCurrentTime());
                try {
                    tempStockList = stockService.getStockByBatchNo(cycle);

                    List<CompletableFuture<Void>> futures = new ArrayList<>();
                    for (Stock stock : tempStockList) {
                        futures.add(asyncStockService.fetchStockAsync(stock));
                    }

                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                            .get(threadMaximumLifeCycle, TimeUnit.SECONDS);

                } catch (Exception e) {
                    logger.error("Scheduler error at {}: {}", timeManager.getCurrentTime(), e.toString());
                }
                long end = System.currentTimeMillis();
                logger.info("Scheduler finished at {}. Total time = {} ms",
                        timeManager.getCurrentTime(), (end - start));
                runToSendData();
            } else {
                logger.info("Market is currently close");
            }
        }
        finally {
            running.set(false);
        }
    }

    @Override
    public void runToSendData() {

        try {
            if (tempStockList != null && !tempStockList.isEmpty()) {

                logger.info("Sending data at {}", timeManager.getCurrentTime());

                List<Stock> filteredList = tempStockList.stream()
                        .filter(s -> s.getPrice() != null)
                        .toList();

                logger.info("{} valid stocks will be sent ({} removed due to null price)",
                        filteredList.size(),
                        tempStockList.size() - filteredList.size());

                stockViewerImpl.sendStocks(filteredList);
            }
        } catch (Exception e) {
            logger.error("Failed to send data at {}: {}", timeManager.getCurrentTime(), e.getMessage());
        }
    }


    @Override
    @Scheduled(cron = "${webscrapper.scheduler.cronToRefreshData}", zone = "${webscrapper.timezone}")
    public void runToRefreshdata() {
        try {
            TimeUnit.SECONDS.sleep(priority * 3);
            initiatier.init();
            logger.info("Data refreshed at {}", timeManager.getCurrentTime());
        } catch (Exception e) {
            logger.error("Failed to refresh data at {}: {}", timeManager.getCurrentTime(), e.getMessage());
        }
    }

    @Override
    @Scheduled(fixedRateString = "${webscrapper.scheduler.runToCheckStatus}")
    public void statusChecker() {
        Response<String> response = apiClient.getForObject(
                baseUrl + "/api/statuschecker/" + uniqueId + "/" + uniqueCode,
                new ParameterizedTypeReference<Response<String>>() {}
        );
        logger.info("Checking status. result is {}", response.getResponse());
    }
}
