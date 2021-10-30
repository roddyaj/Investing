package com.roddyaj.invest.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

public final class StringUtils
{
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

	public static double parsePrice(String s)
	{
		double value = 0;
		if (!(s == null || s.isBlank() || "--".equals(s) || "N/A".equals(s)))
		{
			try
			{
				value = Double.parseDouble(s.replace("$", "").replace(",", ""));
			}
			catch (NumberFormatException e)
			{
				System.out.println("parsePrice " + e);
			}
		}
		return value;
	}

	public static double parsePercent(String s)
	{
		double value = 0;
		if (!(s == null || s.isBlank() || "--".equals(s) || "N/A".equals(s)))
		{
			try
			{
				value = Double.parseDouble(s.replace("%", "").replace(",", ""));
			}
			catch (NumberFormatException e)
			{
				System.out.println("parsePercent " + e);
			}
		}
		return value;
	}

	public static double parseDouble(String s)
	{
		double value = 0;
		if (!(s == null || s.isBlank() || "--".equals(s) || "N/A".equals(s)))
		{
			try
			{
				value = Double.parseDouble(s.replace(",", ""));
			}
			catch (NumberFormatException e)
			{
				System.out.println("parseDouble " + e);
			}
		}
		return value;
	}

	public static int parseInt(String s)
	{
		int value = 0;
		if (!(s == null || s.isBlank() || "--".equals(s) || "N/A".equals(s)))
		{
			try
			{
				value = Integer.parseInt(s.replace(",", ""));
			}
			catch (NumberFormatException e)
			{
				System.out.println("parseInt " + e);
			}
		}
		return value;
	}

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

	public static String limit(String s, int length)
	{
		return s.substring(0, Math.min(length, s.length()));
	}

	public static String fill(char c, int length)
	{
		char[] chars = new char[length];
		Arrays.fill(chars, c);
		return new String(chars);
	}

	private StringUtils()
	{
	}
}
