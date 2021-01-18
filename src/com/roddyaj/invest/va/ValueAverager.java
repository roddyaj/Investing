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
		REAL_PERIODS.put("year", (double)ANNUAL_TRADING_DAYS);
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
		Position position = account.getPosition(symbol);
		double actualAmount = position.getMarketValue();
		double sharePrice = position.getPrice();

		JSONObject positionsConfig = (JSONObject)accountConfig.get("positions");
		LocalDate day0 = LocalDate.parse((String)getValue(positionsConfig, symbol, "t0"));
		double day0Value = getDouble(positionsConfig, symbol, "v0");
		Object contribOverride = getValue(positionsConfig, symbol, "contrib");
		double annualGrowth = getDouble(positionsConfig, symbol, "annualGrowthPct") / 100;
		double minOrderAmount = getDouble(positionsConfig, symbol, "minOrderAmount");
		double tradingDaysPerPeriod = getDaysPerPeriod(symbol, positionsConfig, TRADING_PERIODS);
		boolean allowSell = getBoolean(positionsConfig, symbol, "sell");

		double contrib;
		if (contribOverride instanceof Number)
		{
			contrib = ((Number)contribOverride).doubleValue();
		}
		else
		{
			double estPortfolioBalance = getEstPortfolioBalance(symbol, accountConfig, day0, account.getTotalValue());
			contrib = allocation.getAllocation(symbol) * estPortfolioBalance - actualAmount;
		}

		double dailyContrib = contrib / tradingDaysPerPeriod;
		double dailyGrowthRate = 1 + annualGrowth / ANNUAL_TRADING_DAYS;
		double expectedAmount = day0Value;
		final LocalDate today = LocalDate.now();
		for (LocalDate date = day0; date.compareTo(today) <= 0; date = date.plusDays(1))
		{
			if (isTradingDay(date))
				expectedAmount = expectedAmount * dailyGrowthRate + dailyContrib;
		}

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

//		double percent = (actualAmount / account.getTotalValue()) * 100;
//		System.out.println(symbol + " Current: " + percent + " Desired: " + (allocation.getAllocation(symbol) * 100));
	}

	private static double getEstPortfolioBalance(String symbol, JSONObject accountConfig, LocalDate day0, double accountTotal)
	{
		JSONObject positionsConfig = (JSONObject)accountConfig.get("positions");
		double realDaysPerPeriod = getDaysPerPeriod(symbol, positionsConfig, REAL_PERIODS);
		LocalDate futureDate = day0.plusDays(Math.round(realDaysPerPeriod));
		long numDays = ChronoUnit.DAYS.between(LocalDate.now(), futureDate);
		double accountContrib = JSONUtils.getDouble(accountConfig, "annualContrib");
		double dailyContrib = accountContrib / 365;

		return accountTotal + numDays * dailyContrib;
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

	private static double getDaysPerPeriod(String symbol, JSONObject config, Map<String, ? extends Number> periods)
	{
		String period = (String)getValue(config, symbol, "period");
		double multiplier = 1;
		if (period.contains(" "))
		{
			String[] tokens = period.split("\\s+");
			period = tokens[1];
			multiplier = Double.parseDouble(tokens[0]);
		}
		return periods.get(period).doubleValue() * multiplier;
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
