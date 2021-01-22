package com.roddyaj.invest.va;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

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
		Path accountFile = Paths.get(args[0]);
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
		Algorithm algorithm = new Algorithm();

		accountSettings.getRealPositions()
			.map(position -> algorithm.evaluate(position.getSymbol(), accountSettings, account))
			.filter(Objects::nonNull)
			.sorted((o1, o2) -> Double.compare(o2.getAmount(), o1.getAmount()))
			.forEach(System.out::println);
	}

	private AccountSettings readSettings(Path accountFile) throws IOException
	{
		Path settingsFile = Paths.get(dataDir.toString(), "settings.json");
		Settings settings = new ObjectMapper().readValue(settingsFile.toFile(), Settings.class);
		String accountKey = accountFile.getFileName().toString().split("-", 2)[0];
		return settings.getAccount(accountKey);
	}
}
