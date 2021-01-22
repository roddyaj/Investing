package com.roddyaj.invest.va;

import static com.roddyaj.invest.va.TemporalUtil.ANNUAL_TRADING_DAYS;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import com.roddyaj.invest.va.model.Account;
import com.roddyaj.invest.va.model.Order;
import com.roddyaj.invest.va.model.Point;
import com.roddyaj.invest.va.model.Position;
import com.roddyaj.invest.va.model.config.AccountSettings;
import com.roddyaj.invest.va.model.config.PositionSettings;

public class Algorithm
{
	public Order evaluate(String symbol, AccountSettings accountSettings, Account account)
	{
		if (!account.hasSymbol(symbol))
		{
			System.out.println(String.format("Initiate new position in %s", symbol));
			return null;
		}

		PositionSettings positionSettings = accountSettings.getPosition(symbol);
		Position position = account.getPosition(symbol);

		Point p0 = new Point(LocalDate.parse(positionSettings.getT0()), positionSettings.getV0());
		Point p1 = getP1(symbol, accountSettings, account, p0.date);
		double targetValue = getTargetValue(positionSettings, p0, p1);

		double delta = targetValue - position.getMarketValue();
		long sharesToBuy = Math.round(delta / position.getPrice());
		Order order = new Order(symbol, (int)sharesToBuy, position.getPrice());

		if (allowOrder(order, position, accountSettings))
			return order;
		return null;
	}

	private static Point getP1(String symbol, AccountSettings accountSettings, Account account, LocalDate t0)
	{
		LocalDate t1 = t0.plusDays(getDaysPerPeriod(symbol, accountSettings, TemporalUtil.REAL_PERIODS));
		double dailyAccountContrib = accountSettings.getAnnualContrib() / ANNUAL_TRADING_DAYS;
		double futureAccountTotal = getFutureValue(new Point(LocalDate.now(), account.getTotalValue()), t1, 0.06, dailyAccountContrib);
		double v1 = futureAccountTotal * accountSettings.getAllocation(symbol);
		return new Point(t1, v1);
	}

	private static double getTargetValue(PositionSettings positionSettings, Point p0, Point p1)
	{
		double annualGrowth = positionSettings.getAnnualGrowthPct() / 100;
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

	private static boolean allowOrder(Order order, Position position, AccountSettings accountSettings)
	{
		double minOrderAmount = Math.max(position.getMarketValue() * 0.006, 20);
		if (order.shareCount < 0)
			minOrderAmount *= 2;
		boolean allowSell = accountSettings.getSell(order.symbol);
		return Math.abs(order.getAmount()) > minOrderAmount && (order.shareCount > 0 || allowSell);
	}

	private static int getDaysPerPeriod(String symbol, AccountSettings accountSettings, Map<String, ? extends Number> periods)
	{
		String period = accountSettings.getPeriod(symbol);
		double multiplier = 1;
		if (period.contains(" "))
		{
			String[] tokens = period.split("\\s+");
			period = tokens[1];
			multiplier = Double.parseDouble(tokens[0]);
		}
		return (int)Math.round(periods.get(period).doubleValue() * multiplier);
	}
}
