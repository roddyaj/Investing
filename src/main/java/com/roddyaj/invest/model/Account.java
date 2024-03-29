package com.roddyaj.invest.model;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.roddyaj.invest.api.model.Quote;
import com.roddyaj.invest.api.model.QuoteProvider;
import com.roddyaj.invest.model.Message.Level;
import com.roddyaj.invest.model.settings.AccountSettings;

public class Account implements QuoteProvider, AccountDataSource
{
	private final AccountSettings accountSettings;
	private final AccountDataSource dataSource;
	private final AllocationMap allocation;
	private boolean costBasisCalculated;
	private final List<Message> messages = new ArrayList<>();

	public Account(AccountSettings accountSettings, AccountDataSource dataSource)
	{
		this.accountSettings = accountSettings;
		this.dataSource = dataSource;

		// Create the allocation map
		double untrackedTotal = getPositions().stream()
				.filter(p -> !p.isOption() && p.getQuantity() > 0 && !accountSettings.hasAllocation(p.getSymbol()))
				.mapToDouble(Position::getMarketValue).sum();
		double untrackedPercent = untrackedTotal / getTotalValue();
		allocation = new AllocationMap(accountSettings.getAllocations(), untrackedPercent, messages);

		ZonedDateTime dateTime = getDateTime();
		if (dateTime != null && !LocalDate.now().equals(dateTime.toLocalDate()))
			messages.add(new Message(Level.WARN, "Account data is not from today: " + dateTime));
	}

	@Override
	public String getName()
	{
		return accountSettings.getName();
	}

	@Override
	public Quote getQuote(String symbol)
	{
		return getPositions(symbol).map(p -> {
			return new Quote(p.isOption() ? p.getOption().getUnderlyingPrice() : p.getPrice(), p.isOption() ? null : p.getDayChangePct());
		}).findFirst().orElse(null);
	}

	public AccountSettings getAccountSettings()
	{
		return accountSettings;
	}

	public double getAllocation(String symbol)
	{
		return allocation.getAllocation(symbol);
	}

	public Map<String, Double> getAllocationMap()
	{
		return allocation.getAllocationMap();
	}

	@Override
	public ZonedDateTime getDateTime()
	{
		return dataSource.getDateTime();
	}

	@Override
	public double getTotalValue()
	{
		return dataSource.getTotalValue();
	}

	@Override
	public List<Position> getPositions()
	{
		List<Position> positions = dataSource.getPositions();
		if (!costBasisCalculated)
		{
			updateCostBasis(positions);
			costBasisCalculated = true;
		}
		return positions;
	}

	@Override
	public List<Transaction> getTransactions()
	{
		return dataSource.getTransactions();
	}

	@Override
	public List<OpenOrder> getOpenOrders()
	{
		return dataSource.getOpenOrders();
	}

	@Override
	public List<CompletePosition> getCompletePositions()
	{
		return dataSource.getCompletePositions();
	}

	public List<OpenOrder> getOpenOrders(String symbol, Action action, Character optionType)
	{
		return getOpenOrders().stream().filter(order -> {
			boolean match = true;
			if (order.option() != null)
				match &= order.option().getSymbol().equals(symbol) && (optionType != null && order.option().getType() == optionType.charValue());
			else
				match &= order.symbol().equals(symbol) && optionType == null;
			match &= action == Action.SELL ? order.quantity() < 0 : order.quantity() > 0;
			return match;
		}).toList();
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
			if (transaction.action() == Action.BUY)
			{
				costBasisMap.computeIfAbsent(transaction.symbol(), k -> new Lots()).add(transaction.quantity(), transaction.price());
			}
			else if (transaction.action() == Action.SELL)
			{
				Lots lots = costBasisMap.get(transaction.symbol());
				if (lots != null)
					lots.removeFifo(transaction.quantity());
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
