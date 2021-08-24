package com.roddyaj.invest.schwab;

import java.time.LocalDate;

import org.apache.commons.csv.CSVRecord;

import com.roddyaj.invest.model.Option;
import com.roddyaj.invest.model.Transaction;
import com.roddyaj.invest.util.StringUtils;

public class SchwabTransactionSource
{
	private static final String DATE = "Date";
	private static final String ACTION = "Action";
	private static final String SYMBOL = "Symbol";
//	private static final String DESCRIPTION = "Description";
	private static final String QUANTITY = "Quantity";
	private static final String PRICE = "Price";
//	private static final String FEES_AND_COMM = "Fees & Comm";
	private static final String AMOUNT = "Amount";

	public static Transaction convert(CSVRecord record)
	{
		LocalDate date = StringUtils.parseDate(record.get(DATE));
		String action = record.get(ACTION);
		String symbolOrOption = record.get(SYMBOL);
		int quantity = (int)Math.round(StringUtils.parseDouble(record.get(QUANTITY)));
		double price = StringUtils.parsePrice(record.get(PRICE));
		double amount = StringUtils.parsePrice(record.get(AMOUNT));

		Option option = null;
		String symbol;
		boolean isOption = symbolOrOption.contains(" ");
		if (isOption)
		{
			option = new Option(symbolOrOption);
			symbol = option.symbol;
		}
		else
		{
			symbol = symbolOrOption;
		}

		return new Transaction(date, action, symbol, quantity, price, amount, option);
	}
}
