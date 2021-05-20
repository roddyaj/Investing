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
//		transactions.forEach(System.out::println);

		List<Position> positions = FileUtils.readCsv("Adam_Investing-Positions").stream().filter(r -> r.getRecordNumber() > 2).map(Position::new)
				.collect(Collectors.toList());
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
		positions.stream().filter(p -> p.isOption() && (p.marketValue / p.costBasis) < .1).forEach(p -> System.out.println(p.symbol));
	}

	private void analyzeCalls(Collection<? extends Position> positions, Collection<? extends Transaction> transactions)
	{
		Set<String> symbolsWithCalls = positions.stream().filter(Position::isCallOption).map(p -> p.symbol.split(" ")[0]).collect(Collectors.toSet());

		Map<String, Double> symbolToLast100Buy = new HashMap<>();
		for (Transaction t : transactions)
		{
			if (!t.isOption && t.action.equals("Buy") && t.quantity == 100 && !symbolToLast100Buy.containsKey(t.symbol))
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

		Map<String, Position> symbolToPosition = positions.stream().collect(Collectors.toMap(r -> r.symbol, r -> r));
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
		positions.stream().filter(p -> p.isPutOption()).forEach(p -> System.out.println(p.toShortString()));
		positions.stream().filter(p -> p.isCallOption()).forEach(p -> System.out.println(p.toShortString()));
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

		public boolean isOption()
		{
			return "Option".equals(securityType);
		}

		public boolean isCallOption()
		{
			return isOption() && symbol.endsWith(" C");
		}

		public boolean isPutOption()
		{
			return isOption() && symbol.endsWith(" P");
		}

		public String toShortString()
		{
			return String.format("%-23s %3d", symbol, quantity);
		}

		@Override
		public String toString()
		{
			return String.format("%-5s %3d %7.2f %7.2f %-23s %5.2f", symbol, quantity, marketValue, costBasis, securityType, dayChangePct);
		}
	}

	private static class Transaction
	{
		public final LocalDate date;
		public final String action;
		public final String symbol;
		public final int quantity;
		public final double price;
		public final double amount;

		public final boolean isOption;
		public Option option;
		public int days;
		public double annualReturn;

		private static final String STOCK_FORMAT = "%s %-14s %-5s %3d %6.2f %8.2f";
		private static final String OPTION_FORMAT = STOCK_FORMAT + "  %s %5.2f %s %2dd (%5.1f%%)";

		public Transaction(CSVRecord record)
		{
			date = StringUtils.parseDate(record.get(0));
			action = record.get(1);
			quantity = (int)Math.round(StringUtils.parseDouble(record.get(4)));
			price = StringUtils.parsePrice(record.get(5));
			amount = StringUtils.parsePrice(record.get(7));

			String symbolOrOption = record.get(2);
			isOption = symbolOrOption.contains(" ");
			if (isOption)
			{
				option = new Option(symbolOrOption);
				symbol = option.symbol;
				days = (int)ChronoUnit.DAYS.between(date, option.expiryDate);
				annualReturn = amount / option.strike * (365.0 / days);
			}
			else
			{
				symbol = symbolOrOption;
			}
		}

		public boolean isCallOption()
		{
			return isOption && option.type == 'C';
		}

		public boolean isPutOption()
		{
			return isOption && option.type == 'P';
		}

		@Override
		public String toString()
		{
			return !isOption ? String.format(STOCK_FORMAT, date, StringUtils.limit(action, 14), symbol, quantity, price, amount)
					: String.format(OPTION_FORMAT, date, StringUtils.limit(action, 14), symbol, quantity, price, amount, option.expiryDate,
							option.strike, option.type, days, annualReturn);
		}
	}

	private static class Option
	{
		public final String symbol;
		public final LocalDate expiryDate;
		public final double strike;
		public final char type;

		public Option(String optionText)
		{
			String[] tokens = optionText.split(" ");
			symbol = tokens[0];
			expiryDate = StringUtils.parseDate(tokens[1]);
			strike = Double.parseDouble(tokens[2]);
			type = tokens[3].charAt(0);
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