package com.thirdeye3.webscrapper.services;

import java.util.List;

import com.thirdeye3.webscrapper.dtos.Stock;

public interface StockViewer {
	void sendStocks(List<Stock> stocks);

}
