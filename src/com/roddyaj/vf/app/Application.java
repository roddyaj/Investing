package com.roddyaj.vf.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import com.roddyaj.vf.api.alphavantage.AlphaVantageAPI;
import com.roddyaj.vf.api.schwab.SchwabScreenCsv;
import com.roddyaj.vf.model.Pair;
import com.roddyaj.vf.model.SymbolData;
import com.roddyaj.vf.strategy.AnalystTargetStrategy;
import com.roddyaj.vf.strategy.Rule1Strategy;
import com.roddyaj.vf.strategy.Strategy;

public class Application
{
	private final Path inputFile;

	private final String apiKey;

	public Application(String[] args)
	{
		inputFile = args.length > 0 ? Paths.get(args[0]) : null;
		apiKey = args.length > 1 ? args[1] : null;
	}

	public void run()
	{
		if (argsValid())
		{
			List<String> symbols = parseInputFile();
			if (!symbols.isEmpty())
				processSymbols(symbols);
		}
	}

	private boolean argsValid()
	{
		boolean valid = false;
		if (inputFile == null)
			System.out.println("Error: No file specified");
		else if (!Files.exists(inputFile))
			System.out.println(String.format("Error: %s does not exist", inputFile.toString()));
		else if (apiKey == null)
			System.out.println("Error: No API key specified");
		else
			valid = true;
		return valid;
	}

	private List<String> parseInputFile()
	{
		List<String> symbols = List.of();
		try
		{
			symbols = SchwabScreenCsv.parseSymbols(inputFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return symbols;
	}

	private void processSymbols(Collection<? extends String> symbols)
	{
		AlphaVantageAPI avAPI = new AlphaVantageAPI(apiKey);
		try
		{
			for (String symbol : symbols)
			{
				try
				{
					SymbolData data = avAPI.requestData(symbol);
					evaluate(symbol, data);
				}
				catch (RuntimeException e)
				{
					e.printStackTrace();
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void evaluate(String symbol, SymbolData data)
	{
		List<Strategy> strategies = List.of(new Rule1Strategy(), new AnalystTargetStrategy());

		boolean allPass = true;
		StringBuilder message = new StringBuilder();
		message.append(String.format("%-5s %7.2f", symbol, data.price));
		for (Strategy strategy : strategies)
		{
			Pair<Boolean, String> result = strategy.evaluate(data);
			boolean pass = result.first.booleanValue();
			allPass &= pass;
			message.append("   ").append(result.second).append(String.format(" %-4s", pass ? "Yes!" : "No"));
		}

		if (allPass)
			System.out.println(message);
	}
}
