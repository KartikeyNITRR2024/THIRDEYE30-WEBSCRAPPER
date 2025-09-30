package com.thirdeye3.webscrapper.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class WebscrapperRequest {
    private List<Stock> stockList;
    private Map<Long, Stock> stockMap;
}

