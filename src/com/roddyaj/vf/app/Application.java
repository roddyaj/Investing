package com.roddyaj.vf.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import com.roddyaj.vf.api.alphavantage.AlphaVantageAPI;
import com.roddyaj.vf.api.schwab.SchwabScreenCsv;
import com.roddyaj.vf.model.SymbolData;

public class Application
{
	private final Path inputFile;

	private final String apiKey;

	private final boolean sleep = true;

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
			int index = 0;
			for (String symbol : symbols)
			{
				try
				{
					SymbolData data = avAPI.requestData(symbol);
					calculate(symbol, data, index++);

					if (sleep)
						Thread.sleep(61_000);
				}
				catch (RuntimeException e)
				{
					e.printStackTrace();
				}
			}
		}
		catch (IOException | InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	private void calculate(String symbol, SymbolData data, int index)
	{
		double historicalPE = 1000; // TODO

		int years = data.shareholderEquity.size() - 1;
		double recentEquity = data.shareholderEquity.get(0).second.doubleValue();
		double olderEquity = data.shareholderEquity.get(data.shareholderEquity.size() - 1).second.doubleValue();
		double growthRate = Math.pow(recentEquity / olderEquity, 1. / years);
//		System.out.println(recentEquity + " " + olderEquity + " " + years + " " + growthRate);

		final double marr = 0.15;
		final double mosFactor = 0.5;

		double mosPrice = data.eps * Math.min(historicalPE, (growthRate - 1) * 100 * 2) * Math.pow(growthRate, 5) / Math.pow(1 + marr, 5) * mosFactor;

		boolean buy = data.price < mosPrice;
		boolean analystBuy = data.price < data.analystTargetPrice;

		boolean logIt = buy && analystBuy;
		if (logIt)
		{
			StringBuilder message = new StringBuilder();
			message.append(String.format("%3d. %-5s %7.2f", index, symbol, data.price)).append("   ");
			message.append(String.format("Rule 1: %7.2f %-4s", mosPrice, (buy ? "Yes!" : "No"))).append("   ");
			message.append(String.format("Analyst: %7.2f %-4s", data.analystTargetPrice, (analystBuy ? "Yes!" : "No")));
			System.out.println(message);
		}
	}
}
