package com.roddyaj.invest.model;

import java.time.LocalDate;

import com.roddyaj.invest.util.StringUtils;

public class Option
{
	public final String symbol;
	public final LocalDate expiryDate;
	public final double strike;
	public final char type;

	public Option(String optionText)
	{
		String[] tokens = optionText.split(" ");
		symbol = tokens[0];
		expiryDate = StringUtils.parseDate(tokens[1]);
		strike = Double.parseDouble(tokens[2]);
		type = tokens[3].charAt(0);
	}
}
