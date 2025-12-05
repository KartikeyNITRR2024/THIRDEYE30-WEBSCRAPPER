package com.thirdeye3.webscrapper.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Value("${webscrapper.executor.settings:10:20:100:StockFetcher-}")
    private String executorSettings;

    @Bean(name = "stockExecutor")
    public Executor stockExecutor() {

        String[] parts = executorSettings.split(":");

        int corePool = Integer.parseInt(parts[0]);
        int maxPool = Integer.parseInt(parts[1]);
        int queueCap = Integer.parseInt(parts[2]);
        String prefix = parts.length > 3 ? parts[3] : "StockFetcher-";

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePool);
        executor.setMaxPoolSize(maxPool);
        executor.setQueueCapacity(queueCap);
        executor.setThreadNamePrefix(prefix);
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }
}
