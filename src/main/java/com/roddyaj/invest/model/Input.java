package com.roddyaj.invest.model;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roddyaj.invest.api.alphavantage.AlphaVantageAPI;
import com.roddyaj.invest.api.finnhub.FinnhubAPI;
import com.roddyaj.invest.api.model.QuoteRegistry;
import com.roddyaj.invest.model.settings.Api;
import com.roddyaj.invest.model.settings.Settings;
import com.roddyaj.invest.util.AppFileUtils;

public class Input
{
	private final Settings settings;
	private final Account account;
	private final Information information = new Information();
	private final List<Account> otherAccounts;
	private final QuoteRegistry quoteRegistry = new QuoteRegistry();

	public Input(String accountName)
	{
		settings = readSettings();
		account = newAccount(accountName);
		otherAccounts = Stream.of(settings.getAccounts()).filter(a -> !a.getName().equals(accountName)).map(a -> newAccount(a.getName()))
				.collect(Collectors.toList());

		quoteRegistry.addProvider(account);
		for (Account otherAccount : otherAccounts)
			quoteRegistry.addProvider(otherAccount);
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

	public QuoteRegistry getQuoteRegistry()
	{
		return quoteRegistry;
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
