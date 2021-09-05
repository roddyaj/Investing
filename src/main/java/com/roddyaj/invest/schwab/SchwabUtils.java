package com.roddyaj.invest.schwab;

import java.time.LocalDate;

import com.roddyaj.invest.model.Option;
import com.roddyaj.invest.util.StringUtils;

public final class SchwabUtils
{
	public static Option parseOptionText(String optionText)
	{
		if (optionText.indexOf(' ') != -1)
		{
			String[] tokens = optionText.split(" ");
			String symbol = tokens[0];
			LocalDate expiryDate = StringUtils.parseDate(tokens[1]);
			double strike = Double.parseDouble(tokens[2]);
			char type = tokens[3].charAt(0);
			return new Option(symbol, expiryDate, strike, type);
		}
		return null;
	}

	private SchwabUtils()
	{
	}
}
