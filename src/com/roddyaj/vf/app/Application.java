package com.roddyaj.vf.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.roddyaj.vf.api.alphavantage.AlphaVantageAPI;
import com.roddyaj.vf.api.schwab.SchwabScreenCsv;
import com.roddyaj.vf.model.SymbolData;
import com.roddyaj.vf.strategy.AnalystTargetStrategy;
import com.roddyaj.vf.strategy.Rule1Strategy;
import com.roddyaj.vf.strategy.Strategy;

public class Application
{
	public void run(String[] args)
	{
		try
		{
			JSONObject settings = readSettings();

			List<SymbolData> stocks = getStocksToCheck(args);
			if (!stocks.isEmpty())
			{
				if (populateData(stocks, settings))
					evaluate(stocks);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private JSONObject readSettings() throws IOException
	{
		Path settingsFile = Paths.get(System.getProperty("user.home"), ".vf", "settings.json");
		String json = Files.readString(settingsFile);
		JSONParser parser = new JSONParser();
		try
		{
			return (JSONObject)parser.parse(json);
		}
		catch (ParseException e)
		{
			throw new IOException(e);
		}
	}

	private List<SymbolData> getStocksToCheck(String[] args) throws IOException
	{
		List<SymbolData> stocks = List.of();
		if (args.length > 0)
		{
			Path inputFile = Paths.get(args[0]);
			if (Files.exists(inputFile))
				stocks = SchwabScreenCsv.parseSymbols(inputFile);
			else
				stocks = Arrays.stream(args[0].split(",")).map(SymbolData::new).collect(Collectors.toList());
		}
		else
			System.out.println("Usage: TODO");
		return stocks;
	}

	private boolean populateData(Collection<? extends SymbolData> stocks, JSONObject settings) throws IOException
	{
		boolean populated = false;
		String apiKey = (String)settings.get("alphavantage.apiKey");
		if (apiKey != null)
		{
			AlphaVantageAPI avAPI = new AlphaVantageAPI(apiKey);
			for (SymbolData stock : stocks)
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
			populated = true;
		}
		else
			System.out.println("Error: No API key specified");
		return populated;
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
		for (Strategy strategy : strategies)
			allPass &= strategy.evaluate(stock);

		if (allPass)
		{
			String name = stock.name.substring(0, Math.min(30, stock.name.length()));
			StringBuilder message = new StringBuilder(String.format("%-5s %-30s %7.2f", stock.symbol, name, stock.price));
			message.append("  ").append(String.format(" %-4s", allPass ? "Yes!" : "No"));
			System.out.println(message);
		}
	}
}
