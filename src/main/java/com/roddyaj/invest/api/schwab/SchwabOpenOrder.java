package com.roddyaj.invest.api.schwab;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVRecord;

import com.roddyaj.invest.util.StringUtils;

public record SchwabOpenOrder(
	String symbol,
	String nameOfSecurity,
	String action,
	int quantity,
	String orderType,
	Double limitPrice,
	LocalDate timing,
	LocalDateTime timeAndDate_ET,
	String status,
	int orderNumber)
{
	private static final DateTimeFormatter TIME_AND_DATE_FORMAT = DateTimeFormatter.ofPattern("h:mm a MM/dd/yyyy");

	public SchwabOpenOrder(CSVRecord record)
	{
		// @formatter:off
		this(
			record.get("Symbol"),
			record.get("Name of security"),
			record.get("Action"),
			Integer.parseInt(record.get("Quantity|Face Value").split(" ")[0]),
			getOrderType(record.get("Price")),
			getLimitPrice(record.get("Price")),
			getTiming(record.get("Timing")),
			LocalDateTime.parse(record.get("Time and Date (ET)"), TIME_AND_DATE_FORMAT),
			record.get("Status"),
			Integer.parseInt(record.get("Order Number")));
		// @formatter:on
	}

	public String toCsvString()
	{
		return List.of(symbol, nameOfSecurity, action, quantity, orderType, limitPrice, timing, timeAndDate_ET, status, orderNumber).stream()
			.map(Object::toString).collect(Collectors.joining(","));
	}

	private static String getOrderType(String price)
	{
		return price.contains(" ") ? price.split(" ")[0] : price;
	}

	private static Double getLimitPrice(String price)
	{
		return price.contains(" ") ? StringUtils.parsePrice(price.split(" ")[1]) : null;
	}

	private static LocalDate getTiming(String timing)
	{
		return timing.equals("Day Only") ? LocalDate.now() : SchwabUtils.parseDate(timing.split(" ")[2]);
	}
}
