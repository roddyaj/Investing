package com.roddyaj.invest.va;

import static com.roddyaj.invest.va.TemporalUtil.ANNUAL_TRADING_DAYS;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.roddyaj.invest.va.model.Account;
import com.roddyaj.invest.va.model.Order;
import com.roddyaj.invest.va.model.Point;
import com.roddyaj.invest.va.model.Position;
import com.roddyaj.invest.va.model.Report;
import com.roddyaj.invest.va.model.config.AccountSettings;
import com.roddyaj.invest.va.model.config.Allocation;
import com.roddyaj.invest.va.model.config.PositionSettings;

public class Algorithm
{
	private final AccountSettings accountSettings;

	private final Account account;

	private final List<Report> reports = new ArrayList<>();

	private final List<String> warnings = new ArrayList<>();

	public Algorithm(AccountSettings accountSettings, Account account)
	{
		this.accountSettings = accountSettings;
		this.account = account;
	}

	public void run(boolean report)
	{
		determineAndPrintOrders();

		if (report)
			report();
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

	private void report()
	{
		double eoyAccountValue = getFutureAccountValue(TemporalUtil.END_OF_YEAR);
		System.out.println(String.format("\nEstimated EOY account value: %6.0f", eoyAccountValue));

		System.out.println(Report.getHeader());
		reports.forEach(System.out::println);

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
			if (entry.getValue() != 100)
				warnings.add("Category '" + entry.getKey() + "' doesn't add up to 100%: " + entry.getValue());
		}

		if (!warnings.isEmpty())
		{
			System.out.println("\nWarnings:");
			warnings.forEach(System.out::println);
		}

		System.out.println("\nCurrent snapshot of positions:");
		// TODO copy before modifying
		for (PositionSettings positionSetting : accountSettings.getPositions())
		{
			Position position = account.getPosition(positionSetting.getSymbol());
			if (position != null)
			{
				positionSetting.setT0(LocalDate.now().toString());
				positionSetting.setV0(position.getMarketValue());
			}
		}
		accountSettings.getRealPositions().forEach(System.out::println);
	}

	private Order evaluate(String symbol)
	{
		if (!account.hasSymbol(symbol))
		{
			System.out.println(String.format("Initiate new position in %s", symbol));
			return null;
		}

		PositionSettings positionSettings = accountSettings.getPosition(symbol);
		Position position = account.getPosition(symbol);

		Point p0 = new Point(LocalDate.parse(positionSettings.getT0()), positionSettings.getV0());
		Point p1 = getP1(symbol, p0.date);
		double targetValue = getTargetValue(positionSettings, p0, p1);

		double delta = targetValue - position.getMarketValue();
		long sharesToBuy = Math.round(delta / position.getPrice());
		Order order = new Order(symbol, (int)sharesToBuy, position.getPrice());

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
		return getFutureValue(new Point(LocalDate.now(), account.getTotalValue()), t, 0.06, accountSettings.getAnnualContrib());
	}

	private static double getTargetValue(PositionSettings positionSettings, Point p0, Point p1)
	{
		double annualGrowth = positionSettings.getAnnualGrowthPct() / 100;
		double valueDelta = p1.value - getFutureValue(p0, p1.date, annualGrowth, 0);
		long daysBetween = ChronoUnit.DAYS.between(p0.date, p1.date);
		double annualContrib = (valueDelta / daysBetween) * 365;
		return getFutureValue(p0, LocalDate.now(), annualGrowth, annualContrib);
	}

	private static double getFutureValue(Point startPoint, LocalDate futureDate, double annualGrowthRate, double annualContrib)
	{
		double futureValue = startPoint.value;
		double dailyGrowthRate = 1 + annualGrowthRate / ANNUAL_TRADING_DAYS;
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
		double minOrderAmount = Math.max(position.getMarketValue() * 0.005, 35);
		if (order.shareCount < 0)
			minOrderAmount *= 2;
		boolean allowSell = accountSettings.getSell(order.symbol);
		return Math.abs(order.getAmount()) > minOrderAmount && (order.shareCount > 0 || allowSell);
	}

	private static boolean startsWith(String s1, String s2)
	{
		return s1 != null && s1.startsWith(s2);
	}
}
