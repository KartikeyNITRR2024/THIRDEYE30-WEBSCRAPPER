package com.thirdeye3.webscrapper.services;

import com.thirdeye3.webscrapper.dtos.Stock;

public interface WebscrapperService {
	void fetchLiveStockInfo(Stock stock);
}
