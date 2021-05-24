package com.roddyaj.invest.model;

import org.apache.commons.csv.CSVRecord;

import com.roddyaj.invest.util.StringUtils;

public class Position implements Comparable<Position>
{
	public final String symbol;
	public final int quantity;
	public final double price;
	public final double marketValue;
	public final double dayChangePct;
	public final double costBasis;
	public final String securityType;

	public final Option option;

	public Position(CSVRecord record)
	{
		String symbolOrOption = record.get(0);
		quantity = StringUtils.parseInt(record.get(2));
		price = StringUtils.parsePrice(record.get(3));
		marketValue = StringUtils.parsePrice(record.get(6));
		dayChangePct = StringUtils.parsePercent(record.get(8));
		costBasis = StringUtils.parsePrice(record.get(9));
		double intrinsicValue = record.size() > 22 ? StringUtils.parseDouble(record.get(22)) : 0;
		String money = record.size() > 23 ? record.get(23) : null;
		securityType = record.size() > 24 ? record.get(24) : null;

		if ("Option".equals(securityType))
		{
			option = new Option(symbolOrOption, money, intrinsicValue);
			symbol = option.symbol;
		}
		else
		{
			option = null;
			symbol = symbolOrOption;
		}
	}

	public boolean isOption()
	{
		return option != null;
	}

	public boolean isCallOption()
	{
		return isOption() && option.type == 'C';
	}

	public boolean isPutOption()
	{
		return isOption() && option.type == 'P';
	}

	public String toStringStock()
	{
		return String.format("%-5s %3d %7.2f %7.2f %7.2f", symbol, quantity, marketValue, costBasis, dayChangePct);
	}

	public String toStringOption()
	{
		String moneyText = "OTM".equals(option.money) ? " " : "*";
		return String.format("%-5s %2d %s %5.2f %s %s", symbol, quantity, option.expiryDate, option.strike, option.type, moneyText);
	}

	@Override
	public String toString()
	{
		return isOption() ? toStringOption() : toStringStock();
	}

	@Override
	public int compareTo(Position o)
	{
		return symbol.compareTo(o.symbol);
	}
}
