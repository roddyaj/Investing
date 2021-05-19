package com.roddyaj.invest.va;

import static com.roddyaj.invest.va.TemporalUtil.ANNUAL_TRADING_DAYS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.roddyaj.invest.util.StringUtils;
import com.roddyaj.invest.va.model.Account;
import com.roddyaj.invest.va.model.Order;
import com.roddyaj.invest.va.model.Point;
import com.roddyaj.invest.va.model.Position;
import com.roddyaj.invest.va.model.Report;
import com.roddyaj.invest.va.model.config.AccountSettings;
import com.roddyaj.invest.va.model.config.Allocation;
import com.roddyaj.invest.va.model.config.PositionSettings;
import com.roddyaj.invest.va.model.config.Settings;

public class Algorithm
{
	private final AccountSettings accountSettings;

	private final Account account;

	private final Settings settings;

	private final List<Report> reports = new ArrayList<>();

	private final List<String> warnings = new ArrayList<>();

	public Algorithm(AccountSettings accountSettings, Account account, Settings settings)
	{
		this.accountSettings = accountSettings;
		this.account = account;
		this.settings = settings;
	}

	public void run(int reportLevel)
	{
		if (!LocalDate.now().equals(account.date))
			System.out.println("\n\033[33mAccount data is not from today: " + account.date + "\033[0m");

		if (reportLevel > 0)
			System.out.println("\n---------- ORDERS ----------\n");
		determineAndPrintOrders();

		if (reportLevel > 0)
			report(reportLevel);
	}

	private void determineAndPrintOrders()
	{
		// @formatter:off
		accountSettings.getRealPositions()
			.map(position -> evaluate(position.getSymbol()))
			.filter(Objects::nonNull)
			.sorted((o1, o2) -> Double.compare(o2.getAmount(), o1.getAmount()))
			.forEach(System.out::println);
		// @formatter:on
	}

	private Order evaluate(String symbol)
	{
		PositionSettings positionSettings = accountSettings.getPosition(symbol);
		Position position = account.getPosition(symbol);

		Point p0 = new Point(LocalDate.parse(positionSettings.getT0()), positionSettings.getV0());
		Point p1 = getP1(symbol, p0.date);
		double targetValue;
		if (account.date.isBefore(p1.date))
		{
			targetValue = getTargetValue(p0, p1, account.date, positionSettings.getAnnualGrowth());
		}
		else
		{
			double annualContrib = accountSettings.getAnnualContrib() * accountSettings.getAllocation(symbol);
			targetValue = getFutureValue(p1, account.date, positionSettings.getAnnualGrowth(), annualContrib);
		}

		if (!account.hasSymbol(symbol))
		{
			if (targetValue > 0)
				System.out.println(String.format("Initiate new position in %-4s for $%.2f", symbol, targetValue));
			return null;
		}

		double changePct = StringUtils.parsePercent(position.getValue("Price Change %"));

		double delta = targetValue - position.getMarketValue();
		long sharesToBuy = Math.round(delta / position.getPrice());
		Order order = new Order(symbol, (int)sharesToBuy, position.getPrice(), changePct);

		reports.add(new Report(symbol, p0, p1, targetValue, accountSettings.getAllocation(symbol), position));

		if (p1.value < p0.value && !accountSettings.getSell(symbol))
			warnings.add("Sell not enabled for " + symbol);

		if (allowOrder(order, position))
			return order;
		return null;
	}

	private Point getP1(String symbol, LocalDate t0)
	{
		Period period = Period.parse(accountSettings.getPeriod(symbol));
		long daysInPeriod = TemporalUtil.getDaysApprox(period);
		LocalDate t1 = t0.plusDays(daysInPeriod);
		double v1 = getFutureAccountValue(t1) * accountSettings.getAllocation(symbol);
		return new Point(t1, v1);
	}

	private double getFutureAccountValue(LocalDate t)
	{
		return getFutureValue(new Point(account.date, account.getTotalValue()), t, 0.06, accountSettings.getAnnualContrib());
	}

	private static double getTargetValue(Point p0, Point p1, LocalDate t, double annualGrowth)
	{
		double valueDelta = p1.value - getFutureValue(p0, p1.date, annualGrowth, 0);
		long daysBetween = ChronoUnit.DAYS.between(p0.date, p1.date);
		double annualContrib = (valueDelta / daysBetween) * 365;
		return getFutureValue(p0, t, annualGrowth, annualContrib);
	}

	private static double getFutureValue(Point startPoint, LocalDate futureDate, double annualGrowth, double annualContrib)
	{
		double futureValue = startPoint.value;
		double dailyGrowthRate = 1 + annualGrowth / ANNUAL_TRADING_DAYS;
		double dailyContrib = annualContrib / ANNUAL_TRADING_DAYS;
		for (LocalDate date = startPoint.date; date.compareTo(futureDate) < 0; date = date.plusDays(1))
		{
			if (TemporalUtil.isTradingDay(date))
				futureValue = futureValue * dailyGrowthRate + dailyContrib;
		}
		return futureValue;
	}

