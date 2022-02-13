package com.roddyaj.invest.api.schwab;

import java.nio.file.Path;
import java.util.List;

import com.roddyaj.invest.model.Action;
import com.roddyaj.invest.model.Option;
import com.roddyaj.invest.model.Transaction;
import com.roddyaj.invest.model.settings.AccountSettings;
import com.roddyaj.invest.util.AppFileUtils;

public class SchwabTransactionsSource
{
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
				transactions = new SchwabTransactionsFile(transactionsFile).getTransactions().stream().map(SchwabTransactionsSource::convert)
						.toList();
			else
				transactions = List.of();
		}
		return transactions;
	}

	private Path getAccountFile()
	{
		final String pattern = "_Transactions_.*\\.CSV";
		Path file = AppFileUtils.getAccountFile(accountSettings.getName() + pattern,
				(p1, p2) -> SchwabTransactionsFile.getTime(p2).compareTo(SchwabTransactionsFile.getTime(p1)));
		if (file == null)
		{
			String masked = "XXXX" + accountSettings.getAccountNumber().substring(4);
			file = AppFileUtils.getAccountFile(masked + pattern,
					(p1, p2) -> SchwabTransactionsFile.getTime(p2).compareTo(SchwabTransactionsFile.getTime(p1)));
		}
		return file;
	}

	private static Transaction convert(SchwabTransaction transaction)
	{
		Option option = null;
		String symbol;
		boolean isOption = transaction.symbol() != null && transaction.symbol().contains(" ");
		if (isOption)
		{
			option = SchwabUtils.parseOptionText(transaction.symbol());
			symbol = option.getSymbol();
		}
		else
		{
			symbol = transaction.symbol();
		}

		// @formatter:off
		return new Transaction(
			transaction.date(),
			parseAction(transaction.action()),
			symbol != null ? symbol : "",
			transaction.quantity() != null ? (int)Math.round(transaction.quantity().doubleValue()) : 0,
			transaction.price() != null ? transaction.price().doubleValue() : 0,
			transaction.amount() != null ? transaction.amount().doubleValue() : 0,
			option);
		// @formatter:on
	}

	private static Action parseAction(String s)
	{
		Action action = switch (s)
		{
			case "Buy" -> Action.BUY;
			case "Sell" -> Action.SELL;
			case "Sell to Open" -> Action.SELL_TO_OPEN;
			case "Buy to Close" -> Action.BUY_TO_CLOSE;
			case "Buy to Open" -> Action.BUY_TO_OPEN;
			case "Sell to Close" -> Action.SELL_TO_CLOSE;
			case "Journal", "MoneyLink Deposit", "MoneyLink Transfer", "Funds Received", "Bank Transfer" -> Action.TRANSFER;
			default -> null;
		};
		if (action == null)
		{
			if (s.contains(" Div"))
				action = Action.DIVIDEND;
		}
		return action;
	}
}
