package com.roddyaj.vf.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.roddyaj.vf.api.alphavantage.AlphaVantageAPI;
import com.roddyaj.vf.api.schwab.SchwabScreenCsv;
import com.roddyaj.vf.model.Pair;
import com.roddyaj.vf.model.SymbolData;
import com.roddyaj.vf.strategy.AnalystTargetStrategy;
import com.roddyaj.vf.strategy.Rule1Strategy;
import com.roddyaj.vf.strategy.Strategy;

public class Application
{
	private final String[] args;

	public Application(String[] args)
	{
		this.args = args;
	}

	public void run()
	{
		try
		{
			List<SymbolData> stocks = getStocksToCheck(args);
			if (populateData(stocks, args))
				evaluate(stocks);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private List<SymbolData> getStocksToCheck(String[] args) throws IOException
	{
		List<SymbolData> stocks = List.of();
		Path inputFile = args.length > 0 ? Paths.get(args[0]) : null;
		if (inputFile == null)
			System.out.println("Error: No file specified");
		else if (!Files.exists(inputFile))
			System.out.println(String.format("Error: %s does not exist", inputFile.toString()));
		else
			stocks = SchwabScreenCsv.parseSymbols(inputFile).stream().map(SymbolData::new).collect(Collectors.toList());
		return stocks;
	}

	private boolean populateData(Collection<? extends SymbolData> stocks, String[] args) throws IOException
	{
		boolean populated = false;
		String apiKey = args.length > 1 ? args[1] : null;
		if (apiKey == null)
			System.out.println("Error: No API key specified");
		else
		{
			AlphaVantageAPI avAPI = new AlphaVantageAPI(apiKey);
			for (SymbolData stock : stocks)
				populateData(stock, avAPI);
			populated = true;
		}
		return populated;
	}

	private void populateData(SymbolData stock, AlphaVantageAPI avAPI) throws IOException
	{
		try
		{
			avAPI.requestData(stock);
		}
		catch (RuntimeException e)
		{
			e.printStackTrace();
		}
	}

	private void evaluate(Collection<? extends SymbolData> stocks)
	{
		List<Strategy> strategies = List.of(new Rule1Strategy(), new AnalystTargetStrategy());
		for (SymbolData stock : stocks)
			evaluate(stock, strategies);
	}

	private void evaluate(SymbolData stock, Collection<? extends Strategy> strategies)
	{
		boolean allPass = true;
		String name = stock.name.substring(0, Math.min(30, stock.name.length()));
		StringBuilder message = new StringBuilder(String.format("%-5s %-30s %7.2f", stock.symbol, name, stock.price));
		for (Strategy strategy : strategies)
		{
			Pair<Boolean, String> result = strategy.evaluate(stock);
			boolean pass = result.first.booleanValue();
			allPass &= pass;
			message.append("   ").append(result.second).append(String.format(" %-4s", pass ? "Yes!" : "No"));
		}

		if (allPass)
			System.out.println(message);
	}
}
