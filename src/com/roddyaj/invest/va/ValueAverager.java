package com.roddyaj.invest.va;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roddyaj.invest.model.Program;
import com.roddyaj.invest.va.api.schwab.SchwabAccountCsv;
import com.roddyaj.invest.va.model.Account;
import com.roddyaj.invest.va.model.config.AccountSettings;
import com.roddyaj.invest.va.model.config.Settings;

public class ValueAverager implements Program
{
	private final Path dataDir;

	public ValueAverager(Path dataDir)
	{
		this.dataDir = dataDir;
	}

	@Override
	public String getName()
	{
		return "ValueAverager";
	}

	@Override
	public void run(String[] args)
	{
		if (args.length == 0)
		{
			System.out.println("No account file specified");
			return;
		}

		Path accountFile = Paths.get(args[0]);
		if (!Files.exists(accountFile))
		{
			System.out.println("File does not exist: " + accountFile);
			return;
		}

		try
		{
			run(accountFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void run(Path accountFile) throws IOException
	{
		AccountSettings accountSettings = readSettings(accountFile);
		Account account = SchwabAccountCsv.parse(accountFile);

		if (accountSettings != null)
			new Algorithm(accountSettings, account).run();
	}

	private AccountSettings readSettings(Path accountFile) throws IOException
	{
		Path settingsFile = Paths.get(dataDir.toString(), "settings.json");
		Settings settings = new ObjectMapper().readValue(settingsFile.toFile(), Settings.class);
		String accountKey = accountFile.getFileName().toString().split("-", 2)[0];
		AccountSettings accountSettings = settings.getAccount(accountKey);
		if (accountSettings == null)
			System.out.println("No account settings found for '" + accountKey + "' in " + settingsFile);
		return accountSettings;
	}
}
