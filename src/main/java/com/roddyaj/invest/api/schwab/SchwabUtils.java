package com.roddyaj.invest.api.schwab;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.roddyaj.invest.model.Option;

public final class SchwabUtils
{
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("M/d/yyyy");

	public static boolean parseBoolean(String value)
	{
		return "Yes".equals(value);
	}

	public static Integer parseInt(String value)
	{
		Integer intValue = null;
		if (isPresent(value))
		{
			try
			{
				intValue = Integer.parseInt(sanitize(value));
			}
			catch (NumberFormatException e)
			{
				System.out.println("parseInt " + e);
			}
		}
		return intValue;
	}

	public static Double parseDouble(String value)
	{
		Double doubleValue = null;
		if (isPresent(value))
		{
			try
			{
				doubleValue = Double.parseDouble(sanitize(value));
			}
			catch (NumberFormatException e)
			{
				System.out.println("parseDouble " + e);
			}
		}
		return doubleValue;
	}

	public static String parseString(String value)
	{
		return isPresent(value) ? value : null;
	}

	public static LocalDate parseDate(String value)
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

	private static boolean isPresent(String value)
	{
		return !(value == null || value.isBlank() || "--".equals(value) || "N/A".equals(value));
	}

	private static String sanitize(String value)
	{
		return value.replace(",", "").replace("$", "").replace("%", "");
	}

	private SchwabUtils()
	{
	}
}
