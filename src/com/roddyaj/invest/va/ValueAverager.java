package com.roddyaj.invest.va;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.roddyaj.invest.model.Program;
import com.roddyaj.invest.util.JSONUtils;
import com.roddyaj.invest.va.api.schwab.SchwabAccountCsv;
import com.roddyaj.invest.va.model.Account;
import com.roddyaj.invest.va.model.Point;
import com.roddyaj.invest.va.model.Position;

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

	private static final Map<String, Integer> TRADING_PERIODS = new HashMap<>();
	static
	{
		TRADING_PERIODS.put("day", 1);
		TRADING_PERIODS.put("week", 5);
		TRADING_PERIODS.put("month", 21);
		TRADING_PERIODS.put("year", ANNUAL_TRADING_DAYS);
	}

	private static final Map<String, Double> REAL_PERIODS = new HashMap<>();
	static
	{
		REAL_PERIODS.put("day", 1.);
		REAL_PERIODS.put("week", 7.);
		REAL_PERIODS.put("month", 30.44);
		REAL_PERIODS.put("year", 365.);
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
		Account account = SchwabAccountCsv.parse(accountFile);

		JSONObject settings = readSettings();
		String accountKey = accountFile.getFileName().toString().split("-", 2)[0];
		JSONObject accountConfig = (JSONObject)settings.get(accountKey);
		JSONObject positionsConfig = (JSONObject)accountConfig.get("positions");

		Allocation allocation = new Allocation((JSONObject)accountConfig.get("allocation"));

		for (Object key : positionsConfig.keySet())
		{
			String symbol = (String)key;
			if (!symbol.startsWith("_"))
			{
				if (account.hasSymbol(symbol))
					evaluate(symbol, accountConfig, account, allocation);
				else
					System.out.println(String.format("Initiate new position in %s", symbol));
			}
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

	private void evaluate(String symbol, JSONObject accountConfig, Account account, Allocation allocation)
	{
		JSONObject positionsConfig = (JSONObject)accountConfig.get("positions");
		Position position = account.getPosition(symbol);

		Point p0 = getP0(symbol, positionsConfig);
		Point p1 = getP1(symbol, accountConfig, account, allocation, p0.date);
		double targetValue = getTargetValue(symbol, positionsConfig, p0, p1);

		double delta = targetValue - position.getMarketValue();
		long sharesToBuy = Math.round(delta / position.getPrice());
		double buyAmount = sharesToBuy * position.getPrice();

		double minOrderAmount = Math.max(position.getMarketValue() * 0.006, 20);
		if (sharesToBuy < 0)
			minOrderAmount *= 2;
		boolean allowSell = getBoolean(positionsConfig, symbol, "sell");
		if (Math.abs(buyAmount) > minOrderAmount && (sharesToBuy > 0 || allowSell))
		{
			String action = sharesToBuy >= 0 ? "Buy " : "Sell";
			System.out.println(String.format("%s %s %d  (@ %.2f = %.0f)", symbol, action, Math.abs(sharesToBuy), position.getPrice(), buyAmount));
		}
	}

	private Point getP0(String symbol, JSONObject positionsConfig)
	{
		LocalDate t0 = LocalDate.parse((String)getValue(positionsConfig, symbol, "t0"));
		double v0 = getDouble(positionsConfig, symbol, "v0");
		return new Point(t0, v0);
	}

	private Point getP1(String symbol, JSONObject accountConfig, Account account, Allocation allocation, LocalDate t0)
	{
		JSONObject positionsConfig = (JSONObject)accountConfig.get("positions");
		LocalDate t1 = t0.plusDays(getDaysPerPeriod(symbol, positionsConfig, REAL_PERIODS));
		double dailyAccountContrib = JSONUtils.getDouble(accountConfig, "annualContrib") / ANNUAL_TRADING_DAYS;
		double futureAccountTotal = getFutureValue(new Point(LocalDate.now(), account.getTotalValue()), t1, 0.06, dailyAccountContrib);
		double v1 = futureAccountTotal * allocation.getAllocation(symbol);
		return new Point(t1, v1);
	}

	private double getTargetValue(String symbol, JSONObject positionsConfig, Point p0, Point p1)
	{
		double annualGrowth = getDouble(positionsConfig, symbol, "annualGrowthPct") / 100;
		int numTradingDaysP0ToP1 = (int)ChronoUnit.DAYS.between(p0.date, p1.date) * ANNUAL_TRADING_DAYS / 365;
		double dailyContrib = (p1.value - getFutureValue(p0, p1.date, annualGrowth, 0)) / numTradingDaysP0ToP1;
		return getFutureValue(p0, LocalDate.now(), annualGrowth, dailyContrib);
	}

	private static double getFutureValue(Point startPoint, LocalDate futureDate, double annualGrowthRate, double dailyContrib)
	{
		double futureValue = startPoint.value;
		double dailyGrowthRate = 1 + annualGrowthRate / ANNUAL_TRADING_DAYS;
		for (LocalDate date = startPoint.date; date.compareTo(futureDate) < 0; date = date.plusDays(1))
		{
			if (isTradingDay(date))
				futureValue = futureValue * dailyGrowthRate + dailyContrib;
		}
		return futureValue;
	}

	private static boolean isTradingDay(LocalDate date)
	{
		DayOfWeek day = date.getDayOfWeek();
		return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY && !HOLIDAYS.contains(date);
	}

	private static int getDaysPerPeriod(String symbol, JSONObject config, Map<String, ? extends Number> periods)
	{
		String period = (String)getValue(config, symbol, "period");
		double multiplier = 1;
		if (period.contains(" "))
		{
			String[] tokens = period.split("\\s+");
			period = tokens[1];
			multiplier = Double.parseDouble(tokens[0]);
		}
		return (int)Math.round(periods.get(period).doubleValue() * multiplier);
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
