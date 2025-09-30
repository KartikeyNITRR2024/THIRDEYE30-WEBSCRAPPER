package com.thirdeye3.webscrapper.services.impl;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thirdeye3.webscrapper.dtos.Stock;
import com.thirdeye3.webscrapper.exceptions.WebScrapperException;
import com.thirdeye3.webscrapper.services.WebscrapperService;
import com.thirdeye3.webscrapper.utils.TimeManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class WebscrapperServiceImpl implements WebscrapperService {

    private static final Logger logger = LoggerFactory.getLogger(WebscrapperServiceImpl.class);

    @Value("${webscrapper.baseUrl}")
    private String webscrapperBaseUrl;

    @Autowired
    private TimeManager timeManager;

    @Override
    public void fetchLiveStockInfo(Stock stock) {
        String url = webscrapperBaseUrl + stock.getUniqueCode() + ":" + stock.getMarketCode();

        try {
            Document doc = Jsoup.connect(url).get();
            String cssClass = "YMlKec fxKbKc";
            Element priceElement = doc.getElementsByClass(cssClass).first();

            if (priceElement == null) {
                logger.error("❌ Price element not found for {}", stock.getUniqueCode());
                throw new WebScrapperException("Price element not found for stock: " + stock.getUniqueCode());
            }

            String priceText = priceElement.text().trim();
            try {
                double price = Double.parseDouble(priceText.substring(1).replace(",", ""));
                stock.setPrice(price);
                stock.setCurrentTime(timeManager.getCurrentTime());
                //logger.info("✅ Price fetched for {}: {}", stock.getUniqueCode(), price);
            } catch (NumberFormatException e) {
                logger.error("❌ Could not parse price for {}: {}", stock.getUniqueCode(), priceText);
                throw new WebScrapperException("Invalid price format for stock: " + stock.getUniqueCode());
            }

        } catch (IOException e) {
            logger.error("❌ Error fetching price for {}: {}", stock.getUniqueCode(), e.getMessage());
            throw new WebScrapperException("Failed to fetch stock price: " + e.getMessage());
        }
    }
}
