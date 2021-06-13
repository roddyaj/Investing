package com.roddyaj.invest.model;

import java.time.LocalDate;

import com.roddyaj.invest.util.StringUtils;

public class Option
{
	public final String symbol;
	public final LocalDate expiryDate;
	public final double strike;
	public final char type;
	public final String money;
	public final double intrinsicValue;

	public Option(String optionText)
	{
		this(optionText, null, 0);
	}

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

	public double getUnderlyingPrice()
	{
		return type == 'P' ? strike - intrinsicValue : strike + intrinsicValue;
	}

	@Override
	public String toString()
	{
		return "Option [symbol=" + symbol + ", expiryDate=" + expiryDate + ", strike=" + strike + ", type=" + type + ", money=" + money
				+ ", intrinsicValue=" + intrinsicValue + ", getUnderlyingPrice=" + getUnderlyingPrice() + "]";
	}
}
