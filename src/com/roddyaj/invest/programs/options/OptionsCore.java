package com.roddyaj.invest.programs.options;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.model.Transaction;
import com.roddyaj.invest.util.Pair;

public class OptionsCore
{
	public void run(Collection<? extends Position> positions, Collection<? extends Transaction> transactions)
	{
//		transactions.forEach(System.out::println);
//		positions.forEach(System.out::println);

		analyzeBuyToClose(positions);
		analyzeCalls(positions, transactions);
		analyzePuts(positions, transactions);
		currentPositions(positions);
		monthlyIncome(transactions);
	}

	private void analyzeBuyToClose(Collection<? extends Position> positions)
	{
		System.out.println("\nBuy To Close:");
		positions.stream().filter(p -> p.isOption() && (p.marketValue / p.costBasis) < .1).forEach(System.out::println);
	}

	private void analyzeCalls(Collection<? extends Position> positions, Collection<? extends Transaction> transactions)
	{
		Set<String> symbolsWithCalls = positions.stream().filter(Position::isCallOption).map(p -> p.symbol).collect(Collectors.toSet());

		Map<String, Double> symbolToLast100Buy = new HashMap<>();
		for (Transaction t : transactions)
		{
			if (!t.isOption() && t.action.equals("Buy") && t.quantity == 100 && !symbolToLast100Buy.containsKey(t.symbol))
				symbolToLast100Buy.put(t.symbol, t.price);
		}

		System.out.println("\nSell Calls:");
		positions.stream().filter(p -> !p.isOption() && p.quantity >= 100 && !symbolsWithCalls.contains(p.symbol)).forEach(p -> {
			String s = String.format("%-4s %s (bought $%5.2f)", p.symbol, p.dayChangePct >= 0 ? "Y" : "N",
					symbolToLast100Buy.getOrDefault(p.symbol, 0.));
			System.out.println(s);
		});
	}

	private void analyzePuts(Collection<? extends Position> positions, Collection<? extends Transaction> transactions)
	{
		List<Transaction> historicalPuts = transactions.stream().filter(Transaction::isPutOption).collect(Collectors.toList());

		// Get list of CSP candidates based on historical activity
		List<String> putCandidates = new ArrayList<>();
		Set<String> historicalPutSymbols = historicalPuts.stream().map(Transaction::getSymbol).collect(Collectors.toSet());
		for (String symbol : historicalPutSymbols)
		{
			boolean haveCurrentPosition = positions.stream().anyMatch(p -> p.symbol.equals(symbol) && (p.isPutOption() || p.quantity > 50));
			if (!haveCurrentPosition)
				putCandidates.add(symbol);
		}

		// Calculate historical return on each one
		List<Pair<String, Double>> candidatesWithReturn = new ArrayList<>();
		for (String symbol : putCandidates)
		{
			double averageReturn = historicalPuts.stream().filter(t -> t.symbol.equals(symbol) && t.action.equals("Sell to Open"))
					.collect(Collectors.averagingDouble(t -> t.annualReturn));
			candidatesWithReturn.add(new Pair<>(symbol, averageReturn));
		}
		Collections.sort(candidatesWithReturn, (o1, o2) -> o2.right.compareTo(o1.right));

		// Format output
		System.out.println("\nSell Puts:");
		candidatesWithReturn.stream().forEach(p -> System.out.println(String.format("%-4s (%.0f%% return)", p.left, p.right)));
	}

	private void currentPositions(Collection<? extends Position> positions)
	{
		System.out.println("\nCurrent Options:");
		positions.stream().filter(Position::isPutOption).forEach(System.out::println);
		positions.stream().filter(Position::isCallOption).forEach(System.out::println);
	}

	private void monthlyIncome(Collection<? extends Transaction> transactions)
	{
		System.out.println("\nMonthly Income:");
		Map<String, Double> monthToIncome = new HashMap<>();
		final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM");
		for (Transaction transaction : transactions)
		{
			if (transaction.isOption() && (transaction.action.startsWith("Sell to") || transaction.action.startsWith("Buy to")))
				monthToIncome.merge(transaction.date.format(format), transaction.amount, Double::sum);
		}
		monthToIncome.entrySet().stream().sorted((e1, e2) -> e2.getKey().compareTo(e1.getKey()))
				.forEach(e -> System.out.println(String.format("%s %7.2f", e.getKey(), e.getValue())));
	}
}