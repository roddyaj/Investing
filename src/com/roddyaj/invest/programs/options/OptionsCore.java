package com.roddyaj.invest.programs.options;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.roddyaj.invest.model.Account;
import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.model.Transaction;

public class OptionsCore
{
	public OptionsOutput run(Account account)
	{
//		transactions.forEach(System.out::println);
//		positions.forEach(System.out::println);

		OptionsOutput output = new OptionsOutput(account.getName());

		analyzeBuyToClose(account, output);
		analyzeCalls(account, output);
		analyzePuts(account, output);
		availableToTrade(account, output);
		currentPositions(account, output);
		monthlyIncome(account, output);

		return output;
	}

	private void analyzeBuyToClose(Account account, OptionsOutput output)
	{
		account.getPositions().stream().filter(p -> p.isOption() && (p.marketValue / p.costBasis) <= .15).forEach(output.buyToClose::add);
	}

	private void analyzeCalls(Account account, OptionsOutput output)
	{
		Map<String, Double> symbolToLast100Buy = new HashMap<>();
		for (Transaction t : account.getTransactions())
		{
			if (!t.isOption() && t.action.equals("Buy") && t.quantity == 100 && !symbolToLast100Buy.containsKey(t.symbol))
				symbolToLast100Buy.put(t.symbol, t.price);
		}

		for (Position position : account.getPositions())
		{
			if (!position.isOption() && position.quantity >= 100)
			{
				int totalCallsSold = account.getPositions().stream().filter(p -> p.symbol.equals(position.symbol) && p.isCallOption())
						.mapToInt(p -> p.quantity).sum();
				int availableShares = position.quantity + totalCallsSold * 100;
				int availableCalls = (int)Math.floor(availableShares / 100.0);
				if (availableCalls > 0)
					output.callsToSell.add(new CallToSell(position, symbolToLast100Buy.getOrDefault(position.symbol, 0.), availableCalls));
			}
		}
	}

	private void analyzePuts(Account account, OptionsOutput output)
	{
		final double MAX_ALLOCATION = 2500;

		List<Transaction> historicalOptions = account.getTransactions().stream().filter(Transaction::isOption).collect(Collectors.toList());

		// Get list of CSP candidates based on historical activity
		Set<String> historicalSymbols = historicalOptions.stream().map(Transaction::getSymbol).collect(Collectors.toSet());
		for (String symbol : historicalSymbols)
		{
			List<Position> symbolPositions = account.getPositions().stream().filter(p -> p.symbol.equals(symbol)).collect(Collectors.toList());
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

	private void availableToTrade(Account account, OptionsOutput output)
	{
		double cashBalance = account.getPositions().stream().filter(p -> p.symbol.equals("Cash & Cash Investments")).mapToDouble(p -> p.marketValue)
				.findAny().orElse(0);
		double putOnHold = account.getPositions().stream().filter(Position::isPutOption).mapToDouble(p -> p.option.strike * p.quantity * -100).sum();
		output.availableToTrade = cashBalance - putOnHold;
	}

	private void currentPositions(Account account, OptionsOutput output)
	{
		account.getPositions().stream().filter(Position::isCallOption).sorted().forEach(output.currentPositions::add);
		account.getPositions().stream().filter(Position::isPutOption).sorted().forEach(output.currentPositions::add);
	}

	private void monthlyIncome(Account account, OptionsOutput output)
	{
		final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM");
		for (Transaction transaction : account.getTransactions())
		{
			if (transaction.isOption() && (transaction.action.startsWith("Sell to") || transaction.action.startsWith("Buy to")))
				output.monthToIncome.merge(transaction.date.format(format), transaction.amount, Double::sum);
		}
	}
}