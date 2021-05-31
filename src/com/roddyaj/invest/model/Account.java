package com.roddyaj.invest.model;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roddyaj.invest.model.settings.AccountSettings;
import com.roddyaj.invest.model.settings.Settings;
import com.roddyaj.invest.util.AppFileUtils;
import com.roddyaj.invest.util.AppFileUtils.FileType;
import com.roddyaj.invest.util.FileUtils;

public class Account
{
	private final String name;
	private AccountSettings accountSettings;
	private List<Position> positions;
	private List<Transaction> transactions;

	public Account(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public AccountSettings getAccountSettings()
	{
		if (accountSettings == null)
		{
			Path settingsFile = Paths.get(AppFileUtils.SETTINGS_DIR.toString(), "settings.json");
			try
			{
				Settings settings = new ObjectMapper().readValue(settingsFile.toFile(), Settings.class);
				accountSettings = settings.getAccount(name);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return accountSettings;
	}

	public List<Position> getPositions()
	{
		if (positions == null)
		{
			Path positionsFile = AppFileUtils.getAccountFile(name, FileType.POSITIONS);
			positions = positionsFile != null
					? FileUtils.readCsv(positionsFile).stream().filter(r -> r.getRecordNumber() > 2).map(Position::new).collect(Collectors.toList())
					: List.of();
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
				transactionsFile = AppFileUtils.getAccountFile(accountSettings.getAlias(), FileType.TRANSACTIONS);
			}

			transactions = transactionsFile != null ? FileUtils.readCsv(transactionsFile).stream().filter(r -> r.getRecordNumber() > 2)
					.map(Transaction::new).collect(Collectors.toList()) : List.of();
		}
		return transactions;
	}
}
