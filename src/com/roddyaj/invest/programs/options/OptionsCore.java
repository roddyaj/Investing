package com.roddyaj.invest.programs.options;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.model.Transaction;
import com.roddyaj.invest.util.Pair;

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
		currentPositions(positions, output);
		monthlyIncome(transactions, output);

		return output;
	}

	private void analyzeBuyToClose(Collection<? extends Position> positions, OptionsOutput output)
	{
		positions.stream().filter(p -> p.isOption() && (p.marketValue / p.costBasis) < .1).forEach(output.buyToClose::add);
	}

	private void analyzeCalls(Collection<? extends Position> positions, Collection<? extends Transaction> transactions, OptionsOutput output)
	{
		Set<String> symbolsWithCalls = positions.stream().filter(Position::isCallOption).map(p -> p.symbol).collect(Collectors.toSet());

		for (Transaction t : transactions)
		{
			if (!t.isOption() && t.action.equals("Buy") && t.quantity == 100 && !output.symbolToLast100Buy.containsKey(t.symbol))
				output.symbolToLast100Buy.put(t.symbol, t.price);
		}

		positions.stream().filter(p -> !p.isOption() && p.quantity >= 100 && !symbolsWithCalls.contains(p.symbol)).forEach(output.callsToSell::add);
	}

	private void analyzePuts(Collection<? extends Position> positions, Collection<? extends Transaction> transactions, OptionsOutput output)
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
		for (String symbol : putCandidates)
		{
			double averageReturn = historicalPuts.stream().filter(t -> t.symbol.equals(symbol) && t.action.equals("Sell to Open"))
					.collect(Collectors.averagingDouble(t -> t.annualReturn));
			output.putsToSell.add(new Pair<>(symbol, averageReturn));
		}
		Collections.sort(output.putsToSell, (o1, o2) -> o2.right.compareTo(o1.right));

		// Calculate available cash to trade
		double cashBalance = positions.stream().filter(p -> p.symbol.equals("Cash & Cash Investments")).mapToDouble(p -> p.marketValue).findAny()
				.orElse(0);
		double putOnHold = positions.stream().filter(Position::isPutOption).mapToDouble(p -> p.option.strike * 100).sum();
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