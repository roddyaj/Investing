package com.roddyaj.invest.model;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roddyaj.invest.model.settings.Settings;
import com.roddyaj.invest.util.AppFileUtils;

public class Input
{
	private final Settings settings;
	private final Account account;
	private final Information information;
	private final List<Account> otherAccounts;

	public Input(String accountName)
	{
		settings = readSettings();
		account = newAccount(accountName);
		information = new Information();
		otherAccounts = Stream.of(settings.getAccounts()).filter(a -> !a.getName().equals(accountName)).map(a -> newAccount(a.getName()))
				.collect(Collectors.toList());
	}

	public Settings getSettings()
	{
		return settings;
	}

	public Account getAccount()
	{
		return account;
	}

	public Information getInformation()
	{
		return information;
	}

	public List<Account> getOtherAccounts()
	{
		return otherAccounts;
	}

	public Double getPrice(String symbol)
	{
		Double price = account.getPrice(symbol);
		if (price == null)
		{
			for (Account otherAccount : otherAccounts)
			{
				price = otherAccount.getPrice(symbol);
				if (price != null)
					break;
			}
		}
		return price;
	}

	public Double getDayChange(String symbol)
	{
		Double dayChange = account.getDayChange(symbol);
		if (dayChange == null)
		{
			for (Account otherAccount : otherAccounts)
			{
				dayChange = otherAccount.getDayChange(symbol);
				if (dayChange != null)
					break;
			}
		}
		return dayChange;
	}

	private Settings readSettings()
	{
		Settings settings = null;
		Path settingsFile = Paths.get(AppFileUtils.SETTINGS_DIR.toString(), "settings.json");
		try
		{
			settings = new ObjectMapper().readValue(settingsFile.toFile(), Settings.class);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return settings;
	}

	private Account newAccount(String accountName)
	{
		return new Account(accountName, settings.getAccount(accountName));
	}
}
