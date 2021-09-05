package com.roddyaj.invest.programs.positions;

import static com.roddyaj.invest.programs.positions.TemporalUtil.ANNUAL_TRADING_DAYS;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.roddyaj.invest.model.Account;
import com.roddyaj.invest.model.Action;
import com.roddyaj.invest.model.Input;
import com.roddyaj.invest.model.Message.Level;
import com.roddyaj.invest.model.Order;
import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.model.SecurityType;
import com.roddyaj.invest.model.settings.AccountSettings;
import com.roddyaj.invest.model.settings.Allocation;
import com.roddyaj.invest.model.settings.PositionSettings;
import com.roddyaj.invest.model.settings.Settings;

public class PositionManagerCore
{
	private final Account account;

	private final Settings settings;

	private final AccountSettings accountSettings;

	private final PositionManagerOutput output;

	private final List<Report> reports = new ArrayList<>();

	public PositionManagerCore(Input input)
	{
		account = input.account;
		settings = input.getSettings();
		accountSettings = input.account.getAccountSettings();
		output = new PositionManagerOutput();
	}

	public PositionManagerOutput run()
	{
		if (accountSettings == null)
		{
			output.addMessage(Level.INFO, "Account not found: " + account.getName());
			return output;
		}

		// Create the allocation map
		double untrackedTotal = account.getPositions().stream().filter(p -> p.getQuantity() > 0 && !accountSettings.hasAllocation(p.getSymbol()))
				.mapToDouble(Position::getMarketValue).sum();
		double untrackedPercent = untrackedTotal / account.getTotalValue();
		accountSettings.createMap(untrackedPercent, output);

		if (!LocalDate.now().equals(account.getDate()))
			output.addMessage(Level.WARN, "Account data is not from today: " + account.getDate());

		// Determine the managed orders
		List<Order> orders = accountSettings.getRealPositions().map(p -> evaluate(p.getSymbol())).filter(Objects::nonNull)
				.sorted((o1, o2) -> Double.compare(o1.getAmount(), o2.getAmount())).collect(Collectors.toList());
		output.addOrders(orders);

		// Handle odd lots
		output.addUnmanagedOrders(new OddLots(account, accountSettings).calculateOddLots());

		report(1);

		return output;
	}

	private Order evaluate(String symbol)
	{
		PositionSettings positionSettings = accountSettings.getPosition(symbol);
		Position position = account.getPosition(symbol);

		Point p0 = new Point(LocalDate.parse(positionSettings.getT0()), positionSettings.getV0());
		Point p1 = getP1(symbol, p0.date);
		double targetValue;
		if (account.getDate().isBefore(p1.date))
		{
			targetValue = getTargetValue(p0, p1, account.getDate(), settings.getAnnualGrowth(symbol));
		}
		else
		{
			double annualContrib = accountSettings.getAnnualContrib() * accountSettings.getAllocation(symbol);
			targetValue = getFutureValue(p1, account.getDate(), settings.getAnnualGrowth(symbol), annualContrib);
		}

		if (!account.hasSymbol(symbol))
		{
			if (targetValue > 0)
				return new Order(symbol, 0, targetValue, null);
			else
				return null;
		}

		double delta = targetValue - position.getMarketValue();
		long sharesToBuy = Math.round(delta / position.getPrice());
		Order order = new Order(symbol, (int)sharesToBuy, position.getPrice(), position);
		order.setOptional(order.getPosition() != null
				&& (order.getQuantity() >= 0 ? order.getPosition().getDayChangePct() > .1 : order.getPosition().getDayChangePct() < -.1));
		order.setOpenOrders(account.getOpenOrders(symbol, order.getQuantity() >= 0 ? Action.BUY : Action.SELL, null));

		reports.add(new Report(symbol, p0, p1, targetValue, accountSettings.getAllocation(symbol), position));

//		if (p1.value < p0.value && !accountSettings.getSell(symbol))
//			output.addMessage(Level.WARN, "Sell not enabled for " + symbol);

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
		return getFutureValue(new Point(account.getDate(), account.getTotalValue()), t, 0.06, accountSettings.getAnnualContrib());
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
		if (order.getQuantity() < 0)
			minOrderAmount *= 2;
		boolean allowSell = accountSettings.getSell(order.getSymbol());
		return Math.abs(order.getAmount()) > minOrderAmount && (order.getQuantity() > 0 || allowSell);
	}

	private void report(int reportLevel)
	{
		reports.add(new Report("Cash", null, null, 0, accountSettings.getAllocation("cash"), account.getPosition("Cash & Cash Investments")));

		double eoyAccountValue = getFutureAccountValue(TemporalUtil.END_OF_YEAR);
		System.out.println(String.format("\nEstimated EOY account value: %6.0f", eoyAccountValue));

		System.out.println(Report.toString(reports));

//		for (Position position : account.getPositions())
//		{
//			if (startsWith(position.securityType, "ETF") && accountSettings.getPosition(position.symbol) == null)
//				output.addMessage(Level.WARN, "Position " + position.symbol + " is not being tracked");
//		}

//		// Write CSV report
//		Path csvPath = Paths.get(settings.getDefaultDataDir(), "report.csv");
//		List<Report> csvReports = reports.stream().filter(r -> r.targetPct > 0).collect(Collectors.toList());
//		csvReports.add(new Report("Stocks", null, null, 0, accountSettings.getAllocation("stocks"), null));
//		try
//		{
//			Files.writeString(csvPath, Report.toCsvString(csvReports));
//			System.out.println("\nWrote " + csvPath);
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//		}

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
						positionSetting.setSymbol(position.getSymbol());
						positionSetting.setT0(account.getDate().toString());
						positionSetting.setV0(position.getMarketValue());
						positions1.add(positionSetting);
					}
					else
					{
						PositionSettings positionSetting = new PositionSettings();
						positionSetting.setSymbol(symbol);
						positionSetting.setT0(account.getDate().toString());
						positionSetting.setV0(0);
						positions1.add(positionSetting);
					}
				}
			}
			List<PositionSettings> positions2 = new ArrayList<>();
			for (Position position : account.getPositions())
			{
				if (position.getSecurityType() == SecurityType.ETF && !symbols.contains(position.getSymbol()))
				{
					symbols.add(position.getSymbol());

					PositionSettings positionSetting = new PositionSettings();
					positionSetting.setSymbol(position.getSymbol());
					positionSetting.setT0(account.getDate().toString());
					positionSetting.setV0(position.getMarketValue());
					positions2.add(positionSetting);
				}
			}
			positions1.stream().forEach(System.out::println);
			System.out.println();
			positions2.stream().sorted((p1, p2) -> p1.getSymbol().compareTo(p2.getSymbol())).forEach(System.out::println);
		}
	}
}