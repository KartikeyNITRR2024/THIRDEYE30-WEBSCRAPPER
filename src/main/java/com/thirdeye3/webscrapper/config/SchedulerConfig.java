package com.thirdeye3.webscrapper.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import com.thirdeye3.webscrapper.services.SchedulerService;
import com.thirdeye3.webscrapper.services.impl.SchedulerServiceImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Configuration
@EnableScheduling
public class SchedulerConfig implements SchedulingConfigurer {
	
	private static final Logger logger = LoggerFactory.getLogger(SchedulerConfig.class);

    @Value("${webscrapper.numberofcycle.perminute}")
    private Integer noOfCyclePerMinute;
    
    @Value("${webscrapper.jobstartingsecond.inminute}")
    private Integer startingSecond;
    
    
    
    @Autowired
    private SchedulerService schedulerService;

    private List<Integer> secondsList = new ArrayList<>();
    private boolean initialized = false;
    private int cycleIndex = 0;

    private void initSeconds() {
        int gap = 60 / noOfCyclePerMinute;
        secondsList.clear();
        for (int s = 0; s < 60; s += gap) {
            secondsList.add(s+startingSecond);
        }
        initialized = true;
        logger.info("Running seconds of scheduler is " + secondsList);
    }

    private String getSecondsString() {
        return String.join(",", secondsList.stream().map(String::valueOf).toList());
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(
                this::runCycleTask,

                triggerContext -> {
                    if (!initialized) initSeconds();
                    String cron = getSecondsString() + " * * * * *";
                    return new CronTrigger(cron).nextExecution(triggerContext);
                }
        );
    }

    private void runCycleTask() {
        cycleIndex++;
        if (cycleIndex > secondsList.size()) {
            cycleIndex = 1;
        }
        logger.info("Running for scheduler cycle " + cycleIndex);
        schedulerService.runToWebscrap(cycleIndex - 1);
    }
}
