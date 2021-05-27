package com.roddyaj.invest.programs.va;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roddyaj.invest.framework.Program;
import com.roddyaj.invest.programs.va.api.schwab.SchwabAccountCsv;
import com.roddyaj.invest.programs.va.model.Account;
import com.roddyaj.invest.programs.va.model.config.AccountSettings;
import com.roddyaj.invest.programs.va.model.config.Settings;

public class ValueAverager implements Program
{
	private final Path dataDir;

	public ValueAverager(Path dataDir)
	{
		this.dataDir = dataDir;
	}

	@Override
	public void run(String[] args)
	{
		if (args.length == 0)
		{
			System.out.println("No account file specified");
			return;
		}

		try
		{
			Path settingsFile = Paths.get(dataDir.toString(), "settings.json");
			Settings settings = new ObjectMapper().readValue(settingsFile.toFile(), Settings.class);

			Path accountFile = getAccountFile(args, settings);
			if (accountFile == null)
			{
				System.out.println("Account file not found");
				return;
			}

			int reportLevel = 0;
			if (args.length > 1)
			{
				if (args[1].equals("-print"))
					reportLevel = 1;
				else if (args[1].equals("-print2"))
					reportLevel = 2;
			}

			run(settings, accountFile, reportLevel);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	// TODO replace with FileUtils method(s)
	private Path getAccountFile(String[] args, Settings settings) throws IOException
	{
		Path argFile = Paths.get(args[0]);
		Path defaultDataDir = Paths.get(settings.getDefaultDataDir());
		Path accountFile = null;
		if (Files.exists(argFile))
		{
			accountFile = argFile;
		}
		else if (Files.exists(defaultDataDir))
		{
			accountFile = Files.list(defaultDataDir).filter(p -> p.getFileName().toString().startsWith(argFile.toString()))
					.sorted((o1, o2) -> o2.getFileName().compareTo(o1.getFileName())).findFirst().orElse(null);
		}
		return accountFile;
	}

	private void run(Settings settings, Path accountFile, int reportLevel) throws IOException
	{
		String accountKey = accountFile.getFileName().toString().split("-", 2)[0];
		AccountSettings accountSettings = settings.getAccount(accountKey);

		if (accountSettings != null)
		{
			Account account = SchwabAccountCsv.parse(accountFile);
			new Algorithm(accountSettings, account, settings).run(reportLevel);
		}
		else
		{
			System.out.println("No account settings found for '" + accountKey + "' in settings.");
		}
	}
}
