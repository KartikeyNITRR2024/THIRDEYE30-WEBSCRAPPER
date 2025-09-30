package com.thirdeye3.webscrapper.utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.thirdeye3.webscrapper.dtos.Stock;
import com.thirdeye3.webscrapper.services.WebscrapperService;

@Service
public class AsyncStockService {
    @Autowired
    private WebscrapperService webscrapper;

    @Async("stockExecutor")
    public CompletableFuture<Void> fetchStockAsync(Stock stock) {
    	
    	 try {
             long sleepTime = ThreadLocalRandom.current().nextLong(0, 3001);
             Thread.sleep(sleepTime);
         } catch (InterruptedException e) {
             Thread.currentThread().interrupt();
         }
    	
        webscrapper.fetchLiveStockInfo(stock);
        return CompletableFuture.completedFuture(null);
    }
}
