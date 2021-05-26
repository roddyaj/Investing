package com.roddyaj.invest.programs.options;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.model.Transaction;

public class OptionsCore
{
	public OptionsOutput run(Collection<? extends Position> positions, Collection<? extends Transaction> transactions)
	{
//		transactions.forEach(System.out::println);
//		positions.forEach(System.out::println);

		OptionsOutput output = new OptionsOutput();

		analyzeBuyToClose(positions, output);
		analyzeCalls(positions, transactions, output);
		analyzePuts(positions, transactions, output);
		availableToTrade(positions, output);
		currentPositions(positions, output);
		monthlyIncome(transactions, output);

		return output;
	}

	private void analyzeBuyToClose(Collection<? extends Position> positions, OptionsOutput output)
	{
		positions.stream().filter(p -> p.isOption() && (p.marketValue / p.costBasis) <= .1).forEach(output.buyToClose::add);
	}

	private void analyzeCalls(Collection<? extends Position> positions, Collection<? extends Transaction> transactions, OptionsOutput output)
	{
		Map<String, Double> symbolToLast100Buy = new HashMap<>();
		for (Transaction t : transactions)
		{
			if (!t.isOption() && t.action.equals("Buy") && t.quantity == 100 && !symbolToLast100Buy.containsKey(t.symbol))
				symbolToLast100Buy.put(t.symbol, t.price);
		}

		for (Position position : positions)
		{
			if (!position.isOption() && position.quantity >= 100)
			{
				int totalCallsSold = positions.stream().filter(p -> p.symbol.equals(position.symbol) && p.isCallOption()).mapToInt(p -> p.quantity)
						.sum();
				int availableShares = position.quantity + totalCallsSold * 100;
				int availableCalls = (int)Math.floor(availableShares / 100.0);
				if (availableCalls > 0)
					output.callsToSell.add(new CallToSell(position, symbolToLast100Buy.getOrDefault(position.symbol, 0.), availableCalls));
			}
		}
	}

	private void analyzePuts(Collection<? extends Position> positions, Collection<? extends Transaction> transactions, OptionsOutput output)
	{
		final double MAX_ALLOCATION = 2500;

		List<Transaction> historicalOptions = transactions.stream().filter(Transaction::isOption).collect(Collectors.toList());

		// Get list of CSP candidates based on historical activity
		Set<String> historicalSymbols = historicalOptions.stream().map(Transaction::getSymbol).collect(Collectors.toSet());
		for (String symbol : historicalSymbols)
		{
			List<Position> symbolPositions = positions.stream().filter(p -> p.symbol.equals(symbol)).collect(Collectors.toList());
			if (!symbolPositions.isEmpty())
			{
				int shareCount = symbolPositions.stream().filter(p -> !p.isOption()).mapToInt(p -> p.quantity).sum();
				int putsSold = symbolPositions.stream().filter(p -> p.isPutOption()).mapToInt(p -> p.quantity).sum();
				double price = symbolPositions.stream().mapToDouble(p -> p.isOption() ? p.option.getUnderlyingPrice() : p.price).findFirst()
						.orElse(0);
				double totalInvested = (shareCount + putsSold * -100) * price;
				double available = MAX_ALLOCATION - totalInvested;
				double canSellCount = available / (price * 100); // Hack: using price in place of strike since we don't have strike
				if (canSellCount > 0.9)
					output.putsToSell.add(new PutToSell(symbol, available));
			}
			else
				output.putsToSell.add(new PutToSell(symbol, MAX_ALLOCATION));
		}

		// Calculate historical return on each one
		for (PutToSell put : output.putsToSell)
		{
			put.averageReturn = historicalOptions.stream().filter(t -> t.symbol.equals(put.symbol) && t.action.equals("Sell to Open"))
					.collect(Collectors.averagingDouble(t -> t.annualReturn));
		}

		Collections.sort(output.putsToSell);
	}

	private void availableToTrade(Collection<? extends Position> positions, OptionsOutput output)
	{
		double cashBalance = positions.stream().filter(p -> p.symbol.equals("Cash & Cash Investments")).mapToDouble(p -> p.marketValue).findAny()
				.orElse(0);
		double putOnHold = positions.stream().filter(Position::isPutOption).mapToDouble(p -> p.option.strike * p.quantity * -100).sum();
		output.availableToTrade = cashBalance - putOnHold;
	}

	private void currentPositions(Collection<? extends Position> positions, OptionsOutput output)
	{
		positions.stream().filter(Position::isPutOption).sorted().forEach(output.currentPositions::add);
		positions.stream().filter(Position::isCallOption).sorted().forEach(output.currentPositions::add);
	}

	private void monthlyIncome(Collection<? extends Transaction> transactions, OptionsOutput output)
	{
		final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM");
		for (Transaction transaction : transactions)
		{
			if (transaction.isOption() && (transaction.action.startsWith("Sell to") || transaction.action.startsWith("Buy to")))
				output.monthToIncome.merge(transaction.date.format(format), transaction.amount, Double::sum);
		}
	}
}