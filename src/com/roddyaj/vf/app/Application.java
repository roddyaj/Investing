package com.roddyaj.vf.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.roddyaj.vf.api.alphavantage.AlphaVantageAPI;
import com.roddyaj.vf.api.schwab.SchwabScreenCsv;

public class Application
{
	private final Path inputFile;

	public Application(String[] args)
	{
		inputFile = args.length > 0 ? Paths.get(args[0]) : null;
	}

	public void run()
	{
		List<String> symbols = parseInputFile();
		if (!symbols.isEmpty())
//			processSymbols(symbols);
			processSymbols(symbols.subList(0, 1));
	}

	private List<String> parseInputFile()
	{
		List<String> symbols = List.of();
		if (inputFile == null)
			System.out.println("Error: No file specified");
		else if (!Files.exists(inputFile))
			System.out.println(String.format("Error: %s does not exist", inputFile.toString()));
		else
		{
			try
			{
				symbols = SchwabScreenCsv.parseSymbols(inputFile);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return symbols;
	}

	private void processSymbols(Collection<? extends String> symbols)
	{
		AlphaVantageAPI avAPI = new AlphaVantageAPI("demo");
		try
		{
			for (String symbol : symbols)
				processSymbol(avAPI, symbol);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void processSymbol(AlphaVantageAPI avAPI, String symbol) throws IOException
	{
		JSONObject json;

		json = avAPI.getOverview(symbol);
		double eps = Double.parseDouble((String)json.get("EPS"));

		json = avAPI.getBalanceSheet(symbol);
		JSONArray annualReports = (JSONArray)json.get("annualReports");
		long[] equities = new long[annualReports.size()];
		int i = 0;
		for (Object r : annualReports)
		{
			JSONObject report = (JSONObject)r;
			long equity = Long.parseLong((String)report.get("totalShareholderEquity"));
			equities[i++] = equity;
		}

		json = avAPI.getQuote(symbol);
		JSONObject quote = (JSONObject)json.get("Global Quote");
		double price = Double.parseDouble((String)quote.get("05. price"));

		double historicalPE = 1000; // TODO

		double growthRate = Math.pow((double)equities[0] / equities[equities.length - 1], 1. / (equities.length - 1));

		final double marr = 0.15;
		final double mosFactor = 0.5;

		double mosPrice = eps * Math.min(historicalPE, (growthRate - 1) * 100 * 2) * Math.pow(growthRate, 5) / Math.pow(1 + marr, 5) * mosFactor;

		boolean buy = price < mosPrice;
		System.out.println(symbol + " mosPrice: " + mosPrice + " price: " + price + "\tBuy? " + (buy ? "Yes!" : "No"));
	}
}
