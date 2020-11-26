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

import com.roddyaj.vf.api.DataRequesterImpl;
import com.roddyaj.vf.api.schwab.SchwabScreenCsv;
import com.roddyaj.vf.model.Results;
import com.roddyaj.vf.model.SymbolData;
import com.roddyaj.vf.model.SymbolData.DataRequester;
import com.roddyaj.vf.model.SymbolResult;
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
				DataRequester requester = new DataRequesterImpl(settings);
				for (SymbolData stock : stocks)
					stock.setRequester(requester);

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

	private void evaluate(Collection<? extends SymbolData> stocks) throws IOException
	{
		Results results = new Results();
		List<Strategy> strategies = List.of(new AnalystTargetStrategy(), new Rule1Strategy());
		for (SymbolData stock : stocks)
			evaluate(stock, strategies, results);
		System.out.println(results);
	}

	private void evaluate(SymbolData stock, Collection<? extends Strategy> strategies, Results results) throws IOException
	{
		boolean allPass = true;
		SymbolResult result = new SymbolResult(stock);
		for (Strategy strategy : strategies)
		{
			allPass &= strategy.evaluate(stock, result);
			if (!allPass)
				break;
		}
		result.pass = allPass;

		results.addResult(result);
	}
}
