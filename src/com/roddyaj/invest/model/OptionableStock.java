package com.roddyaj.invest.model;

import org.apache.commons.csv.CSVRecord;

import com.roddyaj.invest.util.StringUtils;

public class OptionableStock
{
	public final String symbol;
	public final double price;

	public OptionableStock(CSVRecord record)
	{
		symbol = record.get(0);
		price = StringUtils.parsePrice(record.get(4));
	}

	@Override
	public String toString()
	{
		return "OptionableStock [symbol=" + symbol + ", price=" + price + "]";
	}
}
