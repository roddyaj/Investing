package com.roddyaj.invest.api.schwab;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVRecord;

import com.roddyaj.invest.model.Action;
import com.roddyaj.invest.model.Option;
import com.roddyaj.invest.model.Transaction;
import com.roddyaj.invest.model.settings.AccountSettings;
import com.roddyaj.invest.util.AppFileUtils;
import com.roddyaj.invest.util.FileUtils;
import com.roddyaj.invest.util.StringUtils;

public class SchwabTransactionsSource
{
	private static final String DATE = "Date";
	private static final String ACTION = "Action";
	private static final String SYMBOL = "Symbol";
//	private static final String DESCRIPTION = "Description";
	private static final String QUANTITY = "Quantity";
	private static final String PRICE = "Price";
//	private static final String FEES_AND_COMM = "Fees & Comm";
	private static final String AMOUNT = "Amount";

	private static final Pattern FILE_PATTERN = Pattern.compile("(.+?)_Transactions_([-\\d]+).CSV");

	private final AccountSettings accountSettings;

	private List<Transaction> transactions;

	public SchwabTransactionsSource(AccountSettings accountSettings)
	{
		this.accountSettings = accountSettings;
	}

	public List<Transaction> getTransactions()
	{
		if (transactions == null)
		{
			Path transactionsFile = getAccountFile();
			if (transactionsFile != null)
			{
				final LocalDate yearAgo = LocalDate.now().minusYears(1);
				Predicate<CSVRecord> filter = record -> {
					LocalDate date = StringUtils.parseDate(record.get(0));
					return date != null && date.isAfter(yearAgo);
				};
				transactions = FileUtils.readCsv(transactionsFile, 1).stream().filter(filter).map(SchwabTransactionsSource::convert)
						.collect(Collectors.toList());
			}
			else
			{
				transactions = List.of();
			}
		}
		return transactions;
	}

	private Path getAccountFile()
	{
		final String pattern = "_Transactions_.*\\.CSV";
		Path file = AppFileUtils.getAccountFile(accountSettings.getName() + pattern, (p1, p2) -> getTime(p2).compareTo(getTime(p1)));
		if (file == null)
		{
			String masked = "XXXX" + accountSettings.getAccountNumber().substring(4);
			file = AppFileUtils.getAccountFile(masked + pattern, (p1, p2) -> getTime(p2).compareTo(getTime(p1)));
		}
		return file;
	}

	private static String getTime(Path path)
	{
		String timeString = null;
		Matcher m = FILE_PATTERN.matcher(path.getFileName().toString());
		if (m.find())
			timeString = m.group(2).replace("-", "");
		return timeString;
	}

	private static Transaction convert(CSVRecord record)
	{
		LocalDate date = StringUtils.parseDate(record.get(DATE));
		Action action = parseAction(record.get(ACTION));
		String symbolOrOption = record.get(SYMBOL);
		int quantity = (int)Math.round(StringUtils.parseDouble(record.get(QUANTITY)));
		double price = StringUtils.parsePrice(record.get(PRICE));
		double amount = StringUtils.parsePrice(record.get(AMOUNT));

		Option option = null;
		String symbol;
		boolean isOption = symbolOrOption.contains(" ");
		if (isOption)
		{
			option = SchwabUtils.parseOptionText(symbolOrOption);
			symbol = option.getSymbol();
		}
		else
		{
			symbol = symbolOrOption;
		}

		return new Transaction(date, action, symbol, quantity, price, amount, option);
	}

	private static Action parseAction(String s)
	{
		return switch (s)
		{
			case "Buy" -> Action.BUY;
			case "Sell" -> Action.SELL;
			case "Sell to Open" -> Action.SELL_TO_OPEN;
			case "Buy to Close" -> Action.BUY_TO_CLOSE;
			case "Buy to Open" -> Action.BUY_TO_OPEN;
			case "Sell to Close" -> Action.SELL_TO_CLOSE;
			case "Journal", "MoneyLink Deposit", "MoneyLink Transfer", "Funds Received" -> Action.TRANSFER;
			default -> null;
		};
	}
}
