package com.thirdeye3.webscrapper.services.impl;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thirdeye3.webscrapper.dtos.Stock;
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
    
    @Value("${webscrapper.jsoup.MaximumConnectTime}")
    private Integer jsoupMaximumConnectTime;

    @Autowired
    private TimeManager timeManager;

    @Override
    public void fetchLiveStockInfo(Stock stock) {
        String url = webscrapperBaseUrl + stock.getUniqueCode() + ":" + stock.getMarketCode();

        try {
            Document doc = Jsoup.connect(url)
                    .timeout(jsoupMaximumConnectTime)
                    .get();

            String cssClass = "YMlKec fxKbKc";
            Element priceElement = doc.getElementsByClass(cssClass).first();

            if (priceElement == null) {
                logger.warn("Price element not found for {} — skipping", stock.getUniqueCode());
                stock.setPrice(null);
                stock.setCurrentTime(null);
                return;
            }

            String priceText = priceElement.text().trim();

            try {
                double price = Double.parseDouble(priceText.substring(1).replace(",", ""));
                stock.setPrice(price);
                stock.setCurrentTime(timeManager.getCurrentTime());
            } catch (NumberFormatException e) {
                logger.warn("Invalid price for {} → '{}'", stock.getUniqueCode(), priceText);
                stock.setPrice(null);
                stock.setCurrentTime(null);
            }

        } catch (IOException e) {
            logger.warn("Error fetching {}: {}", stock.getUniqueCode(), e.getMessage());
            stock.setPrice(null);
            stock.setCurrentTime(null);
        }
    }
}
