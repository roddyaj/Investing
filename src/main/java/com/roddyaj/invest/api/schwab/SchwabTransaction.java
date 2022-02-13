package com.roddyaj.invest.api.schwab;

import java.time.LocalDate;

import org.apache.commons.csv.CSVRecord;

public record SchwabTransaction(
	LocalDate date,
	String action,
	String symbol,
	String description,
	Double quantity,
	Double price,
	Double feesAndComm,
	Double amount)
{
	public SchwabTransaction(CSVRecord record)
	{
		// @formatter:off
		this(
			SchwabUtils.parseDate(record.get("Date")),
			record.get("Action"),
			SchwabUtils.parseString(record.get("Symbol")),
			record.get("Description"),
			SchwabUtils.parseDouble(record.get("Quantity")),
			SchwabUtils.parseDouble(record.get("Price")),
			SchwabUtils.parseDouble(record.get("Fees & Comm")),
			SchwabUtils.parseDouble(record.get("Amount")));
		// @formatter:on
	}
}
