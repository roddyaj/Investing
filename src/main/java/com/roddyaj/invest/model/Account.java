package com.roddyaj.invest.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.roddyaj.invest.api.model.Quote;
import com.roddyaj.invest.api.model.QuoteProvider;
import com.roddyaj.invest.api.schwab.SchwabOpenOrdersSource;
import com.roddyaj.invest.api.schwab.SchwabPositionsSource;
import com.roddyaj.invest.api.schwab.SchwabTransactionsSource;
import com.roddyaj.invest.model.settings.AccountSettings;
import com.roddyaj.invest.util.AppFileUtils;

public class Account implements QuoteProvider
{
	private final String name;
	private final AccountSettings accountSettings;
	private final AllocationMap allocation;
	private final SchwabPositionsSource positionsSource;
	private final SchwabTransactionsSource transactionsSource;
	private final SchwabOpenOrdersSource openOrdersSource;
	private boolean costBasisCalculated;
	private final List<Message> messages = new ArrayList<>();

	public Account(String name, AccountSettings accountSettings)
	{
		this.name = AppFileUtils.getFullAccountName(name);
		this.accountSettings = accountSettings;
		positionsSource = new SchwabPositionsSource(this.name);
		transactionsSource = new SchwabTransactionsSource(accountSettings);
		openOrdersSource = new SchwabOpenOrdersSource(accountSettings);

		// Create the allocation map
		double untrackedTotal = getPositions().stream().filter(p -> p.getQuantity() > 0 && !accountSettings.hasAllocation(p.getSymbol()))
				.mapToDouble(Position::getMarketValue).sum();
		double untrackedPercent = untrackedTotal / getTotalValue();
		allocation = new AllocationMap(accountSettings.getAllocations(), untrackedPercent, messages);
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public Quote getQuote(String symbol)
	{
		return getPositions(symbol).map(p -> {
			return new Quote(p.isOption() ? p.getOption().getUnderlyingPrice() : p.getPrice(), p.isOption() ? null : p.getDayChangePct());
		}).findFirst().orElse(null);
	}

	public LocalDate getDate()
	{
		return positionsSource.getDate();
	}

	public AccountSettings getAccountSettings()
	{
		return accountSettings;
	}

	public double getAllocation(String symbol)
	{
		return allocation.getAllocation(symbol);
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

	public List<Message> getMessages()
	{
		return messages;
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
