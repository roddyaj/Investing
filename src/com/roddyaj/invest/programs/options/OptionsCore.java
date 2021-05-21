package com.roddyaj.invest.programs.options;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
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
		positions.stream().filter(p -> p.isOption() && (p.marketValue / p.costBasis) < .1).forEach(p -> System.out.println(p.toStringOption()));
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
		List<Pair<String, Double>> candidates = new ArrayList<>();

		Map<String, Position> symbolToPosition = positions.stream().filter(p -> !p.isOption()).collect(Collectors.toMap(r -> r.symbol, r -> r));
		List<Transaction> allPuts = transactions.stream().filter(t -> t.isPutOption()).collect(Collectors.toList());
		Set<String> allPutSymbols = allPuts.stream().map(t -> t.symbol).collect(Collectors.toSet());
		for (String symbol : allPutSymbols)
		{
			List<Transaction> symbolActivePuts = allPuts.stream()
					.filter(t -> t.symbol.equals(symbol) && !LocalDate.now().isAfter(t.option.expiryDate)).collect(Collectors.toList());

			int quantity = 0;
			for (Transaction transaction : symbolActivePuts)
			{
				if (transaction.action.equals("Sell to Open"))
					quantity += transaction.quantity;
				else if (transaction.action.equals("Buy to Close"))
					quantity -= transaction.quantity;
			}

			if (symbolToPosition.containsKey(symbol) && symbolToPosition.get(symbol).quantity > 50)
				quantity++;

			if (quantity <= 0)
			{
				double averageReturn = allPuts.stream().filter(t -> t.symbol.equals(symbol) && t.action.equals("Sell to Open"))
						.collect(Collectors.averagingDouble(t -> t.annualReturn));
				candidates.add(new Pair<>(symbol, averageReturn));
			}
		}

		System.out.println("\nSell Puts:");
		candidates.stream().sorted((o1, o2) -> o2.right.compareTo(o1.right))
				.forEach(p -> System.out.println(String.format("%-4s (%.0f%% return)", p.left, p.right)));
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