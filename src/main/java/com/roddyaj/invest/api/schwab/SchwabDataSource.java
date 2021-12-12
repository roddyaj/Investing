package com.roddyaj.invest.api.schwab;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.roddyaj.invest.html.HtmlUtils;
import com.roddyaj.invest.model.AccountDataSource;
import com.roddyaj.invest.model.Action;
import com.roddyaj.invest.model.OpenOrder;
import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.model.Transaction;
import com.roddyaj.invest.model.settings.AccountSettings;

public class SchwabDataSource implements AccountDataSource
{
	private static final String ALL_IN_ONE_URL = "https://client.schwab.com/Areas/Trade/Allinone/index.aspx";

	public static String getOptionUrl(String occString)
	{
		return ALL_IN_ONE_URL + "#symbol/" + occString.replace(' ', '+');
	}

	public static String getOptionChainsUrl(String symbol)
	{
		return "https://client.schwab.com/Areas/Trade/Options/Chains/Index.aspx#symbol/" + symbol;
	}

	public static String getTradeUrl(Action action, String symbol)
	{
		String tradeaction = action == Action.BUY ? "Buy" : action == Action.SELL ? "Sell" : null;
		return ALL_IN_ONE_URL + String.format("?tradeaction=%s&amp;Symbol=%s", tradeaction, symbol);
	}

	public static List<String> getNavigationLinks()
	{
		List<String> links = new ArrayList<>();
		links.add(HtmlUtils.toLink("https://client.schwab.com/Areas/Accounts/Positions", "Positions"));
		links.add(HtmlUtils.toLink("https://client.schwab.com/Apps/accounts/transactionhistory", "History"));
		links.add(HtmlUtils.toLink("https://client.schwab.com/Apps/Accounts/Balances", "Balances"));
		links.add(HtmlUtils.toLink("https://client.schwab.com/Trade/OrderStatus/ViewOrderStatus.aspx?ViewTypeFilter=Open", "Open Orders"));
		links.add(HtmlUtils.toLink("https://client.schwab.com/Trade/OrderStatus/ViewOrderStatus.aspx?ViewTypeFilter=Today", "Today's Orders"));
		return links;
	}

	private final SchwabPositionsSource positionsSource;
	private final SchwabTransactionsSource transactionsSource;
	private final SchwabOpenOrdersSource openOrdersSource;

	public SchwabDataSource(AccountSettings accountSettings)
	{
		positionsSource = new SchwabPositionsSource(accountSettings);
		transactionsSource = new SchwabTransactionsSource(accountSettings);
		openOrdersSource = new SchwabOpenOrdersSource(accountSettings);
	}

	@Override
	public LocalDate getDate()
	{
		return positionsSource.getDate();
	}

	@Override
	public double getTotalValue()
	{
		return getPositions().stream().filter(p -> p.getSymbol().equals("Account Total")).mapToDouble(Position::getMarketValue).findFirst().orElse(0);
	}

	@Override
	public List<Position> getPositions()
	{
		return positionsSource.getPositions();
	}

	@Override
	public List<Transaction> getTransactions()
	{
		return transactionsSource.getTransactions();
	}

	@Override
	public List<OpenOrder> getOpenOrders()
	{
		return openOrdersSource.getOpenOrders();
	}
}
