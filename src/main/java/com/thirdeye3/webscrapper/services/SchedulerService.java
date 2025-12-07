package com.thirdeye3.webscrapper.services;

public interface SchedulerService {
    void runToWebscrap(int cycle);
    void runToSendData(int cycle);
	void runToRefreshdata();
	void statusChecker();
}
