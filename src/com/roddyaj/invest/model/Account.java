package com.roddyaj.invest.model;

import java.time.LocalDate;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Stream;

import com.roddyaj.invest.model.settings.AccountSettings;
import com.roddyaj.invest.schwab.SchwabOpenOrdersSource;
import com.roddyaj.invest.schwab.SchwabPositionsSource;
import com.roddyaj.invest.schwab.SchwabTransactionsSource;
import com.roddyaj.invest.util.AppFileUtils;

public class Account
{
	private final String name;
	private final AccountSettings accountSettings;
	private final SchwabPositionsSource positionsSource;
	private final SchwabTransactionsSource transactionsSource;
	private final SchwabOpenOrdersSource openOrdersSource;

	public Account(String name, AccountSettings accountSettings)
	{
		this.name = AppFileUtils.getFullAccountName(name);
		this.accountSettings = accountSettings;
		positionsSource = new SchwabPositionsSource(this.name);
		transactionsSource = new SchwabTransactionsSource(accountSettings);
		openOrdersSource = new SchwabOpenOrdersSource(accountSettings);
	}

	public String getName()
	{
		return name;
	}

	public LocalDate getDate()
	{
		return positionsSource.getDate();
	}

	public AccountSettings getAccountSettings()
	{
		return accountSettings;
	}

	public List<Position> getPositions()
	{
		return positionsSource.getPositions();
	}

	public List<Transaction> getTransactions()
	{
		return transactionsSource.getTransactions();
	}

	public List<OpenOrder> getOpenOrders()
	{
		return openOrdersSource.getOpenOrders();
	}

	public int getOpenOrderCount(String symbol, Action action)
	{
		return getOpenOrderCount(symbol, action, null);
	}

	public int getOpenOrderCount(String symbol, Action action, Character optionType)
	{
		return Math.abs(getOpenOrders().stream().filter(order -> {
			boolean match = true;
			if (order.getOption() != null)
				match &= order.getOption().getSymbol().equals(symbol)
						&& (optionType == null || order.getOption().getType() == optionType.charValue());
			else
				match &= order.getSymbol().equals(symbol) && optionType == null;
			match &= action == Action.SELL ? order.getQuantity() < 0 : order.getQuantity() > 0;
			return match;
		}).mapToInt(OpenOrder::getQuantity).sum());
	}

	public Double getPrice(String symbol)
	{
		OptionalDouble price = getPositions(symbol).mapToDouble(p -> p.isOption() ? p.getOption().getUnderlyingPrice() : p.getPrice()).findFirst();
		return price.isPresent() ? price.getAsDouble() : null;
	}

	public Double getDayChange(String symbol)
	{
		OptionalDouble dayChangePct = getPositions(symbol).filter(p -> !p.isOption()).mapToDouble(Position::getDayChangePct).findFirst();
		return dayChangePct.isPresent() ? dayChangePct.getAsDouble() : null;
	}

	public double getTotalValue()
	{
		return getPositions("Account Total").mapToDouble(Position::getMarketValue).findFirst().orElse(0);
	}

	public Position getPosition(String symbol)
	{
		return getPositions(symbol).findFirst().orElse(null);
	}

	public boolean hasSymbol(String symbol)
	{
		return getPositions(symbol).findAny().isPresent();
	}

	public Stream<Position> getPositions(String symbol)
	{
		return getPositions().stream().filter(p -> p.getSymbol().equals(symbol));
	}
}
