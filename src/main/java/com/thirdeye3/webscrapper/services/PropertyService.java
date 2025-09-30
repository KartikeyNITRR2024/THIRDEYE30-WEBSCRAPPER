package com.thirdeye3.webscrapper.services;
import java.time.LocalTime;
import java.util.Map;

public interface PropertyService {

	void updateProperties();

	Map<String, Object> getProperties();

	LocalTime getMarketStart();

	LocalTime getMarketEnd();

}
