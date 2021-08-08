package com.roddyaj.invest.model;

import org.apache.commons.csv.CSVRecord;

import com.roddyaj.invest.util.StringUtils;

public class OptionableStock
{
	private static final String SYMBOL = "Symbol";
	private static final String PRICE = "Price";

	public final String symbol;
	public final double price;

	public OptionableStock(CSVRecord record)
	{
		symbol = record.get(SYMBOL);
		price = StringUtils.parsePrice(record.get(PRICE));
	}

	@Override
	public String toString()
	{
		return "OptionableStock [symbol=" + symbol + ", price=" + price + "]";
	}
}
