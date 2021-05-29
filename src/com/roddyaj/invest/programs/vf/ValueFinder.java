package com.roddyaj.invest.programs.vf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.roddyaj.invest.framework.Program;
import com.roddyaj.invest.programs.vf.api.DataRequesterImpl;
import com.roddyaj.invest.programs.vf.api.misc.SymbolReader;
import com.roddyaj.invest.programs.vf.api.schwab.SchwabScreenCsv;
import com.roddyaj.invest.programs.vf.model.Results;
import com.roddyaj.invest.programs.vf.model.SymbolData;
import com.roddyaj.invest.programs.vf.model.SymbolData.DataRequester;
import com.roddyaj.invest.programs.vf.report.Report;
import com.roddyaj.invest.programs.vf.strategy.Strategies;

public class ValueFinder implements Program
{
	private final Path dataDir;

	public ValueFinder(Path dataDir)
	{
		this.dataDir = dataDir;
	}

	@Override
	public String getName()
	{
		return "ValueFinder";
	}

	@Override
	public void run(String... args)
	{
		try
		{
			JSONObject settings = readSettings();

			List<SymbolData> stocks = getStocksToCheck(args);
			if (!stocks.isEmpty())
			{
				DataRequester requester = new DataRequesterImpl(settings, dataDir);
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
		Path settingsFile = Paths.get(dataDir.toString(), "settings.json");
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
		}
		else
			System.out.println("Usage: TODO");
		return stocks;
	}
}