	private boolean allowOrder(Order order, Position position)
	{
		double minOrderAmount = Math.max(position.getMarketValue() * 0.005, 50);
		if (order.shareCount < 0)
			minOrderAmount *= 2;
		boolean allowSell = accountSettings.getSell(order.symbol);
		return Math.abs(order.getAmount()) > minOrderAmount && (order.shareCount > 0 || allowSell);
	}

	private void report(int reportLevel)
	{
		System.out.println("\n-------------------------------------- REPORT --------------------------------------\n");

		reports.add(new Report("Cash", null, null, 0, accountSettings.getAllocation("cash"), account.getPosition("Cash & Cash Investments")));

		double eoyAccountValue = getFutureAccountValue(TemporalUtil.END_OF_YEAR);
		System.out.println(String.format("Estimated EOY account value: %6.0f", eoyAccountValue));

		System.out.println(Report.toString(reports));

		for (Position position : account.getPositions())
		{
			if (startsWith(position.getValue("Security Type"), "ETF") && accountSettings.getPosition(position.symbol) == null)
				warnings.add("Position " + position.symbol + " is not being tracked");
		}

		Map<String, Double> validationMap = new HashMap<>();
		for (Allocation allocation : accountSettings.getAllocations())
		{
			int i = allocation.getCat().lastIndexOf(".");
			String categoryParent = i != -1 ? allocation.getCat().substring(0, i) : "_root";
			Double val = validationMap.getOrDefault(categoryParent, 0.);
			val += allocation.getPercent();
			validationMap.put(categoryParent, val);
		}
		for (Map.Entry<String, Double> entry : validationMap.entrySet())
		{
			if (Math.abs(entry.getValue() - 100) > 0.001)
				warnings.add("Category '" + entry.getKey() + "' doesn't add up to 100%: " + entry.getValue());
		}

		if (!warnings.isEmpty())
		{
			System.out.println("\nWarnings:");
			warnings.forEach(System.out::println);
		}

		// Write CSV report
		Path csvPath = Paths.get(settings.getDefaultDataDir(), "report.csv");
		List<Report> csvReports = reports.stream().filter(r -> r.targetPct > 0).collect(Collectors.toList());
		csvReports.add(new Report("Stocks", null, null, 0, accountSettings.getAllocation("stocks"), null));
		try
		{
			Files.writeString(csvPath, Report.toCsvString(csvReports));
			System.out.println("\nWrote " + csvPath);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		if (reportLevel >= 2)
		{
			System.out.println("\nCurrent snapshot of positions:");
			Set<String> symbols = new HashSet<>();
			List<PositionSettings> positions1 = new ArrayList<>();
			for (Allocation allocation : accountSettings.getAllocations())
			{
				String symbol = allocation.getCatLastToken();
				if (symbol.toUpperCase().equals(symbol) && !symbols.contains(symbol))
				{
					symbols.add(symbol);

					Position position = account.getPosition(symbol);
					if (position != null)
					{
						PositionSettings positionSetting = new PositionSettings();
						positionSetting.setSymbol(position.symbol);
						positionSetting.setT0(account.date.toString());
						positionSetting.setV0(position.getMarketValue());
						positionSetting.setAnnualGrowthPct(10);
						positions1.add(positionSetting);
					}
					else
					{
						PositionSettings positionSetting = new PositionSettings();
						positionSetting.setSymbol(symbol);
						positionSetting.setT0(account.date.toString());
						positionSetting.setV0(0);
						positionSetting.setAnnualGrowthPct(10);
						positions1.add(positionSetting);
					}
				}
			}
			List<PositionSettings> positions2 = new ArrayList<>();
			for (Position position : account.getPositions())
			{
				if (startsWith(position.getValue("Security Type"), "ETF") && !symbols.contains(position.symbol))
				{
					symbols.add(position.symbol);

					PositionSettings positionSetting = new PositionSettings();
					positionSetting.setSymbol(position.symbol);
					positionSetting.setT0(account.date.toString());
					positionSetting.setV0(position.getMarketValue());
					positionSetting.setAnnualGrowthPct(10);
					positions2.add(positionSetting);
				}
			}
			positions1.stream().forEach(System.out::println);
			System.out.println();
			positions2.stream().sorted((p1, p2) -> p1.getSymbol().compareTo(p2.getSymbol())).forEach(System.out::println);
		}
	}

	private static boolean startsWith(String s1, String s2)
	{
		return s1 != null && s1.startsWith(s2);
	}
}
