package com.roddyaj.invest.va;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.roddyaj.invest.model.Program;
import com.roddyaj.invest.va.api.schwab.SchwabAccountCsv;

public class ValueAverager implements Program
{
	private final Path dataDir;

	private static final Set<LocalDate> HOLIDAYS = new HashSet<>();
	static
	{
		HOLIDAYS.add(LocalDate.of(2021, 1, 1));
		HOLIDAYS.add(LocalDate.of(2021, 1, 18));
		HOLIDAYS.add(LocalDate.of(2021, 2, 15));
		HOLIDAYS.add(LocalDate.of(2021, 4, 2));
		HOLIDAYS.add(LocalDate.of(2021, 5, 31));
		HOLIDAYS.add(LocalDate.of(2021, 7, 5));
		HOLIDAYS.add(LocalDate.of(2021, 9, 6));
		HOLIDAYS.add(LocalDate.of(2021, 11, 25));
		HOLIDAYS.add(LocalDate.of(2021, 12, 24));
	}

	public ValueAverager(Path dataDir)
	{
		this.dataDir = dataDir;
	}

	@Override
	public String getName()
	{
		return "ValueAverager";
	}

	@Override
	public void run(String[] args)
	{
		Path accountFile = Paths.get(args[0]);
		try
		{
			run(accountFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void run(Path accountFile) throws IOException
	{
		JSONObject settings = readSettings();
		String accountKey = accountFile.getFileName().toString().split("-", 2)[0];
		JSONObject config = (JSONObject)settings.get(accountKey);

		Map<String, Map<String, String>> accountMap = SchwabAccountCsv.parse(accountFile);

		TemporalInfo temporalInfo = new TemporalInfo(LocalDate.of(2021, 1, 1).minusDays(4), LocalDate.of(2021, 12, 31));

		for (Object symbol : config.keySet())
			evaluate((String)symbol, config, accountMap, temporalInfo);
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

	private void evaluate(String symbol, JSONObject config, Map<String, Map<String, String>> accountMap, TemporalInfo temporalInfo)
	{
		JSONObject symbolConfig = (JSONObject)config.get(symbol);
		double day0Value = ((Number)symbolConfig.get("day0Value")).doubleValue();
		double dailyContrib = ((Number)symbolConfig.get("dailyContrib")).doubleValue();
		double growthRate = ((Number)symbolConfig.get("growthRate")).doubleValue();

		double expectedAmount = day0Value;
		for (LocalDate date = temporalInfo.startDate; date.compareTo(temporalInfo.today) <= 0; date = date.plusDays(1))
		{
			if (isTradingDay(date))
				expectedAmount = expectedAmount * (1 + growthRate / temporalInfo.numTradingDays) + dailyContrib;
		}

		Map<String, String> symbolMap = accountMap.get(symbol);
		double actualAmount = SchwabAccountCsv.parsePrice(symbolMap.get("Market Value"));
		double sharePrice = SchwabAccountCsv.parsePrice(symbolMap.get("Price"));
		double delta = expectedAmount - actualAmount;
		long sharesToBuy = Math.round(delta / sharePrice);

		System.out.println(String.format("%s: Buy %d of %s", temporalInfo.today.toString(), sharesToBuy, symbol));
	}

	private static boolean isTradingDay(LocalDate date)
	{
		DayOfWeek day = date.getDayOfWeek();
		return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY && !HOLIDAYS.contains(date);
	}

	private static class TemporalInfo
	{
		public final LocalDate startDate;

		public final LocalDate today = LocalDate.now();

		public final int numTradingDays;

		public TemporalInfo(LocalDate startDate, LocalDate endDate)
		{
			this.startDate = startDate;
			int numTradingDays = 0;
			for (LocalDate date = startDate; date.compareTo(endDate) <= 0; date = date.plusDays(1))
			{
				if (isTradingDay(date))
					numTradingDays++;
			}
			this.numTradingDays = numTradingDays;
		}
	}
}
