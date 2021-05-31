package com.roddyaj.invest.programs.options;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.roddyaj.invest.model.Input;
import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.model.Transaction;

public class OptionsCore
{
	public OptionsOutput run(Input input)
	{
		OptionsOutput output = new OptionsOutput(input.account.getName());

		analyzeBuyToClose(input, output);
		analyzeCalls(input, output);
		analyzePuts(input, output);
		availableToTrade(input, output);
		currentPositions(input, output);
		monthlyIncome(input, output);

		return output;
	}

	private void analyzeBuyToClose(Input input, OptionsOutput output)
	{
		input.account.getPositions().stream().filter(p -> p.isOption() && (p.marketValue / p.costBasis) <= .15).forEach(output.buyToClose::add);
	}

	private void analyzeCalls(Input input, OptionsOutput output)
	{
		Map<String, Double> symbolToLast100Buy = new HashMap<>();
		for (Transaction t : input.account.getTransactions())
		{
			if (!t.isOption() && t.action.equals("Buy") && t.quantity == 100 && !symbolToLast100Buy.containsKey(t.symbol))
				symbolToLast100Buy.put(t.symbol, t.price);
		}

		for (Position position : input.account.getPositions())
		{
			if (!position.isOption() && position.quantity >= 100)
			{
				int totalCallsSold = input.account.getPositions().stream().filter(p -> p.symbol.equals(position.symbol) && p.isCallOption())
						.mapToInt(p -> p.quantity).sum();
				int availableShares = position.quantity + totalCallsSold * 100;
				int availableCalls = (int)Math.floor(availableShares / 100.0);
				if (availableCalls > 0)
					output.callsToSell.add(new CallToSell(position, symbolToLast100Buy.getOrDefault(position.symbol, 0.), availableCalls));
			}
		}
	}

	private void analyzePuts(Input input, OptionsOutput output)
	{
		final double MAX_ALLOCATION = 2500;

		List<Transaction> historicalOptions = input.account.getTransactions().stream().filter(Transaction::isOption).collect(Collectors.toList());

		// Get list of CSP candidates based on historical activity
		Set<String> symbols = historicalOptions.stream().map(Transaction::getSymbol).collect(Collectors.toSet());

		// Add in other candidates
		Set<String> optionable = input.information.getOptionableStocks().stream().map(r -> r.symbol).collect(Collectors.toSet());
		String[] guruCodes = new String[] { "SAM" };
		Set<String> guruPicks = input.information.getDataromaStocks(guruCodes).stream().map(r -> r.symbol).collect(Collectors.toSet());
		Set<String> intersection = new HashSet<>(optionable);
		intersection.retainAll(guruPicks);
		symbols.addAll(intersection);

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

	private void availableToTrade(Input input, OptionsOutput output)
	{
		double cashBalance = input.account.getPositions().stream().filter(p -> p.symbol.equals("Cash & Cash Investments"))
				.mapToDouble(p -> p.marketValue).findAny().orElse(0);
		double putOnHold = input.account.getPositions().stream().filter(Position::isPutOption).mapToDouble(p -> p.option.strike * p.quantity * -100)
				.sum();
		output.availableToTrade = cashBalance - putOnHold;
	}

	private void currentPositions(Input input, OptionsOutput output)
	{
		input.account.getPositions().stream().filter(Position::isCallOption).sorted().forEach(output.currentPositions::add);
		input.account.getPositions().stream().filter(Position::isPutOption).sorted().forEach(output.currentPositions::add);
	}

	private void monthlyIncome(Input input, OptionsOutput output)
	{
		final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM");
		for (Transaction transaction : input.account.getTransactions())
		{
			if (transaction.isOption() && (transaction.action.startsWith("Sell to") || transaction.action.startsWith("Buy to")))
				output.monthToIncome.merge(transaction.date.format(format), transaction.amount, Double::sum);
		}
	}
}