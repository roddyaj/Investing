package com.roddyaj.invest.api.schwab;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.roddyaj.invest.model.Option;

public final class SchwabUtils
{
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

	public static LocalDate parseDate(String s)
	{
		LocalDate date = null;
		if (s.contains("/"))
		{
			try
			{
				date = LocalDate.parse(s, DATE_FORMAT);
			}
			catch (DateTimeParseException e)
			{
				date = LocalDate.parse(s.split(" ")[0], DATE_FORMAT);
			}
		}
		return date;
	}

	public static Option parseOptionText(String optionText)
	{
		if (optionText.indexOf(' ') != -1)
		{
			String[] tokens = optionText.split(" ");
			String symbol = tokens[0];
			LocalDate expiryDate = parseDate(tokens[1]);
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
