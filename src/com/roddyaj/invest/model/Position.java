package com.roddyaj.invest.model;

import org.apache.commons.csv.CSVRecord;

import com.roddyaj.invest.util.StringUtils;

public class Position implements Comparable<Position>
{
	public final String symbol;
	public final int quantity;
	public final double marketValue;
	public final double dayChangePct;
	public final double costBasis;
	public final String securityType;

	public Option option;
	public String money;

	public Position(CSVRecord record)
	{
		String symbolOrOption = record.get(0);
		quantity = StringUtils.parseInt(record.get(2));
		marketValue = StringUtils.parsePrice(record.get(6));
		dayChangePct = StringUtils.parsePercent(record.get(8));
		costBasis = StringUtils.parsePrice(record.get(9));
		money = record.size() > 23 ? record.get(23) : null;
		securityType = record.size() > 24 ? record.get(24) : null;

		if (isOption())
		{
			option = new Option(symbolOrOption);
			symbol = option.symbol;
		}
		else
		{
			symbol = symbolOrOption;
		}
	}

	public boolean isOption()
	{
		return "Option".equals(securityType);
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
		String moneyText = "OTM".equals(money) ? " " : "*";
		return String.format("%-5s %2d %s %5.2f %s %s", symbol, quantity, option.expiryDate, option.strike, option.type, moneyText);
	}

	@Override
	public String toString()
	{
		return option == null ? toStringStock() : toStringOption();
	}

	@Override
	public int compareTo(Position o)
	{
		return symbol.compareTo(o.symbol);
	}
}
