package com.roddyaj.invest.api.schwab;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.roddyaj.invest.model.Option;

public final class SchwabUtils
{
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("M/d/yyyy");

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

	private static LocalDate parseDate(String value)
	{
		LocalDate date = null;
		if (isPresent(value) && value.contains("/"))
		{
			try
			{
				date = LocalDate.parse(value, DATE_FORMAT);
			}
			catch (DateTimeParseException e)
			{
				date = LocalDate.parse(value.split(" ")[0], DATE_FORMAT);
			}
		}
		return date;
	}

	private static boolean isPresent(String value)
	{
		return !(value == null || value.isBlank() || "--".equals(value) || "N/A".equals(value));
	}

	private SchwabUtils()
	{
	}
}
