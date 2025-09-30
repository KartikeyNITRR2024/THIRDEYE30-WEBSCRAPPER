package com.thirdeye3.webscrapper.services;

public interface SchedulerService {
    void runToWebscrap();
    void runToSendData();
	void runToRefreshdata();
	void statusChecker();
}
