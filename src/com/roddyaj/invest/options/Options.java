package com.roddyaj.invest.options;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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

public class Options implements Program
{
	public static void main(String[] args)
	{
		new Options().run(null);
	}

	@Override
	public void run(String[] args)
	{
		List<Transaction> optionTransactions = FileUtils.readCsv("Adam_Investing_Transactions").stream()
				.filter(r -> r.size() > 1 && (r.get(1).startsWith("Sell to") || r.get(1).startsWith("Buy to"))).map(Transaction::new)
				.collect(Collectors.toList());

		List<Position> positions = FileUtils.readCsv("Adam_Investing-Positions").stream().filter(r -> r.getRecordNumber() > 2).map(Position::new)
				.collect(Collectors.toList());

		analyzeBuyToClose(positions);
		analyzeCalls(positions);
		analyzePuts(positions, optionTransactions);
		monthlyIncome(optionTransactions);
	}

	private void analyzeBuyToClose(Collection<? extends Position> positions)
	{
		System.out.println("\nBuy To Close:");
		positions.stream().filter(p -> "Option".equals(p.securityType) && (p.marketValue / p.costBasis) < .1)
				.forEach(p -> System.out.println(p.symbol));
	}

	private void analyzeCalls(Collection<? extends Position> positions)
	{
		System.out.println("\nSell Calls:");
		Set<String> symbolsWithCalls = positions.stream().filter(p -> "Option".equals(p.securityType) && p.symbol.endsWith(" C"))
				.map(p -> p.symbol.split(" ")[0]).collect(Collectors.toSet());
		positions.stream().filter(p -> !"Option".equals(p.securityType) && p.quantity >= 100 && !symbolsWithCalls.contains(p.symbol))
				.forEach(p -> System.out.println(p.symbol));

	}

	private void analyzePuts(Collection<? extends Position> positions, Collection<? extends Transaction> transactions)
	{
		List<Pair<String, Double>> candidates = new ArrayList<>();

		Map<String, Integer> symbolToQuantity = positions.stream().collect(Collectors.toMap(r -> r.symbol, r -> r.quantity));
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

			if (symbolToQuantity.containsKey(symbol) && symbolToQuantity.get(symbol) > 50)
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
				.forEach(p -> System.out.println(String.format("%-4s (%.0f %% return)", p.left, p.right)));
	}

	private void monthlyIncome(Collection<? extends Transaction> transactions)
	{
		System.out.println("\nMonthly Income:");
		Map<String, Double> monthToIncome = new HashMap<>();
		final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM");
		for (Transaction transaction : transactions)
			monthToIncome.merge(transaction.date.format(format), transaction.amount, Double::sum);
		monthToIncome.entrySet().stream().sorted((e1, e2) -> e2.getKey().compareTo(e1.getKey()))
				.forEach(e -> System.out.println(String.format("%s %.2f", e.getKey(), e.getValue())));
	}

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

	private static LocalDate parseDate(String s)
	{
		LocalDate date;
		try
		{
			date = LocalDate.parse(s, FORMATTER);
		}
		catch (DateTimeParseException e)
		{
			date = LocalDate.parse(s.split(" ")[0], FORMATTER);
		}
		return date;
	}

	private static double parsePrice(String s)
	{
		if (s.isBlank() || "--".equals(s) || "N/A".equals(s))
			return 0;
		return Double.parseDouble(s.replace("$", "").replace(",", ""));
	}

	private static int parseInt(String s)
	{
		try
		{
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e)
		{
			return 0;
		}
	}

	private static class Position
	{
		public final String symbol;
		public final int quantity;
		public final double marketValue;
		public final double costBasis;
		public final String securityType;

		public Position(CSVRecord record)
		{
			symbol = record.get(0);
			quantity = parseInt(record.get(2));
			marketValue = parsePrice(record.get(6));
			costBasis = parsePrice(record.get(9));
			securityType = record.size() > 24 ? record.get(24) : null;
		}

		@Override
		public String toString()
		{
			return String.format("%s %d %.2f %.2f %s", symbol, quantity, marketValue, costBasis, securityType);
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

		public final String symbol;
		public final LocalDate expiryDate;
		public final double strike;
		public final char type;

		public final int days;
		public final double annualReturn;

		public Transaction(CSVRecord record)
		{
			date = parseDate(record.get(0));
			action = record.get(1);
			option = record.get(2);
			quantity = Integer.parseInt(record.get(4));
			price = parsePrice(record.get(5));
			amount = parsePrice(record.get(7));

			String[] tokens = option.split(" ");
			symbol = tokens[0];
			expiryDate = parseDate(tokens[1]);
			strike = Double.parseDouble(tokens[2]);
			type = tokens[3].charAt(0);

			days = (int)ChronoUnit.DAYS.between(date, expiryDate);
			annualReturn = amount / strike * (365.0 / days);
		}

		@Override
		public String toString()
		{
			return String.format("%s %s %s %d %.2f %.2f %.2f %.1f", date, action, option, days, strike, price, amount, annualReturn);
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