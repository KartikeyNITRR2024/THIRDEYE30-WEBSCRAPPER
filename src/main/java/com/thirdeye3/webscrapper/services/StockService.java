package com.thirdeye3.webscrapper.services;

import java.util.List;

import com.thirdeye3.webscrapper.dtos.Stock;

public interface StockService {
	void updateStocks();
	List<Stock> getStocks();
}
