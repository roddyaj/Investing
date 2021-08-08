package com.roddyaj.invest.model;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVRecord;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roddyaj.invest.model.settings.AccountSettings;
import com.roddyaj.invest.model.settings.Settings;
import com.roddyaj.invest.util.AppFileUtils;
import com.roddyaj.invest.util.AppFileUtils.FileType;
import com.roddyaj.invest.util.FileUtils;
import com.roddyaj.invest.util.StringUtils;

public class Account
{
	private final String name;
	private LocalDate date;
	private Settings settings;
	private AccountSettings accountSettings;
	private List<Position> positions;
	private List<Transaction> transactions;

	public Account(String name)
	{
		this.name = AppFileUtils.getFullAccountName(name);
	}

	public String getName()
	{
		return name;
	}

	public LocalDate getDate()
	{
		return date;
	}

	public Settings getSettings()
	{
		if (settings == null)
		{
			Path settingsFile = Paths.get(AppFileUtils.SETTINGS_DIR.toString(), "settings.json");
			try
			{
				settings = new ObjectMapper().readValue(settingsFile.toFile(), Settings.class);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return settings;
	}

	public AccountSettings getAccountSettings()
	{
		if (accountSettings == null)
		{
			accountSettings = getSettings().getAccount(name);
		}
		return accountSettings;
	}

	public List<Position> getPositions()
	{
		if (positions == null)
		{
			Path positionsFile = AppFileUtils.getAccountFile(name, FileType.POSITIONS);
			if (positionsFile != null)
			{
				positions = FileUtils.readCsv(positionsFile, 2).stream().map(Position::new).collect(Collectors.toList());

				final Pattern datePattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");
				Matcher matcher = datePattern.matcher(positionsFile.getFileName().toString());
				if (matcher.find())
					date = LocalDate.parse(matcher.group(1));
			}
			else
			{
				positions = List.of();
			}
		}
		return positions;
	}

	public List<Transaction> getTransactions()
	{
		if (transactions == null)
		{
			Path transactionsFile = AppFileUtils.getAccountFile(name, FileType.TRANSACTIONS);

			if (transactionsFile == null)
			{
				AccountSettings accountSettings = getAccountSettings();
				if (accountSettings != null)
					transactionsFile = AppFileUtils.getAccountFile(accountSettings.getAlias(), FileType.TRANSACTIONS);
			}

			final LocalDate yearAgo = LocalDate.now().minusYears(1);
			Predicate<CSVRecord> filter = record -> {
				LocalDate date = StringUtils.parseDate(record.get(0));
				return date != null && date.isAfter(yearAgo);
			};
			transactions = transactionsFile != null
					? FileUtils.readCsv(transactionsFile, 1).stream().filter(filter).map(Transaction::new).collect(Collectors.toList())
					: List.of();
		}
		return transactions;
	}

	public double getPrice(String symbol)
	{
		return getPositions(symbol).mapToDouble(p -> p.isOption() ? p.option.getUnderlyingPrice() : p.price).findFirst().orElse(0);
	}

	public double getTotalValue()
	{
		return getPositions("Account Total").mapToDouble(p -> p.marketValue).findFirst().orElse(0);
	}

	public Position getPosition(String symbol)
	{
		return getPositions(symbol).findFirst().orElse(null);
	}

	public boolean hasSymbol(String symbol)
	{
		return getPositions(symbol).findAny().isPresent();
	}

	public Stream<Position> getPositions(String symbol)
	{
		return getPositions().stream().filter(p -> p.symbol.equals(symbol));
	}
}
