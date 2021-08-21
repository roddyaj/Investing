package com.roddyaj.invest.programs.options;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.roddyaj.invest.model.Input;
import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.model.Transaction;

public class OptionsCore
{
	private final Input input;

	private final List<Transaction> historicalOptions;

	private final OptionsOutput output;

	public OptionsCore(Input input)
	{
		this.input = input;
		historicalOptions = input.account.getTransactions().stream().filter(Transaction::isOption).collect(Collectors.toList());
		output = new OptionsOutput(input.account.getName());
	}

	public OptionsOutput run()
	{
		setUp();
		analyzeBuyToClose();
		analyzeCallsToSell();
		analyzePutsToSell();
		availableToTrade();
		currentPositions();
		monthlyIncome();
		return output;
	}

	private void setUp()
	{
		for (Position position : input.account.getPositions())
		{
			if (position.isOption())
			{
				// Set the opening date
				Transaction recentTransaction = historicalOptions.stream()
						.filter(o -> o.symbol.equals(position.symbol) && "Sell to Open".equals(o.action)).findFirst().orElse(null);
				if (recentTransaction != null)
					position.option.initialDate = recentTransaction.date;

				// Set the underlying position if available
				Position underlying = input.account.getPositions(position.symbol).filter(p -> !p.isOption()).findAny().orElse(null);
				position.option.underlying = underlying;
			}
		}
	}

	private void analyzeBuyToClose()
	{
		input.account.getPositions().stream()
				.filter(p -> p.isOption() && p.getOptionValueRatio() <= .65
						&& (p.isPutOption() || p.option.getUnderlyingPrice() > p.option.underlying.getCostPerShare()))
				.forEach(output.buyToClose::add);
	}

	private void analyzeCallsToSell()
	{
		for (Position position : input.account.getPositions())
		{
			if (!position.isOption() && position.quantity >= 100 && !input.account.getSettings().excludeOption(position.symbol))
			{
				int totalCallsSold = input.account.getPositions().stream().filter(p -> p.symbol.equals(position.symbol) && p.isCallOption())
						.mapToInt(p -> p.quantity).sum();
				int availableShares = position.quantity + totalCallsSold * 100;
				int availableCalls = (int)Math.floor(availableShares / 100.0);
				boolean isUpAtAll = position.dayChangePct > -.1 || position.gainLossPct > -.1;
				if (availableCalls > 0 && isUpAtAll)
					output.callsToSell.add(new CallToSell(position, availableCalls));
			}
		}
	}

	private void analyzePutsToSell()
	{
		Set<String> symbols = new HashSet<>();

//		// Get list of CSP candidates based on historical activity
//		symbols.addAll(historicalOptions.stream().map(Transaction::getSymbol)
//				.filter(s -> !input.account.getSettings().excludeOption(s) && !s.matches(".*\\d.*")).collect(Collectors.toSet()));

//		// Add in other candidates
//		Set<String> optionable = input.information.getOptionableStocks().stream().map(r -> r.symbol).collect(Collectors.toSet());
//		String[] guruCodes = new String[] { "SAM" };
//		Set<String> guruPicks = input.information.getDataromaStocks(guruCodes).stream().map(r -> r.symbol).collect(Collectors.toSet());
//		Set<String> intersection = new HashSet<>(optionable);
//		intersection.retainAll(guruPicks);
//		symbols.addAll(intersection);

		// Add in options from the config
		symbols.addAll(Arrays.asList(input.account.getSettings().getOptionsInclude()));

		// Create the orders with amount available
		for (String symbol : symbols)
		{
			double price = input.getPrice(symbol);

			List<Position> symbolPositions = input.account.getPositions(symbol).collect(Collectors.toList());
			// Existing position: see if we can sell more put(s)
			if (!symbolPositions.isEmpty())
			{
				Position underlying = symbolPositions.stream().filter(p -> !p.isOption()).findFirst().orElse(null);
				int shareCount = underlying != null ? underlying.quantity : 0;
				int putsSold = symbolPositions.stream().filter(p -> p.isPutOption()).mapToInt(p -> p.quantity).sum();
				double totalInvested = (shareCount + putsSold * -100) * price;
				double available = input.account.getAccountSettings().getMaxOptionPosition() - totalInvested;
				double canSellCount = available / (price * 100); // Hack: using price in place of strike since we don't have strike
				boolean isUpForDay = underlying != null && underlying.dayChangePct > .1;
				if (canSellCount > 0.9 && !isUpForDay)
					output.putsToSell.add(new PutToSell(symbol, available, price, underlying));
			}
			// New position
			else
				output.putsToSell.add(new PutToSell(symbol, input.account.getAccountSettings().getMaxOptionPosition(), price, null));
		}

		// Calculate historical return on each one
		for (PutToSell put : output.putsToSell)
		{
			put.averageReturn = calculateAverageReturn(put.symbol, historicalOptions);
		}
	}

	private void availableToTrade()
	{
		List<Position> positions = input.account.getPositions();
		double cashBalance = positions.stream().filter(p -> p.symbol.equals("Cash & Cash Investments")).mapToDouble(p -> p.marketValue).findAny()
				.orElse(0);
		double putOnHold = positions.stream().filter(Position::isPutOption).mapToDouble(p -> p.option.strike * p.quantity * -100).sum();
		output.availableToTrade = cashBalance - putOnHold;
	}

	private void currentPositions()
	{
		input.account.getPositions().stream().filter(Position::isCallOption).sorted().forEach(output.currentPositions::add);
		input.account.getPositions().stream().filter(Position::isPutOption).sorted().forEach(output.currentPositions::add);
	}

	private void monthlyIncome()
	{
		final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM");
		for (Transaction transaction : historicalOptions)
		{
			if (transaction.action.startsWith("Sell to") || transaction.action.startsWith("Buy to"))
				output.monthToIncome.merge(transaction.date.format(format), transaction.amount, Double::sum);
		}
	}

	private static double calculateAverageReturn(String symbol, Collection<? extends Transaction> historicalOptions)
	{
		return historicalOptions.stream().filter(t -> t.symbol.equals(symbol) && t.action.equals("Sell to Open"))
				.collect(Collectors.averagingDouble(t -> t.annualReturn));
	}
}