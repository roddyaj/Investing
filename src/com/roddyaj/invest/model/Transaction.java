package com.roddyaj.invest.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.apache.commons.csv.CSVRecord;

import com.roddyaj.invest.util.StringUtils;

public class Transaction
{
	public final LocalDate date;
	public final String action;
	public final String symbol;
	public final int quantity;
	public final double price;
	public final double amount;

	public Option option;
	public int days;
	public double annualReturn;

	private static final String STOCK_FORMAT = "%s %-14s %-5s %3d %6.2f %8.2f";
	private static final String OPTION_FORMAT = STOCK_FORMAT + "  %s %5.2f %s %2dd (%5.1f%%)";

	public Transaction(CSVRecord record)
	{
		date = StringUtils.parseDate(record.get(0));
		action = record.get(1);
		String symbolOrOption = record.get(2);
		quantity = (int)Math.round(StringUtils.parseDouble(record.get(4)));
		price = StringUtils.parsePrice(record.get(5));
		amount = StringUtils.parsePrice(record.get(7));

		boolean isOption = symbolOrOption.contains(" ");
		if (isOption)
		{
			option = new Option(symbolOrOption);
			symbol = option.symbol;
			days = (int)ChronoUnit.DAYS.between(date, option.expiryDate);
			annualReturn = amount / option.strike * (365.0 / days);
		}
		else
		{
			symbol = symbolOrOption;
		}
	}

	public boolean isOption()
	{
		return option != null;
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
		return String.format(STOCK_FORMAT, date, StringUtils.limit(action, 14), symbol, quantity, price, amount);
	}

	public String toStringOption()
	{
		return String.format(OPTION_FORMAT, date, StringUtils.limit(action, 14), symbol, quantity, price, amount, option.expiryDate, option.strike,
				option.type, days, annualReturn);
	}

	@Override
	public String toString()
	{
		return option == null ? toStringStock() : toStringOption();
	}
}
