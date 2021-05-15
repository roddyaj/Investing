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
	@Override
	public void run(String[] args)
	{
		List<CSVRecord> records = FileUtils.readCsv("Adam_Investing_Transactions");
		records = records.stream().filter(r -> r.size() > 1 && (r.get(1).startsWith("Sell to") || r.get(1).startsWith("Buy to")))
				.collect(Collectors.toList());
		writeCsv(records);
	}

	private void writeCsv(Collection<? extends CSVRecord> records)
	{
		List<String> linesOut = new ArrayList<>();
		linesOut.add("Buy Date,Option,Days,Strike,Price,Premium,Annualized Return");
		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
		for (CSVRecord record : records)
		{
			LocalDate buyDate = LocalDate.parse(record.get(0), formatter);
			String option = record.get(2);
			double price = Double.parseDouble(record.get(5).replace("$", ""));
			double premium = Double.parseDouble(record.get(7).replace("$", ""));
			String[] tokens = option.split(" ");
			LocalDate expiryDate = LocalDate.parse(tokens[1], formatter);
			double strike = Double.parseDouble(tokens[2]);
			long days = ChronoUnit.DAYS.between(buyDate, expiryDate);
			double annualReturn = premium / strike * (365.0 / days);
			linesOut.add(String.format("%s,%s,%d,%.2f,%.2f,%.2f,%.1f", buyDate, option, days, strike, price, premium, annualReturn));
		}
		FileUtils.writeLines("options_history.csv", linesOut);
	}
}