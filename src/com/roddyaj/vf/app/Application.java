package com.roddyaj.vf.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

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
			{
				JSONObject json = avAPI.getOverview(symbol);
				String epsString = (String)json.get("EPS");
				double eps = Double.parseDouble(epsString);
				System.out.println(symbol + ": " + eps);
			}
		}
		catch (IOException | InterruptedException | ParseException e)
		{
			e.printStackTrace();
		}
	}
}
