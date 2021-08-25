package com.roddyaj.invest.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.OptionalDouble;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVRecord;

import com.roddyaj.invest.model.settings.AccountSettings;
import com.roddyaj.invest.schwab.SchwabOpenOrdersSource;
import com.roddyaj.invest.schwab.SchwabPositionsSource;
import com.roddyaj.invest.schwab.SchwabTransactionsSource;
import com.roddyaj.invest.util.AppFileUtils;
import com.roddyaj.invest.util.AppFileUtils.FileType;
import com.roddyaj.invest.util.FileUtils;
import com.roddyaj.invest.util.StringUtils;

public class Account
{
	private final String name;
	private LocalDate date;
	private final AccountSettings accountSettings;
	private List<Position> positions;
	private List<Transaction> transactions;
	private List<Order> openOrders;

	public Account(String name, AccountSettings accountSettings)
	{
		this.name = AppFileUtils.getFullAccountName(name);
		this.accountSettings = accountSettings;
	}

	public String getName()
	{
		return name;
	}

	public LocalDate getDate()
	{
		return date;
	}

	public AccountSettings getAccountSettings()
	{
		return accountSettings;
	}

	public List<Position> getPositions()
	{
		if (positions == null)
		{
			Path positionsFile = AppFileUtils.getAccountFile(name, FileType.POSITIONS);
			if (positionsFile != null)
			{
				positions = FileUtils.readCsv(positionsFile, 2).stream().map(SchwabPositionsSource::convert).collect(Collectors.toList());

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
			Path transactionsFile = getAccountFile(getAccountSettings(), FileType.TRANSACTIONS);

			final LocalDate yearAgo = LocalDate.now().minusYears(1);
			Predicate<CSVRecord> filter = record -> {
				LocalDate date = StringUtils.parseDate(record.get(0));
				return date != null && date.isAfter(yearAgo);
			};
			transactions = transactionsFile != null ? FileUtils.readCsv(transactionsFile, 1).stream().filter(filter)
					.map(SchwabTransactionsSource::convert).collect(Collectors.toList()) : List.of();
		}
		return transactions;
	}

	public List<Order> getOpenOrders()
	{
		if (openOrders == null)
		{
			openOrders = List.of();

			Path ordersFile = getAccountFile(getAccountSettings(), FileType.ORDERS);
			if (ordersFile != null)
			{
				try
				{
					// Correct the file contents to be valid CSV
					List<String> lines = Files
							.lines(ordersFile).filter(line -> !line.isEmpty()).map(line -> line.replace("\" Shares", " Shares\"")
									.replace("\" Share", " Share\"").replace("\" Contracts", " Contracts\"").replace("\" Contract", " Contract\""))
							.collect(Collectors.toList());

					openOrders = FileUtils.readCsv(lines).stream().map(SchwabOpenOrdersSource::convert).filter(o -> o.getQuantity() != 0)
							.collect(Collectors.toList());
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		return openOrders;
	}

	public int getOpenOrderCount(String symbol, Action action)
	{
		return getOpenOrders().stream()
				.filter(o -> o.getSymbol().equals(symbol) && (action == Action.SELL ? o.getQuantity() < 0 : o.getQuantity() > 0))
				.mapToInt(o -> o.getQuantity()).sum();
	}

	public Double getPrice(String symbol)
	{
		OptionalDouble price = getPositions(symbol).mapToDouble(p -> p.isOption() ? p.getOption().getUnderlyingPrice() : p.getPrice()).findFirst();
		return price.isPresent() ? price.getAsDouble() : null;
	}

	public Double getDayChange(String symbol)
	{
		OptionalDouble dayChangePct = getPositions(symbol).filter(p -> !p.isOption()).mapToDouble(Position::getDayChangePct).findFirst();
		return dayChangePct.isPresent() ? dayChangePct.getAsDouble() : null;
	}

	public double getTotalValue()
	{
		return getPositions("Account Total").mapToDouble(Position::getMarketValue).findFirst().orElse(0);
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
		return getPositions().stream().filter(p -> p.getSymbol().equals(symbol));
	}

	private static Path getAccountFile(AccountSettings accountSettings, FileType type)
	{
		Path file = AppFileUtils.getAccountFile(accountSettings.getName(), type);
		if (file == null)
		{
			file = AppFileUtils.getAccountFile(accountSettings.getAccountNumber(), type);
			if (file == null)
			{
				String masked = "XXXX" + accountSettings.getAccountNumber().substring(4);
				file = AppFileUtils.getAccountFile(masked, type);
			}
		}
		return file;
	}
}
