package com.roddyaj.vf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.roddyaj.vf.api.DataRequesterImpl;
import com.roddyaj.vf.api.misc.SymbolReader;
import com.roddyaj.vf.api.schwab.SchwabScreenCsv;
import com.roddyaj.vf.model.Results;
import com.roddyaj.vf.model.SymbolData;
import com.roddyaj.vf.model.SymbolData.DataRequester;
import com.roddyaj.vf.report.Report;
import com.roddyaj.vf.strategy.Strategies;

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

				Strategies strategies = new Strategies(args);
				Results results = strategies.run(stocks);
				Report.print(results);
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
			String firstArg = args[0];
			if (firstArg.equals("sp500"))
				stocks = SymbolReader.readSymbols(Paths.get("data/sp500_symbols.txt"));
			else if (Files.exists(Paths.get(firstArg)))
				stocks = SchwabScreenCsv.parseSymbols(Paths.get(firstArg));
			else
				stocks = SymbolData.fromSymbols(Arrays.asList(firstArg.split(",")));
//			stocks = stocks.subList(230, 230 + 70);
		}
		else
			System.out.println("Usage: TODO");
		return stocks;
	}
}
