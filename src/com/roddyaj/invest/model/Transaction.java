package com.roddyaj.invest.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.apache.commons.csv.CSVRecord;

import com.roddyaj.invest.util.StringUtils;

public class Transaction
{
	private static final String DATE = "Date";
	private static final String ACTION = "Action";
	private static final String SYMBOL = "Symbol";
//	private static final String DESCRIPTION = "Description";
	private static final String QUANTITY = "Quantity";
	private static final String PRICE = "Price";
//	private static final String FEES_AND_COMM = "Fees & Comm";
	private static final String AMOUNT = "Amount";

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
		date = StringUtils.parseDate(record.get(DATE));
		action = record.get(ACTION);
		String symbolOrOption = record.get(SYMBOL);
		quantity = (int)Math.round(StringUtils.parseDouble(record.get(QUANTITY)));
		price = StringUtils.parsePrice(record.get(PRICE));
		amount = StringUtils.parsePrice(record.get(AMOUNT));

		boolean isOption = symbolOrOption.contains(" ");
		if (isOption)
		{
			option = new Option(symbolOrOption);
			symbol = option.symbol;
			days = (int)ChronoUnit.DAYS.between(date, option.expiryDate);
			annualReturn = ((amount / quantity) / option.strike) * (365.0 / days);
		}
		else
		{
			symbol = symbolOrOption;
		}
	}

	public String getSymbol()
	{
		return symbol;
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
		return isOption() ? toStringOption() : toStringStock();
	}
}
