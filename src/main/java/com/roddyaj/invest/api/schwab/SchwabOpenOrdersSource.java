package com.roddyaj.invest.api.schwab;

import java.nio.file.Path;
import java.util.List;

import com.roddyaj.invest.model.OpenOrder;
import com.roddyaj.invest.model.Option;
import com.roddyaj.invest.model.settings.AccountSettings;
import com.roddyaj.invest.util.AppFileUtils;
import com.roddyaj.schwabparse.SchwabOrder;
import com.roddyaj.schwabparse.SchwabOrdersReader;

public class SchwabOpenOrdersSource
{
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
			Path ordersFile = getAccountFile();
			if (ordersFile != null)
				openOrders = new SchwabOrdersReader().read(ordersFile).getOpenOrders().stream().map(SchwabOpenOrdersSource::convert).toList();
			else
				openOrders = List.of();
		}
		return openOrders;
	}

	private Path getAccountFile()
	{
		return AppFileUtils.getAccountFile(accountSettings.getAccountNumber() + " Order Details.*\\.CSV",
			(p1, p2) -> SchwabOrdersReader.getTime(p2).compareTo(SchwabOrdersReader.getTime(p1)));
	}

	private static OpenOrder convert(SchwabOrder order)
	{
		int quantity = order.quantity();
		if (order.action().startsWith("Sell"))
			quantity *= -1;

		Option option = SchwabUtils.parseOptionText(order.symbol());

		// @formatter:off
		return new OpenOrder(
			option != null ? option.getSymbol() : order.symbol(),
			quantity,
			order.limitPrice() != null ? order.limitPrice() : 0,
			option);
		// @formatter:on
	}
}
