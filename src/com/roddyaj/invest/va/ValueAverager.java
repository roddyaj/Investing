package com.roddyaj.invest.va;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
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

	private static final int ANNUAL_TRADING_DAYS = 252;

	private static final Map<String, Integer> PERIODS = new HashMap<>();
	static
	{
		PERIODS.put("day", 1);
		PERIODS.put("week", 5);
		PERIODS.put("month", 21);
		PERIODS.put("year", ANNUAL_TRADING_DAYS);
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

		for (Object key : config.keySet())
		{
			String symbol = (String)key;
			if (!symbol.startsWith("_"))
				evaluate(symbol, config, accountMap);
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

	private void evaluate(String symbol, JSONObject config, Map<String, Map<String, String>> accountMap)
	{
		LocalDate day0 = LocalDate.parse((String)getValue(config, symbol, "day0"));
		double day0Value = getDouble(config, symbol, "day0Value");
		double contrib = getDouble(config, symbol, "contrib");
		double annualGrowth = getDouble(config, symbol, "annualGrowthPct") / 100;
		double minOrderAmount = getDouble(config, symbol, "minOrderAmount");
		double daysPerPeriod = getDaysPerPeriod(symbol, config);
		boolean allowSell = getBoolean(config, symbol, "sell");

		double dailyContrib = contrib / daysPerPeriod;
		double dailyGrowthRate = 1 + annualGrowth / ANNUAL_TRADING_DAYS;
		double expectedAmount = day0Value;
		final LocalDate today = LocalDate.now();
		for (LocalDate date = day0; date.compareTo(today) <= 0; date = date.plusDays(1))
		{
			if (isTradingDay(date))
				expectedAmount = expectedAmount * dailyGrowthRate + dailyContrib;
		}

		Map<String, String> symbolMap = accountMap.get(symbol);
		if (symbolMap != null)
		{
			double actualAmount = SchwabAccountCsv.parsePrice(symbolMap.get("Market Value"));
			double sharePrice = SchwabAccountCsv.parsePrice(symbolMap.get("Price"));

			if (minOrderAmount == 0)
				minOrderAmount = getMinOrderAmount(actualAmount, dailyContrib);

			double delta = expectedAmount - actualAmount;
			long sharesToBuy = Math.round(delta / sharePrice);
			double buyAmount = sharesToBuy * sharePrice;
			if (sharesToBuy < 0)
				minOrderAmount *= 2;

			if (Math.abs(buyAmount) > minOrderAmount && (sharesToBuy > 0 || allowSell))
			{
				String action = sharesToBuy >= 0 ? "Buy " : "Sell";
				System.out.println(String.format("%s %s %d  (@ %.2f = %.0f)", symbol, action, Math.abs(sharesToBuy), sharePrice, buyAmount));
			}
		}
		else
		{
			System.out.println(String.format("Initiate new position in %s", symbol));
		}
	}

	private static boolean isTradingDay(LocalDate date)
	{
		DayOfWeek day = date.getDayOfWeek();
		return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY && !HOLIDAYS.contains(date);
	}

	private static double getMinOrderAmount(double currentValue, double dailyContrib)
	{
		return Math.max(0.006 * Math.min(currentValue, Math.abs(dailyContrib) * ANNUAL_TRADING_DAYS), 20);
	}

	private static double getDaysPerPeriod(String symbol, JSONObject config)
	{
		String period = (String)getValue(config, symbol, "period");
		int multiplier = 1;
		if (period.contains(" "))
		{
			String[] tokens = period.split("\\s+");
			period = tokens[1];
			multiplier = Integer.parseInt(tokens[0]);
		}
		return PERIODS.get(period).intValue() * multiplier;
	}

	private static double getDouble(JSONObject config, String symbol, String key)
	{
		Object value = getValue(config, symbol, key);
		return value instanceof Number ? ((Number)value).doubleValue() : 0;
	}

	private static boolean getBoolean(JSONObject config, String symbol, String key)
	{
		return ((Boolean)getValue(config, symbol, key)).booleanValue();
	}

	private static Object getValue(JSONObject config, String symbol, String key)
	{
		JSONObject symbolConfig = (JSONObject)config.get(symbol);
		Object value = symbolConfig.get(key);
		if (value == null)
		{
			JSONObject defaultConfig = (JSONObject)config.get("_default");
			if (defaultConfig != null)
				value = defaultConfig.get(key);
		}
		return value;
	}
}
