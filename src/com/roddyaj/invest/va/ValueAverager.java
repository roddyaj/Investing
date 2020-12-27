package com.roddyaj.invest.va;

import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import com.roddyaj.invest.model.Program;

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
//		Path settingsFile = Paths.get(dataDir.toString(), "settings.json");

		LocalDate startDate = LocalDate.of(2021, 1, 1);
		LocalDate endDate = LocalDate.of(2021, 12, 31);

		int numTradingDays = 0;
		for (LocalDate date = startDate; date.compareTo(endDate) <= 0; date = date.plusDays(1))
		{
			if (isTradingDay(date))
				numTradingDays++;
		}

		LocalDate today = LocalDate.now();
		evaluate("", startDate, today, numTradingDays);
	}

	private void evaluate(String ticker, LocalDate startDate, LocalDate today, int numTradingDays)
	{
		double day0Value = 0.;
		double dailyContrib = 0.;
		double growthRate = 0.;

		double expectedAmount = day0Value;
		for (LocalDate date = startDate; date.compareTo(today) <= 0; date = date.plusDays(1))
		{
			if (isTradingDay(date))
				expectedAmount = expectedAmount * (1 + growthRate / numTradingDays) + dailyContrib;
		}

		double actualAmount = 0.;
		double sharePrice = 0.;
		double delta = expectedAmount - actualAmount;
		long sharesToBuy = Math.round(delta / sharePrice);
		System.out.println(String.format("%s: Buy %d of %s", today.toString(), sharesToBuy, ticker));
	}

	private static boolean isTradingDay(LocalDate date)
	{
		DayOfWeek day = date.getDayOfWeek();
		return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY && !HOLIDAYS.contains(date);
	}
}
