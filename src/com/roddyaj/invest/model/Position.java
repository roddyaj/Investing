package com.roddyaj.invest.model;

import org.apache.commons.csv.CSVRecord;

import com.roddyaj.invest.util.StringUtils;

public class Position
{
	public final String symbol;
	public final int quantity;
	public final double marketValue;
	public final double dayChangePct;
	public final double costBasis;
	public final String securityType;

	public Position(CSVRecord record)
	{
		symbol = record.get(0);
		quantity = StringUtils.parseInt(record.get(2));
		marketValue = StringUtils.parsePrice(record.get(6));
		dayChangePct = StringUtils.parsePercent(record.get(8));
		costBasis = StringUtils.parsePrice(record.get(9));
		securityType = record.size() > 24 ? record.get(24) : null;
	}

	public boolean isOption()
	{
		return "Option".equals(securityType);
	}

	public boolean isCallOption()
	{
		return isOption() && symbol.endsWith(" C");
	}

	public boolean isPutOption()
	{
		return isOption() && symbol.endsWith(" P");
	}

	public String toShortString()
	{
		return String.format("%-23s %3d", symbol, quantity);
	}

	@Override
	public String toString()
	{
		return String.format("%-5s %3d %7.2f %7.2f %-23s %5.2f", symbol, quantity, marketValue, costBasis, securityType, dayChangePct);
	}
}
