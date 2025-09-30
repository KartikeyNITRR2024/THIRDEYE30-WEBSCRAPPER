package com.thirdeye3.webscrapper.utils;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.thirdeye3.webscrapper.services.PropertyService;

@Component
public class TimeManager {
    
    @Value("${webscrapper.timezone}")
    private String timeZone;
    
    @Value("${webscrapper.istesting}")
    private Integer isTesting;
    
    @Autowired
    private PropertyService propertyService;

    public Timestamp getCurrentTime() {
        ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.of(timeZone));
        LocalDateTime localDateTime = currentTime.toLocalDateTime();
        return Timestamp.valueOf(localDateTime);
    }
    
    public long getMillisUntilNextMinute() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of(timeZone));
        LocalDateTime nextMinute = now.plusMinutes(1).withSecond(0).withNano(0);
        return ChronoUnit.MILLIS.between(now, nextMinute);
    }

    public boolean isMarketOpen() {
    	if(isTesting == 1)
    	{
    		return true;
    	}
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(timeZone));
        LocalTime currentTime = now.toLocalTime();
        DayOfWeek day = now.getDayOfWeek();
        boolean isWeekday = day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
        boolean isWithinMarketHours = !currentTime.isBefore(propertyService.getMarketStart())
                && !currentTime.isAfter(propertyService.getMarketEnd());
        return isWeekday && isWithinMarketHours;
    }

}
