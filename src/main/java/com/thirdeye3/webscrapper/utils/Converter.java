package com.thirdeye3.webscrapper.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thirdeye3.webscrapper.dtos.Stock;

public class Converter {
	
	public static Map<Long, Stock> stockListToStockMap(List<Stock> stocks)
	{
		Map<Long, Stock> stockMap = new HashMap<>();
		for(Stock stock : stocks)
		{
			stockMap.put(stock.getUniqueId(), stock);
		}
		return stockMap;
	}
}
