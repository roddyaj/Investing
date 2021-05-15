package com.roddyaj.invest.options;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

		analyzePuts(optionTransactions);

//		writeHistoryCsv(optionTransactions);
	}

	private void analyzePuts(Collection<? extends Transaction> transactions)
	{
		List<Pair<String, Double>> candidates = new ArrayList<>();

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

			if (quantity <= 0)
			{
				double averageReturn = allPuts.stream().filter(t -> t.symbol.equals(symbol) && t.action.equals("Sell to Open"))
						.collect(Collectors.averagingDouble(t -> t.annualReturn));
				candidates.add(new Pair<>(symbol, averageReturn));
			}
		}

		System.out.println("Puts to Sell:");
		System.out.println("Symb Ret");
		candidates.stream().sorted((o1, o2) -> o2.right.compareTo(o1.right))
				.forEach(p -> System.out.println(String.format("%-4s %3.0f", p.left, p.right)));
	}

	private void writeHistoryCsv(Collection<? extends Transaction> transactions)
	{
		List<String> lines = new ArrayList<>();
		lines.add(Transaction.CSV_HEADER);
		transactions.stream().map(Transaction::toString).forEach(lines::add);
		FileUtils.writeLines("options_history.csv", lines);
	}

	private static class Transaction
	{
		private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

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
			price = parseDollar(record.get(5));
			amount = parseDollar(record.get(7));

			String[] tokens = option.split(" ");
			symbol = tokens[0];
			expiryDate = parseDate(tokens[1]);
			strike = Double.parseDouble(tokens[2]);
			type = tokens[3].charAt(0);

			days = (int)ChronoUnit.DAYS.between(date, expiryDate);
			annualReturn = amount / strike * (365.0 / days);
		}

		public static final String CSV_HEADER = "Date,Action,Description,Days,Strike,Price,Premium,Annualized Return";

		@Override
		public String toString()
		{
			return String.format("%s,%s,%s,%d,%.2f,%.2f,%.2f,%.1f", date, action, option, days, strike, price, amount, annualReturn);
		}

		private LocalDate parseDate(String s)
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

		private double parseDollar(String s)
		{
			return !s.isBlank() ? Double.parseDouble(s.replace("$", "")) : 0;
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