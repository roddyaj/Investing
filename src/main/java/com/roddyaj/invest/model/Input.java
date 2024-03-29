package com.roddyaj.invest.model;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roddyaj.invest.api.alphavantage.AlphaVantageAPI;
import com.roddyaj.invest.api.finnhub.FinnhubAPI;
import com.roddyaj.invest.api.model.QuoteRegistry;
import com.roddyaj.invest.api.schwab.SchwabDataSource;
import com.roddyaj.invest.model.settings.AccountSettings;
import com.roddyaj.invest.model.settings.Api;
import com.roddyaj.invest.model.settings.Settings;
import com.roddyaj.invest.util.AppFileUtils;

public class Input
{
	private final Settings settings;
	private final Account account;
	private final List<Account> otherAccounts;
	private final QuoteRegistry quoteRegistry = new QuoteRegistry();
	private final List<Message> messages = new ArrayList<>();

	public Input(String accountName, boolean offline)
	{
		settings = readSettings();
		account = newAccount(accountName);
		otherAccounts = Stream.of(settings.getAccounts()).filter(a -> !a.getName().equals(accountName)).map(a -> newAccount(a.getName())).toList();

		quoteRegistry.addProvider(account);
		for (Account otherAccount : otherAccounts)
			quoteRegistry.addProvider(otherAccount);
		if (!offline)
		{
			Api apiSettings;
			FinnhubAPI finnhub = new FinnhubAPI();
			apiSettings = settings.getApi(finnhub.getName());
			if (apiSettings != null)
			{
				finnhub.setApiKey(apiSettings.getApiKey());
				finnhub.setRequestLimitPerMinute(apiSettings.getRequestsPerMinute());
				quoteRegistry.addProvider(finnhub);
			}
			AlphaVantageAPI alphaVantage = new AlphaVantageAPI();
			apiSettings = settings.getApi(alphaVantage.getName());
			if (apiSettings != null)
			{
				alphaVantage.setApiKey(apiSettings.getApiKey());
				alphaVantage.setRequestLimitPerMinute(apiSettings.getRequestsPerMinute());
				quoteRegistry.addProvider(alphaVantage);
			}
		}

		messages.addAll(account.getMessages());
	}

	// Constructor for unit tests
	public Input(Account account)
	{
		this.settings = null;
		this.account = account;
		this.otherAccounts = null;
		quoteRegistry.addProvider(account);
	}

	public Settings getSettings()
	{
		return settings;
	}

	public Account getAccount()
	{
		return account;
	}

	public List<Account> getOtherAccounts()
	{
		return otherAccounts;
	}

	public QuoteRegistry getQuoteRegistry()
	{
		return quoteRegistry;
	}

	public List<Message> getMessages()
	{
		return messages;
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
		AccountSettings accountSettings = settings.getAccount(accountName);
		return new Account(accountSettings, new SchwabDataSource(accountSettings));
	}
}
