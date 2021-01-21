package com.roddyaj.invest.va;

import static com.roddyaj.invest.va.TemporalUtil.ANNUAL_TRADING_DAYS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roddyaj.invest.model.Program;
import com.roddyaj.invest.util.JSONUtils;
import com.roddyaj.invest.va.api.schwab.SchwabAccountCsv;
import com.roddyaj.invest.va.model.Account;
import com.roddyaj.invest.va.model.Allocation;
import com.roddyaj.invest.va.model.Order;
import com.roddyaj.invest.va.model.Point;
import com.roddyaj.invest.va.model.Position;
import com.roddyaj.invest.va.model.config.Settings;

public class ValueAverager implements Program
{
	private final Path dataDir;

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

//		Settings settings = readSettingsJackson();

		JSONObject settings = readSettings();
		String accountKey = accountFile.getFileName().toString().split("-", 2)[0];
		JSONArray accountsConfig = (JSONArray)settings.get("accounts");
		Optional<JSONObject> accountConfig = accountsConfig.stream().filter(a -> ((JSONObject)a).get("name").equals(accountKey))
				.map(a -> (JSONObject)a).findAny();
		JSONArray positionsConfig = (JSONArray)accountConfig.get().get("positions");

		Allocation allocation = new Allocation((JSONArray)accountConfig.get().get("allocations"));

		List<Order> orders = new ArrayList<>();
		for (Object positionObj : positionsConfig)
		{
			JSONObject position = (JSONObject)positionObj;
			String symbol = (String)position.get("symbol");
			if (!symbol.startsWith("_"))
			{
				if (account.hasSymbol(symbol))
				{
					Order order = evaluate(symbol, accountConfig.get(), account, allocation);
					if (order != null)
						orders.add(order);
				}
				else
				{
					System.out.println(String.format("Initiate new position in %s", symbol));
				}
			}
		}

		orders.stream().sorted((o1, o2) -> Double.compare(o2.getAmount(), o1.getAmount())).forEach(System.out::println);
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

	private Settings readSettingsJackson() throws IOException
	{
		Path settingsFile = Paths.get(dataDir.toString(), "settings.json");
		Settings settings = new ObjectMapper().readValue(settingsFile.toFile(), Settings.class);
		return settings;
	}

	private Order evaluate(String symbol, JSONObject accountConfig, Account account, Allocation allocation)
	{
		JSONArray positionsConfig = (JSONArray)accountConfig.get("positions");
		Position position = account.getPosition(symbol);

		Point p0 = getP0(symbol, positionsConfig);
		Point p1 = getP1(symbol, accountConfig, account, allocation, p0.date);
		double targetValue = getTargetValue(symbol, positionsConfig, p0, p1);

		double delta = targetValue - position.getMarketValue();
		long sharesToBuy = Math.round(delta / position.getPrice());
		Order order = new Order(symbol, (int)sharesToBuy, position.getPrice());

		if (allowOrder(order, position, positionsConfig))
			return order;
		return null;
	}

	private Point getP0(String symbol, JSONArray positionsConfig)
	{
		LocalDate t0 = LocalDate.parse((String)getValue(positionsConfig, symbol, "t0"));
		double v0 = getDouble(positionsConfig, symbol, "v0");
		return new Point(t0, v0);
	}

	private Point getP1(String symbol, JSONObject accountConfig, Account account, Allocation allocation, LocalDate t0)
	{
		JSONArray positionsConfig = (JSONArray)accountConfig.get("positions");
		LocalDate t1 = t0.plusDays(getDaysPerPeriod(symbol, positionsConfig, TemporalUtil.REAL_PERIODS));
		double dailyAccountContrib = JSONUtils.getDouble(accountConfig, "annualContrib") / ANNUAL_TRADING_DAYS;
		double futureAccountTotal = getFutureValue(new Point(LocalDate.now(), account.getTotalValue()), t1, 0.06, dailyAccountContrib);
		double v1 = futureAccountTotal * allocation.getAllocation(symbol);
		return new Point(t1, v1);
	}

	private double getTargetValue(String symbol, JSONArray positionsConfig, Point p0, Point p1)
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
			if (TemporalUtil.isTradingDay(date))
				futureValue = futureValue * dailyGrowthRate + dailyContrib;
		}
		return futureValue;
	}

	private static boolean allowOrder(Order order, Position position, JSONArray positionsConfig)
	{
		double minOrderAmount = Math.max(position.getMarketValue() * 0.006, 20);
		if (order.shareCount < 0)
			minOrderAmount *= 2;
		boolean allowSell = getBoolean(positionsConfig, order.symbol, "sell");
		return Math.abs(order.getAmount()) > minOrderAmount && (order.shareCount > 0 || allowSell);
	}

	private static int getDaysPerPeriod(String symbol, JSONArray config, Map<String, ? extends Number> periods)
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

	private static double getDouble(JSONArray config, String symbol, String key)
	{
		Object value = getValue(config, symbol, key);
		return value instanceof Number ? ((Number)value).doubleValue() : 0;
	}

	private static boolean getBoolean(JSONArray config, String symbol, String key)
	{
		return ((Boolean)getValue(config, symbol, key)).booleanValue();
	}

	private static Object getValue(JSONArray config, String symbol, String key)
	{
		JSONObject position = getPosition(config, symbol);
		Object value = position.get(key);
		if (value == null)
		{
			JSONObject defaultConfig = getPosition(config, "_default");
			if (defaultConfig != null)
				value = defaultConfig.get(key);
		}
		return value;
	}

	private static JSONObject getPosition(JSONArray config, String symbol)
	{
		JSONObject match = null;
		for (Object positionObj : config)
		{
			JSONObject position = (JSONObject)positionObj;
			if (symbol.equals(position.get("symbol")))
				match = position;
		}
		return match;
	}
}
