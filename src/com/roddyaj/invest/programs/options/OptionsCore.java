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
		analyzeBuyToClose();
		analyzeCallsToSell();
		analyzePutsToSell();
		availableToTrade();
		currentPositions();
		monthlyIncome();
		return output;
	}

	private void analyzeBuyToClose()
	{
		input.account.getPositions().stream().filter(p -> p.isOption() && (p.marketValue / p.costBasis) <= .25).forEach(output.buyToClose::add);
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
				if (availableCalls > 0)
				{
					output.callsToSell.add(new CallToSell(position, availableCalls));
				}
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
			List<Position> symbolPositions = input.account.getPositions().stream().filter(p -> p.symbol.equals(symbol)).collect(Collectors.toList());
			if (!symbolPositions.isEmpty())
			{
				int shareCount = symbolPositions.stream().filter(p -> !p.isOption()).mapToInt(p -> p.quantity).sum();
				int putsSold = symbolPositions.stream().filter(p -> p.isPutOption()).mapToInt(p -> p.quantity).sum();
				double price = symbolPositions.stream().mapToDouble(p -> p.isOption() ? p.option.getUnderlyingPrice() : p.price).findFirst()
						.orElse(0);
				double totalInvested = (shareCount + putsSold * -100) * price;
				double available = input.account.getAccountSettings().getMaxOptionPosition() - totalInvested;
				double canSellCount = available / (price * 100); // Hack: using price in place of strike since we don't have strike
				if (canSellCount > 0.9)
					output.putsToSell.add(new PutToSell(symbol, available, input.getPrice(symbol)));
			}
			else
				output.putsToSell.add(new PutToSell(symbol, input.account.getAccountSettings().getMaxOptionPosition(), input.getPrice(symbol)));
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