package com.thirdeye3.webscrapper.dtos;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Stock {
	private Long uniqueId;
	private String uniqueCode;
	private String marketCode;
	private Double price;
	private Timestamp currentTime;
}
