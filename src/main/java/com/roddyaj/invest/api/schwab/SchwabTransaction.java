package com.roddyaj.invest.api.schwab;

import java.time.LocalDate;

import org.apache.commons.csv.CSVRecord;

import com.roddyaj.invest.util.StringUtils;

public record SchwabTransaction(
	LocalDate date,
	String action,
	String symbol,
	String description,
	int quantity,
	double price,
	double feesAndComm,
	double amount)
{
	public SchwabTransaction(CSVRecord record)
	{
		// @formatter:off
		this(
			StringUtils.parseDate(record.get("Date")),
			record.get("Action"),
			record.get("Symbol"),
			record.get("Description"),
			(int)Math.round(StringUtils.parseDouble(record.get("Quantity"))),
			StringUtils.parsePrice(record.get("Price")),
			StringUtils.parsePrice(record.get("Fees & Comm")),
			StringUtils.parsePrice(record.get("Amount")));
		// @formatter:on
	}
}
