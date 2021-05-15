package com.roddyaj.invest.options;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
		List<CSVRecord> records = FileUtils.readCsv("Adam_Investing_Transactions");
		records = records.stream().filter(r -> r.size() > 1 && (r.get(1).startsWith("Sell to") || r.get(1).startsWith("Buy to")))
				.collect(Collectors.toList());
		writeCsv(records);
//		records.forEach(System.out::println);
	}

	private void writeCsv(Collection<? extends CSVRecord> records)
	{
		List<String> linesOut = new ArrayList<>();
		linesOut.add("Buy Date,Option,Days,Strike,Price,Premium,Annualized Return");
		for (CSVRecord csvRecord : records)
		{
			Record record = new Record(csvRecord);
			double annualReturn = record.premium / record.strike * (365.0 / record.days);
			linesOut.add(String.format("%s,%s,%d,%.2f,%.2f,%.2f,%.1f", record.buyDate, record.option, record.days, record.strike, record.price,
					record.premium, annualReturn));
		}
		FileUtils.writeLines("options_history.csv", linesOut);
	}

	private static class Record
	{
		private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

		public final LocalDate buyDate;
		public final String option;
		public final double price;
		public final double premium;
		public final LocalDate expiryDate;
		public final double strike;
		public final long days;

		public Record(CSVRecord record)
		{
			buyDate = LocalDate.parse(record.get(0), FORMATTER);
			option = record.get(2);
			price = Double.parseDouble(record.get(5).replace("$", ""));
			premium = Double.parseDouble(record.get(7).replace("$", ""));
			String[] tokens = option.split(" ");
			expiryDate = LocalDate.parse(tokens[1], FORMATTER);
			strike = Double.parseDouble(tokens[2]);
			days = ChronoUnit.DAYS.between(buyDate, expiryDate);
		}
	}
}