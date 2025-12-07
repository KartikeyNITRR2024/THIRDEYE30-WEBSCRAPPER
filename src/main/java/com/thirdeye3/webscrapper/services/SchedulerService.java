package com.thirdeye3.webscrapper.services;

public interface SchedulerService {
    void runToWebscrap(int cycle);
    void runToSendData();
	void runToRefreshdata();
	void statusChecker();
}
