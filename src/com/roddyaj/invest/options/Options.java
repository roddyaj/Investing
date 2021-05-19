package com.roddyaj.invest.options;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVRecord;

import com.roddyaj.invest.model.Program;
import com.roddyaj.invest.util.FileUtils;
import com.roddyaj.invest.util.StringUtils;

public class Options implements Program
{
	public static void main(String[] args)
	{
		new Options().run(null);
	}

	@Override
	public void run(String[] args)
	{
		List<Transaction> transactions = FileUtils.readCsv("Adam_Investing_Transactions").stream().filter(r -> r.getRecordNumber() > 2)
				.map(Transaction::new).collect(Collectors.toList());

		List<Position> positions = FileUtils.readCsv("Adam_Investing-Positions").stream().filter(r -> r.getRecordNumber() > 2).map(Position::new)
				.collect(Collectors.toList());

		analyzeBuyToClose(positions);
		analyzeCalls(positions, transactions);
		analyzePuts(positions, transactions);
		monthlyIncome(transactions);
	}

	private void analyzeBuyToClose(Collection<? extends Position> positions)
	{
		System.out.println("\nBuy To Close:");
		positions.stream().filter(p -> "Option".equals(p.securityType) && (p.marketValue / p.costBasis) < .1)
				.forEach(p -> System.out.println(p.symbol));
	}

	private void analyzeCalls(Collection<? extends Position> positions, Collection<? extends Transaction> transactions)
	{
		Set<String> symbolsWithCalls = positions.stream().filter(p -> "Option".equals(p.securityType) && p.symbol.endsWith(" C"))
				.map(p -> p.symbol.split(" ")[0]).collect(Collectors.toSet());

		Map<String, Double> symbolToLast100Buy = new HashMap<>();
		for (Transaction transaction : transactions)
		{
			if (!transaction.isOption && transaction.action.equals("Buy") && transaction.quantity == 100
					&& !symbolToLast100Buy.containsKey(transaction.symbol))
				symbolToLast100Buy.put(transaction.symbol, transaction.price);
		}

		System.out.println("\nSell Calls:");
		positions.stream().filter(p -> !"Option".equals(p.securityType) && p.quantity >= 100 && !symbolsWithCalls.contains(p.symbol)).forEach(p -> {
			String s = String.format("%-4s %s (bought $%5.2f)", p.symbol, p.dayChangePct >= 0 ? "Y" : "N",
					symbolToLast100Buy.getOrDefault(p.symbol, 0.));
			System.out.println(s);
		});
	}

	private void analyzePuts(Collection<? extends Position> positions, Collection<? extends Transaction> transactions)
	{
		List<Pair<String, Double>> candidates = new ArrayList<>();

		Map<String, Position> symbolToPosition = positions.stream().collect(Collectors.toMap(r -> r.symbol, r -> r));
		List<Transaction> allPuts = transactions.stream().filter(t -> t.type == 'P').collect(Collectors.toList());
		Set<String> allPutSymbols = allPuts.stream().map(t -> t.symbol).collect(Collectors.toSet());
		for (String symbol : allPutSymbols)
		{
			List<Transaction> symbolActivePuts = allPuts.stream().filter(t -> t.symbol.equals(symbol) && !LocalDate.now().isAfter(t.expiryDate))
					.collect(Collectors.toList());

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

	private void monthlyIncome(Collection<? extends Transaction> transactions)
	{
		System.out.println("\nMonthly Income:");
		Map<String, Double> monthToIncome = new HashMap<>();
		final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM");
		for (Transaction transaction : transactions)
		{
			if (transaction.isOption && (transaction.action.startsWith("Sell to") || transaction.action.startsWith("Buy to")))
				monthToIncome.merge(transaction.date.format(format), transaction.amount, Double::sum);
		}
		monthToIncome.entrySet().stream().sorted((e1, e2) -> e2.getKey().compareTo(e1.getKey()))
				.forEach(e -> System.out.println(String.format("%s %7.2f", e.getKey(), e.getValue())));
	}

	private static class Position
	{
		public final String symbol;
		public final int quantity;
		public final double marketValue;
		public final double dayChangePct;
		public final double costBasis;
		public final String securityType;

		public Position(CSVRecord record)
		{
			symbol = record.get(0);
			quantity = StringUtils.parseInt(record.get(2));
			marketValue = StringUtils.parsePrice(record.get(6));
			dayChangePct = StringUtils.parsePercent(record.get(8));
			costBasis = StringUtils.parsePrice(record.get(9));
			securityType = record.size() > 24 ? record.get(24) : null;
		}

		@Override
		public String toString()
		{
			return String.format("%-4s %3d %7.2f %7.2f %-23s %5.2f", symbol, quantity, marketValue, costBasis, securityType, dayChangePct);
		}
	}

	private static class Transaction
	{
		public final LocalDate date;
		public final String action;
		public final String option;
		public final int quantity;
		public final double price;
		public final double amount;

		public final boolean isOption;
		public final String symbol;
		public final LocalDate expiryDate;
		public final double strike;
		public final char type;

		public final int days;
		public final double annualReturn;

		public Transaction(CSVRecord record)
		{
			date = StringUtils.parseDate(record.get(0));
			action = record.get(1);
			String symbolOrOption = record.get(2);
			quantity = (int)Math.round(StringUtils.parseDouble(record.get(4)));
			price = StringUtils.parsePrice(record.get(5));
			amount = StringUtils.parsePrice(record.get(7));

			isOption = symbolOrOption.contains(" ");
			if (isOption)
			{
				String[] tokens = symbolOrOption.split(" ");
				option = symbolOrOption;
				symbol = tokens[0];
				expiryDate = StringUtils.parseDate(tokens[1]);
				strike = Double.parseDouble(tokens[2]);
				type = tokens[3].charAt(0);

				days = (int)ChronoUnit.DAYS.between(date, expiryDate);
				annualReturn = amount / strike * (365.0 / days);
			}
			else
			{
				option = null;
				symbol = symbolOrOption;
				expiryDate = null;
				strike = 0;
				type = '\0';

				days = 0;
				annualReturn = 0;
			}
		}

		@Override
		public String toString()
		{
			return String.format("%s %-14s %-5s %-23s %3d %2d %6.2f %7.2f %8.2f %5.1f", date, action, symbol, option, quantity, days, strike, price,
					amount, annualReturn);
		}
	}

	private static class Pair<T1, T2>
	{
		public final T1 left;
		public final T2 right;

		public Pair(T1 left, T2 right)
		{
			this.left = left;
			this.right = right;
		}
	}
}