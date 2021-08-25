package com.roddyaj.invest.schwab;

import org.apache.commons.csv.CSVRecord;

import com.roddyaj.invest.programs.positions.Order;
import com.roddyaj.invest.util.StringUtils;

public class SchwabOpenOrdersSource
{
	private static final String SYMBOL = "Symbol";
//	private static final String NAME_OF_SECURITY = "Name of security";
	private static final String ACTION = "Action";
	private static final String QUANTITY_FACE_VALUE = "Quantity|Face Value";
	private static final String PRICE = "Price";
//	private static final String TIMING = "Timing";
//	private static final String TIME_AND_DATE_ET = "Time and Date (ET)";
	private static final String STATUS = "Status";
//	private static final String REINVEST_CAPITAL_GAINS = "Reinvest Capital Gains";
//	private static final String ORDER_NUMBER = "Order Number";
//	private static final String CHANGE_CANCEL_RESUBMIT = "Change|Cancel|Resubmit";

	public static Order convert(CSVRecord record)
	{
		String symbol = record.get(SYMBOL);
		String action = record.get(ACTION);
		int shareCount = Integer.parseInt(record.get(QUANTITY_FACE_VALUE).split(" ")[0]);
		double price = StringUtils.parsePrice(record.get(PRICE).split(" ")[1]);
		String status = record.get(STATUS);

		if ("Sell".equals(action))
			shareCount *= -1;
		if (!"OPEN".equals(status))
			shareCount = 0;

		return new Order(symbol, shareCount, price, null);
	}
}
