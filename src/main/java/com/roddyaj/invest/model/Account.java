package com.roddyaj.invest.model;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;
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
	private boolean costBasisCalculated;

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
		List<Position> positions = positionsSource.getPositions();
		if (!costBasisCalculated)
		{
			updateCostBasis(positions);
			costBasisCalculated = true;
		}
		return positions;
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
		return Math.abs(getOpenOrders(symbol, action, optionType).stream().mapToInt(OpenOrder::getQuantity).sum());
	}

	public List<OpenOrder> getOpenOrders(String symbol, Action action, Character optionType)
	{
		return getOpenOrders().stream().filter(order -> {
			boolean match = true;
			if (order.getOption() != null)
				match &= order.getOption().getSymbol().equals(symbol)
						&& (optionType != null && order.getOption().getType() == optionType.charValue());
			else
				match &= order.getSymbol().equals(symbol) && optionType == null;
			match &= action == Action.SELL ? order.getQuantity() < 0 : order.getQuantity() > 0;
			return match;
		}).collect(Collectors.toList());
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
		// TODO Make this Schwab-agnostic
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

	private void updateCostBasis(Collection<Position> positions)
	{
		Map<String, Lots> costBasisMap = new HashMap<>();
		List<Transaction> transactions = getTransactions();
		for (int i = transactions.size() - 1; i >= 0; i--)
		{
			Transaction transaction = transactions.get(i);
			if (transaction.getAction() == Action.BUY)
			{
				costBasisMap.computeIfAbsent(transaction.getSymbol(), k -> new Lots()).add(transaction.getQuantity(), transaction.getPrice());
			}
			else if (transaction.getAction() == Action.SELL)
			{
				Lots lots = costBasisMap.get(transaction.getSymbol());
				if (lots != null)
					lots.removeFifo(transaction.getQuantity());
			}
		}

		for (Position position : positions)
		{
			if (!position.isOption())
			{
				Lots lots = costBasisMap.get(position.getSymbol());
				position.setLots(lots);
			}
		}
	}
}
