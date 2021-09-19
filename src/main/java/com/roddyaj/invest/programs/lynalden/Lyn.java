package com.roddyaj.invest.programs.lynalden;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVRecord;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roddyaj.invest.framework.Program;
import com.roddyaj.invest.model.RecommendedStocks;
import com.roddyaj.invest.util.AppFileUtils;
import com.roddyaj.invest.util.FileUtils;

public class Lyn implements Program
{
	// For running in IDE
	public static void main(String[] args)
	{
		new Lyn().run(new LinkedList<>());
	}

	@Override
	public void run(Queue<String> args)
	{
		RecommendedStocks stocks = getStocks();
		System.out.println(stocks.toYahooCsv());
		System.out.println();
		try
		{
			new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(System.out, stocks);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public RecommendedStocks getStocks()
	{
		List<String> tickers = new ArrayList<>();

		Path path = Paths.get(AppFileUtils.INPUT_DIR.toString(), "2 Model Portfolios - Newsletter Portfolio.csv");
		List<CSVRecord> records = FileUtils.readCsv(path, 0);

		final Pattern tickerPattern = Pattern.compile("(.+) \\(([\\w\\.]+?)\\)");
		final Pattern categoryPattern = Pattern.compile("(.+)");
		String category = null;
		for (int columnIndex : new int[] { 1, 4 })
		{
			for (CSVRecord record : records)
			{
				String column = record.get(columnIndex);
				Matcher matcher;
				if ((matcher = tickerPattern.matcher(column)).find())
				{
					String name = matcher.group(1);
					String ticker = matcher.group(2);
					if (!category.contains("ETF") && !name.contains("ETF") && !name.contains("Sprott"))
						tickers.add(ticker);
				}
				else if ((matcher = categoryPattern.matcher(column)).find())
					category = matcher.group(1);
			}
		}

		return new RecommendedStocks("Lyn Alden", tickers);
	}
}
