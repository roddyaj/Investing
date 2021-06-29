package com.roddyaj.invest.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.roddyaj.invest.util.StringUtils;

public class Option
{
	public final String symbol;
	public final LocalDate expiryDate;
	public final double strike;
	public final char type;
	public final String money;
	public final double intrinsicValue;

	// Transaction constructor
	public Option(String optionText)
	{
		this(optionText, null, 0);
	}

	// Position constructor
	public Option(String optionText, String money, double intrinsicValue)
	{
		String[] tokens = optionText.split(" ");
		symbol = tokens[0];
		expiryDate = StringUtils.parseDate(tokens[1]);
		strike = Double.parseDouble(tokens[2]);
		type = tokens[3].charAt(0);
		this.money = money;
		this.intrinsicValue = intrinsicValue;
	}

	// Test constructor
	public Option(String symbol, LocalDate expiryDate, double strike, char type)
	{
		this(symbol, expiryDate, strike, type, null, 0);
	}

	// Full constructor
	public Option(String symbol, LocalDate expiryDate, double strike, char type, String money, double intrinsicValue)
	{
		this.symbol = symbol;
		this.expiryDate = expiryDate;
		this.strike = strike;
		this.type = type;
		this.money = money;
		this.intrinsicValue = intrinsicValue;
	}

	public int getDTE()
	{
		return (int)ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
	}

	public double getUnderlyingPrice()
	{
		return type == 'P' ? strike - intrinsicValue : strike + intrinsicValue;
	}

	@Override
	public String toString()
	{
		return String.format("%s %s %s %.2f %.2f %s", symbol, type, expiryDate, strike, getUnderlyingPrice(), money);
	}
}
