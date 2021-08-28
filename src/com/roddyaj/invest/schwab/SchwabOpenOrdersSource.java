package com.roddyaj.invest.schwab;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVRecord;

import com.roddyaj.invest.model.OpenOrder;
import com.roddyaj.invest.model.Option;
import com.roddyaj.invest.model.settings.AccountSettings;
import com.roddyaj.invest.util.AppFileUtils;
import com.roddyaj.invest.util.FileUtils;
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

	private final AccountSettings accountSettings;

	private List<OpenOrder> openOrders;

	public SchwabOpenOrdersSource(AccountSettings accountSettings)
	{
		this.accountSettings = accountSettings;
	}

	public List<OpenOrder> getOpenOrders()
	{
		if (openOrders == null)
		{
			openOrders = List.of();

			Path ordersFile = AppFileUtils.getAccountFile(accountSettings.getAccountNumber() + " Order Details\\.CSV");
			if (ordersFile != null)
			{
				try
				{
					// Correct the file contents to be valid CSV
					List<String> lines = Files
							.lines(ordersFile).filter(line -> !line.isEmpty()).map(line -> line.replace("\" Shares", " Shares\"")
									.replace("\" Share", " Share\"").replace("\" Contracts", " Contracts\"").replace("\" Contract", " Contract\""))
							.collect(Collectors.toList());

					openOrders = FileUtils.readCsv(lines).stream().map(SchwabOpenOrdersSource::convert).filter(o -> o.getQuantity() != 0)
							.collect(Collectors.toList());
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		return openOrders;
	}

	private static OpenOrder convert(CSVRecord record)
	{
		String symbol = record.get(SYMBOL);
		String action = record.get(ACTION);
		int shareCount = Integer.parseInt(record.get(QUANTITY_FACE_VALUE).split(" ")[0]);
		double price = StringUtils.parsePrice(record.get(PRICE).split(" ")[1]);
		String status = record.get(STATUS);

		if (action.startsWith("Sell"))
			shareCount *= -1;
		if (!"OPEN".equals(status))
			shareCount = 0;

		Option option = SchwabUtils.parseOptionText(symbol);

		return new OpenOrder(symbol, shareCount, price, option);
	}
}
