package com.roddyaj.invest.api.schwab;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVRecord;

public record SchwabPosition(
	String symbol,
	String description,
	Integer quantity,
	Double price,
	Double priceChange,
	Double priceChangePct,
	double marketValue,
	double dayChange,
	double dayChangePct,
	Double costBasis,
	Double gainLoss,
	Double gainLossPct,
	boolean reinvestDividends,
	boolean capitalGains,
	Double percentOfAccount,
	Double dividendYield,
	Double lastDividend,
	LocalDate exDividendDate,
	Double peRatio,
	Double _52WeekLow,
	Double _52WeekHigh,
	Integer volume,
	Double intrinsicValue,
	String inTheMoney,
	String securityType)
{
	public SchwabPosition(CSVRecord record)
	{
		// @formatter:off
		this(
			record.get("Symbol"),
			SchwabUtils.parseString(record.get("Description")),
			SchwabUtils.parseInt(record.get("Quantity")),
			SchwabUtils.parseDouble(record.get("Price")),
			SchwabUtils.parseDouble(record.get("Price Change $")),
			SchwabUtils.parseDouble(record.get("Price Change %")),
			SchwabUtils.parseDouble(record.get("Market Value")),
			SchwabUtils.parseDouble(record.get("Day Change $")),
			SchwabUtils.parseDouble(record.get("Day Change %")),
			SchwabUtils.parseDouble(record.get("Cost Basis")),
			SchwabUtils.parseDouble(record.get("Gain/Loss $")),
			SchwabUtils.parseDouble(record.get("Gain/Loss %")),
			SchwabUtils.parseBoolean(record.get("Reinvest Dividends?")),
			SchwabUtils.parseBoolean(record.get("Capital Gains?")),
			SchwabUtils.parseDouble(record.get("% Of Account")),
			SchwabUtils.parseDouble(record.get("Dividend Yield")),
			SchwabUtils.parseDouble(record.get("Last Dividend")),
			SchwabUtils.parseDate(record.get("Ex-Dividend Date")),
			SchwabUtils.parseDouble(record.get("P/E Ratio")),
			SchwabUtils.parseDouble(record.get("52 Week Low")),
			SchwabUtils.parseDouble(record.get("52 Week High")),
			SchwabUtils.parseInt(record.get("Volume")),
			record.isSet("Intrinsic Value") ? SchwabUtils.parseDouble(record.get("Intrinsic Value")) : null,
			record.isSet("In The Money") ? SchwabUtils.parseString(record.get("In The Money")) : null,
			record.isSet("Security Type") ? record.get("Security Type") : null);
		// @formatter:on
	}

	public String toCsvString()
	{
		return Arrays
			.asList(symbol, description, quantity, price, priceChange, priceChangePct, marketValue, dayChange, dayChangePct, costBasis, gainLoss,
				gainLossPct, reinvestDividends, capitalGains, percentOfAccount, dividendYield, lastDividend, exDividendDate, peRatio, _52WeekLow,
				_52WeekHigh, volume, intrinsicValue, inTheMoney, securityType)
			.stream().map(o -> o != null ? o.toString() : "").collect(Collectors.joining(","));
	}
}
