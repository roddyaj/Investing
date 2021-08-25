package com.roddyaj.invest.schwab;

import java.time.LocalDate;

import org.apache.commons.csv.CSVRecord;

import com.roddyaj.invest.model.Action;
import com.roddyaj.invest.model.Option;
import com.roddyaj.invest.model.Transaction;
import com.roddyaj.invest.util.StringUtils;

public class SchwabTransactionsSource
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
		Action action = parseAction(record.get(ACTION));
		String symbolOrOption = record.get(SYMBOL);
		int quantity = (int)Math.round(StringUtils.parseDouble(record.get(QUANTITY)));
		double price = StringUtils.parsePrice(record.get(PRICE));
		double amount = StringUtils.parsePrice(record.get(AMOUNT));

		Option option = null;
		String symbol;
		boolean isOption = symbolOrOption.contains(" ");
		if (isOption)
		{
			option = SchwabUtils.parseOptionText(symbolOrOption);
			symbol = option.getSymbol();
		}
		else
		{
			symbol = symbolOrOption;
		}

		return new Transaction(date, action, symbol, quantity, price, amount, option);
	}

	private static Action parseAction(String s)
	{
		return switch (s)
		{
			case "Buy" -> Action.BUY;
			case "Sell" -> Action.SELL;
			case "Sell to Open" -> Action.SELL_TO_OPEN;
			case "Buy to Close" -> Action.BUY_TO_CLOSE;
			case "Buy to Open" -> Action.BUY_TO_OPEN;
			case "Sell to Close" -> Action.SELL_TO_CLOSE;
			default -> null;
		};
	}
}